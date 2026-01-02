package az.kapitalbank.mb.bff.transfermobile.transfer.services;

import az.kapitalbank.mb.bff.transfermobile.transfer.clients.AccountClient;
import az.kapitalbank.mb.bff.transfermobile.transfer.clients.CardClient;
import az.kapitalbank.mb.bff.transfermobile.transfer.clients.CustomerClient;
import az.kapitalbank.mb.bff.transfermobile.transfer.dtos.requests.CreateTransferRequest;
import az.kapitalbank.mb.bff.transfermobile.transfer.dtos.requests.CreditAccountRequest;
import az.kapitalbank.mb.bff.transfermobile.transfer.dtos.requests.DebitAccountRequest;
import az.kapitalbank.mb.bff.transfermobile.transfer.dtos.responses.TransferResponse;
import az.kapitalbank.mb.bff.transfermobile.transfer.entities.Transfer;
import az.kapitalbank.mb.bff.transfermobile.transfer.enums.TransferStatus;
import az.kapitalbank.mb.bff.transfermobile.transfer.enums.TransferType;
import az.kapitalbank.mb.bff.transfermobile.transfer.exceptions.CustomerNotFoundException;
import az.kapitalbank.mb.bff.transfermobile.transfer.exceptions.InvalidTransferException;
import az.kapitalbank.mb.bff.transfermobile.transfer.exceptions.TransferNotFoundException;
import az.kapitalbank.mb.bff.transfermobile.transfer.mappers.TransferMapper;
import az.kapitalbank.mb.bff.transfermobile.transfer.repositories.TransferRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransferService {

    private final TransferRepository transferRepository;
    private final TransferMapper transferMapper;
    private final CustomerClient customerClient;
    private final AccountClient accountClient;
    private final CardClient cardClient;


    @Transactional
    public TransferResponse createTransfer(CreateTransferRequest request) {

        validateRequest(request);

        BigDecimal tariff = calculateTariff(request.getType());
        BigDecimal commission = calculateCommission(request.getAmount(), request.getType());
        BigDecimal totalDebit = request.getAmount().add(tariff).add(commission);

        checkSufficientBalance(request.getPayerId(), request.getType(), totalDebit);

        Transfer transfer = transferMapper.convertToEntity(request);
        transfer.setTariff(tariff);
        transfer.setCommission(commission);
        transfer.setTotalAmount(totalDebit);

        transfer = transferRepository.save(transfer);

        try {
            executeTransfer(request, totalDebit);

            transfer.setStatus(TransferStatus.COMPLETED);

        } catch (feign.FeignException ex) {
            transfer.setStatus(TransferStatus.FAILED);
            transferRepository.save(transfer);

            throw new InvalidTransferException(
                    "Transfer failed due to downstream service error",
                    ex
            );
        }

        transfer = transferRepository.save(transfer);
        return transferMapper.convertToResponse(transfer);
    }


    private void executeTransfer(CreateTransferRequest request, BigDecimal totalDebit) {

        switch (request.getType()) {

            case CARD_TO_CARD -> {
                validateCardExists(request.getPayerId());
                validateCardExists(request.getPayeeId());

                cardClient.debit(
                        request.getPayerId(),
                        new DebitAccountRequest(totalDebit)
                );

                cardClient.credit(
                        request.getPayeeId(),
                        new CreditAccountRequest(request.getAmount())
                );
            }

            case ACCOUNT_TO_CARD -> {
                validateCustomerExists(request.getPayerId());

                Long payeeCardId = cardClient.getCardIdByCustomerId(request.getPayeeId());
                if (payeeCardId == null) {
                    throw new InvalidTransferException(
                            "Payee customer has no active card"
                    );
                }

                accountClient.debit(
                        request.getPayerId(),
                        new DebitAccountRequest(totalDebit)
                );

                cardClient.credit(
                        payeeCardId,
                        new CreditAccountRequest(request.getAmount())
                );
            }
        }
    }


    private void checkSufficientBalance(
            Long payerId,
            TransferType type,
            BigDecimal totalDebit
    ) {

        if (totalDebit == null || totalDebit.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTransferException(
                    "Total debit amount must be greater than zero"
            );
        }

        BigDecimal balance;

        try {
            balance = switch (type) {
                case CARD_TO_CARD ->
                        cardClient.getBalance(payerId).getBalance();
                case ACCOUNT_TO_CARD ->
                        accountClient.getBalance(payerId).getBalance();
            };
        } catch (feign.FeignException ex) {
            throw new InvalidTransferException(
                    "Balance service unavailable",
                    ex
            );
        }

        if (balance == null || balance.compareTo(totalDebit) < 0) {
            throw new InvalidTransferException("Insufficient balance");
        }
    }


    private void validateRequest(CreateTransferRequest request) {

        if (request.getPayerId().equals(request.getPayeeId())) {
            throw new InvalidTransferException(
                    "Payer and payee cannot be the same"
            );
        }

        validatePositiveId(request.getPayerId(), "Payer id");
        validatePositiveId(request.getPayeeId(), "Payee id");

        if (request.getType() == null) {
            throw new InvalidTransferException("Transfer type is required");
        }
    }

    private void validateCustomerExists(Long customerId) {
        validatePositiveId(customerId, "Customer id");

        try {
            if (!customerClient.existsById(customerId)) {
                throw new CustomerNotFoundException(customerId);
            }
        } catch (feign.FeignException ex) {
            throw new InvalidTransferException(
                    "Customer service unavailable",
                    ex
            );
        }
    }

    private void validateCardExists(Long cardId) {
        validatePositiveId(cardId, "Card id");

        try {
            boolean exists = cardClient.exists(cardId);
            if (!exists) {
                throw new InvalidTransferException(
                        "Card not found with id: " + cardId
                );
            }
        } catch (feign.FeignException.NotFound ex) {
            throw new InvalidTransferException(
                    "Card not found with id: " + cardId,
                    ex
            );
        } catch (feign.FeignException ex) {
            throw new InvalidTransferException(
                    "Card service unavailable",
                    ex
            );
        }
    }

    private void validatePositiveId(Long id, String fieldName) {
        if (id == null || id <= 0) {
            throw new InvalidTransferException(
                    fieldName + " must be positive"
            );
        }
    }


    private BigDecimal calculateTariff(TransferType type) {
        return switch (type) {
            case CARD_TO_CARD -> new BigDecimal("1.00");
            case ACCOUNT_TO_CARD -> new BigDecimal("0.50");
        };
    }

    private BigDecimal calculateCommission(
            BigDecimal amount,
            TransferType type
    ) {

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTransferException(
                    "Transfer amount must be greater than zero"
            );
        }

        BigDecimal rate = switch (type) {
            case CARD_TO_CARD -> new BigDecimal("0.02");
            case ACCOUNT_TO_CARD -> new BigDecimal("0.01");
        };

        return amount.multiply(rate);
    }


    public TransferResponse getTransferById(Long id) {
        validatePositiveId(id, "Transfer id");

        Transfer transfer = transferRepository.findById(id)
                .orElseThrow(() -> new TransferNotFoundException(id));

        return transferMapper.convertToResponse(transfer);
    }

    public List<TransferResponse> getTransfersByPayeeId(Long payeeId) {
        validatePositiveId(payeeId, "Payee id");

        return transferMapper.convertToResponseList(
                transferRepository.findAllByPayeeId(payeeId)
        );
    }

    public List<TransferResponse> getAllTransfers() {
        return transferMapper.convertToResponseList(
                transferRepository.findAll()
        );
    }


    @Transactional
    public TransferResponse cancelTransfer(Long id) {
        validatePositiveId(id, "Transfer id");

        Transfer transfer = transferRepository.findById(id)
                .orElseThrow(() -> new TransferNotFoundException(id));

        if (transfer.getStatus() != TransferStatus.PENDING) {
            throw new InvalidTransferException(
                    "Only PENDING transfers can be cancelled"
            );
        }

        transfer.setStatus(TransferStatus.CANCELLED);
        return transferMapper.convertToResponse(
                transferRepository.save(transfer)
        );
    }

    @Transactional
    public TransferResponse updateTransfer(
            Long id,
            TransferStatus status
    ) {

        validatePositiveId(id, "Transfer id");

        if (status == null) {
            throw new InvalidTransferException(
                    "Transfer status must not be null"
            );
        }

        Transfer transfer = transferRepository.findById(id)
                .orElseThrow(() -> new TransferNotFoundException(id));

        if (transfer.getStatus() != TransferStatus.PENDING) {
            throw new InvalidTransferException(
                    "Only PENDING transfers can change status"
            );
        }

        transfer.setStatus(status);

        return transferMapper.convertToResponse(
                transferRepository.save(transfer)
        );
    }
}
