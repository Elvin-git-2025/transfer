package az.kapitalbank.mb.bff.transfermobile.transfer.services;

import az.kapitalbank.mb.bff.transfermobile.transfer.clients.AccountClient;
import az.kapitalbank.mb.bff.transfermobile.transfer.dtos.requests.CreditAccountRequest;
import az.kapitalbank.mb.bff.transfermobile.transfer.dtos.requests.DebitAccountRequest;
import az.kapitalbank.mb.bff.transfermobile.transfer.exceptions.CustomerNotFoundException;
import az.kapitalbank.mb.bff.transfermobile.transfer.clients.CustomerClient;
import az.kapitalbank.mb.bff.transfermobile.transfer.dtos.requests.CreateTransferRequest;
import az.kapitalbank.mb.bff.transfermobile.transfer.dtos.responses.TransferResponse;
import az.kapitalbank.mb.bff.transfermobile.transfer.entities.Transfer;
import az.kapitalbank.mb.bff.transfermobile.transfer.enums.TransferStatus;
import az.kapitalbank.mb.bff.transfermobile.transfer.enums.TransferType;
import az.kapitalbank.mb.bff.transfermobile.transfer.exceptions.InvalidTransferException;
import az.kapitalbank.mb.bff.transfermobile.transfer.exceptions.TransferNotFoundException;
import az.kapitalbank.mb.bff.transfermobile.transfer.mappers.TransferMapper;
import az.kapitalbank.mb.bff.transfermobile.transfer.repositories.TransferRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;


@Service
@RequiredArgsConstructor
public class TransferService {

    private final TransferRepository transferRepository;
    private final TransferMapper transferMapper;
    private final CustomerClient customerClient;
    private final AccountClient accountClient;


    @Transactional
    public TransferResponse createTransfer(CreateTransferRequest request) {

        if (request.getPayerId().equals(request.getPayeeId())) {
            throw new InvalidTransferException(
                    "Payer and payee cannot be the same"
            );
        }

        validateCustomerExists(request.getPayerId());
        validateCustomerExists(request.getPayeeId());

        BigDecimal tariff = calculateTariff(request.getType());
        BigDecimal commission =
                calculateCommission(request.getAmount(), request.getType());

        BigDecimal totalDebit =
                request.getAmount().add(tariff).add(commission);

        checkSufficientBalance(request.getPayerId(), totalDebit);

        Transfer transfer = transferMapper.convertToEntity(
                request,
                tariff,
                commission,
                TransferStatus.PENDING,
                LocalDateTime.now()
        );

        Transfer savedTransfer = transferRepository.save(transfer);

        try {
            accountClient.debit(
                    request.getPayerId(),
                    new DebitAccountRequest(totalDebit)
            );

            accountClient.credit(
                    request.getPayeeId(),
                    new CreditAccountRequest(request.getAmount())
            );

            savedTransfer.setStatus(TransferStatus.COMPLETED);

        } catch (feign.FeignException ex) {
            savedTransfer.setStatus(TransferStatus.FAILED);
            transferRepository.save(savedTransfer);

            throw new InvalidTransferException(
                    "Transfer failed due to account service error"
            );
        }

        return transferMapper.convertToResponse(
                transferRepository.save(savedTransfer)
        );
    }



    private void checkSufficientBalance(Long payerId, BigDecimal totalDebit) {

        if (totalDebit == null || totalDebit.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTransferException(
                    "Total debit amount must be greater than zero"
            );
        }

        BigDecimal balance;
        try {
            balance = accountClient
                    .getBalance(payerId)
                    .getBalance();
        } catch (feign.FeignException ex) {
            throw new InvalidTransferException(
                    "Account service unavailable"
            );
        }

        if (balance == null) {
            throw new InvalidTransferException(
                    "Account not found or balance unavailable"
            );
        }

        if (balance.compareTo(totalDebit) < 0) {
            throw new InvalidTransferException(
                    "Insufficient balance"
            );
        }
    }



    private void validateCustomerExists(Long id) {

        if (id == null || id <= 0) {
            throw new InvalidTransferException(
                    "Customer id must be positive"
            );
        }

        try {
            boolean exists = customerClient.existsById(id);
            if (!exists) {
                throw new CustomerNotFoundException(id);
            }
        } catch (feign.FeignException.NotFound ex) {
            throw new CustomerNotFoundException(id);
        } catch (feign.FeignException ex) {
            throw new InvalidTransferException(
                    "Customer service unavailable"
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
        BigDecimal rate = switch (type) {
            case CARD_TO_CARD -> new BigDecimal("0.02");
            case ACCOUNT_TO_CARD -> new BigDecimal("0.01");
        };

        return amount.multiply(rate);
    }

    public TransferResponse getTransferById(Long id) {

        if (id == null || id <= 0) {
            throw new InvalidTransferException(
                    "Transfer id must be positive"
            );
        }

        Transfer transfer =
                transferRepository.findById(id)
                        .orElseThrow(
                                () -> new TransferNotFoundException(id)
                        );

        return transferMapper.convertToResponse(transfer);
    }

    public List<TransferResponse> getTransfersByPayeeId(Long payeeId) {

        if (payeeId == null || payeeId <= 0) {
            throw new InvalidTransferException(
                    "Customer id must be positive"
            );
        }

        List<Transfer> transfers =
                transferRepository.findAllByPayeeId(payeeId);

        return transferMapper.convertToResponseList(transfers);
    }

    public List<TransferResponse> getAllTransfers() {
      return transferMapper.convertToResponseList(transferRepository.findAll());
    }

    @Transactional
    public TransferResponse cancelTransfer(Long id) {

        if (id == null || id <= 0) {
            throw new InvalidTransferException(
                    "Transfer id must be positive"
            );
        }

        Transfer transfer =
                transferRepository.findById(id)
                        .orElseThrow(
                                () -> new TransferNotFoundException(id)
                        );

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

    public TransferResponse updateTransfer(Long id, TransferStatus status) {

        if (id == null || id <= 0) {
            throw new InvalidTransferException(
                    "Transfer id must be positive"
            );
        }

        if (status == null) {
            throw new InvalidTransferException(
                    "Transfer status must not be null"
            );
        }

        Transfer transfer =
                transferRepository.findById(id)
                        .orElseThrow(
                                () -> new TransferNotFoundException(id)
                        );

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
