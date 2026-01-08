package az.transfer.money.util.service;

import az.transfer.money.transfer.clients.AccountClient;
import az.transfer.money.transfer.clients.CardClient;
import az.transfer.money.transfer.clients.CustomerClient;
import az.transfer.money.transfer.dtos.requests.CreateTransferRequest;
import az.transfer.money.transfer.dtos.requests.CreditAccountRequest;
import az.transfer.money.transfer.dtos.requests.DebitAccountRequest;
import az.transfer.money.transfer.dtos.responses.AccountBalanceResponse;
import az.transfer.money.transfer.dtos.responses.TransferResponse;
import az.transfer.money.transfer.entities.Transfer;
import az.transfer.money.transfer.enums.TransferStatus;
import az.transfer.money.transfer.enums.TransferType;
import az.transfer.money.transfer.mappers.TransferMapper;
import az.transfer.money.transfer.repositories.TransferRepository;
import az.transfer.money.transfer.services.TransferService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TransferServiceTest {

    @Mock
    private TransferRepository transferRepository;

    @Mock
    private TransferMapper transferMapper;

    @Mock
    private CustomerClient customerClient;

    @Mock
    private AccountClient accountClient;

    @Mock
    private CardClient cardClient;

    @InjectMocks
    private TransferService transferService;


    @Test
    void createTransfer_cardToCard_successful() {

        Long payerId = 1L;
        Long payeeId = 2L;
        BigDecimal amount = BigDecimal.valueOf(100);

        CreateTransferRequest request = new CreateTransferRequest();
        request.setPayerId(payerId);
        request.setPayeeId(payeeId);
        request.setAmount(amount);
        request.setType(TransferType.CARD_TO_CARD);

        Transfer transferEntity = new Transfer();
        transferEntity.setPayerId(payerId);
        transferEntity.setPayeeId(payeeId);
        transferEntity.setAmount(amount);
        transferEntity.setStatus(TransferStatus.PENDING);

        Transfer savedTransfer = new Transfer();
        savedTransfer.setPayerId(payerId);
        savedTransfer.setPayeeId(payeeId);
        savedTransfer.setAmount(amount);
        savedTransfer.setStatus(TransferStatus.COMPLETED);

        TransferResponse transferResponse = new TransferResponse();
        transferResponse.setStatus(TransferStatus.COMPLETED);
        AccountBalanceResponse balance = new AccountBalanceResponse();
        balance.setBalance(BigDecimal.valueOf(1000));

        when(cardClient.getBalance(anyLong())).thenReturn(balance);
        when(cardClient.exists(anyLong())).thenReturn(true);
        when(transferMapper.convertToEntity(any(), any(), any(), any(), any())).thenReturn(transferEntity);
        when(transferRepository.save(transferEntity)).thenReturn(savedTransfer);
        when(transferMapper.convertToResponse(any())).thenReturn(transferResponse);


        TransferResponse response = transferService.createTransfer(request);

        assertThat(response.getStatus()).isEqualTo(TransferStatus.COMPLETED);

        verify(cardClient).debit(eq(payerId), any(DebitAccountRequest.class));
        verify(cardClient).credit(eq(payeeId), any(CreditAccountRequest.class));
        verify(cardClient, atLeastOnce()).getBalance(anyLong());
        verify(cardClient, times(2)).exists(anyLong());
        verify(transferRepository, times(2)).save(any(Transfer.class));
    }

    @Test
    void createTransfer_cardToCard_whenExecutionFails_returnsFailedStatus() {

        Long payerId = 1L;
        Long payeeId = 2L;
        BigDecimal amount = BigDecimal.valueOf(100);

        CreateTransferRequest request = new CreateTransferRequest();
        request.setPayerId(payerId);
        request.setPayeeId(payeeId);
        request.setAmount(amount);
        request.setType(TransferType.CARD_TO_CARD);

        Transfer transferEntity = new Transfer();
        transferEntity.setPayerId(payerId);
        transferEntity.setPayeeId(payeeId);
        transferEntity.setAmount(amount);
        transferEntity.setStatus(TransferStatus.PENDING);

        Transfer savedTransfer = new Transfer();
        savedTransfer.setStatus(TransferStatus.FAILED);

        TransferResponse transferResponse = new TransferResponse();
        transferResponse.setStatus(TransferStatus.FAILED);

        AccountBalanceResponse balance = new AccountBalanceResponse();
        balance.setBalance(BigDecimal.valueOf(1000));

        when(cardClient.exists(anyLong())).thenReturn(true);
        when(cardClient.getBalance(anyLong())).thenReturn(balance);
        when(transferMapper.convertToEntity(any(), any(), any(), any(), any()))
                .thenReturn(transferEntity);
        when(transferRepository.save(any(Transfer.class)))
                .thenReturn(savedTransfer);
        when(transferMapper.convertToResponse(any()))
                .thenReturn(transferResponse);

        TransferResponse response = transferService.createTransfer(request);

        assertThat(response.getStatus()).isEqualTo(TransferStatus.FAILED);

        verify(cardClient, times(2)).exists(anyLong());
        verify(cardClient).getBalance(anyLong());
        verify(transferRepository, atLeastOnce()).save(any(Transfer.class));
    }


    @Test
    void createTransfer_accountToCard_successful() {

        Long payerId = 1L;
        Long payeeId = 2L;
        BigDecimal amount = BigDecimal.valueOf(100);

        CreateTransferRequest request = new CreateTransferRequest();
        request.setPayerId(payerId);
        request.setPayeeId(payeeId);
        request.setAmount(amount);
        request.setType(TransferType.ACCOUNT_TO_CARD);

        Transfer transferEntity = new Transfer();
        transferEntity.setStatus(TransferStatus.PENDING);

        Transfer savedTransfer = new Transfer();
        savedTransfer.setStatus(TransferStatus.COMPLETED);

        TransferResponse transferResponse = new TransferResponse();
        transferResponse.setStatus(TransferStatus.COMPLETED);

        AccountBalanceResponse balance = new AccountBalanceResponse();
        balance.setBalance(BigDecimal.valueOf(1000));

        when(accountClient.getBalance(payerId)).thenReturn(balance);
        when(customerClient.existsById(payerId)).thenReturn(true);
        when(cardClient.getCardIdByCustomerId(payeeId)).thenReturn(99L);

        when(transferMapper.convertToEntity(any(), any(), any(), any(), any()))
                .thenReturn(transferEntity);
        when(transferRepository.save(any(Transfer.class)))
                .thenReturn(savedTransfer);
        when(transferMapper.convertToResponse(any()))
                .thenReturn(transferResponse);

        TransferResponse response = transferService.createTransfer(request);

        assertThat(response.getStatus()).isEqualTo(TransferStatus.COMPLETED);

        verify(accountClient).getBalance(payerId);
        verify(customerClient).existsById(payerId);
        verify(cardClient).getCardIdByCustomerId(payeeId);
        verify(accountClient).debit(eq(payerId), any());
        verify(cardClient).credit(eq(99L), any());
        verify(transferRepository, times(2)).save(any(Transfer.class));
    }

    @Test
    void createTransfer_accountToCard_whenExecutionFails_returnsFailedStatus() {
        Long payerId = 1L;
        Long payeeId = 2L;
        BigDecimal amount = BigDecimal.valueOf(100);

        CreateTransferRequest request = new CreateTransferRequest();
        request.setPayerId(payerId);
        request.setPayeeId(payeeId);
        request.setAmount(amount);
        request.setType(TransferType.ACCOUNT_TO_CARD);

        Transfer transferEntity = new Transfer();
        transferEntity.setStatus(TransferStatus.PENDING);

        Transfer savedTransfer = new Transfer();
        savedTransfer.setStatus(TransferStatus.FAILED);

        TransferResponse transferResponse = new TransferResponse();
        transferResponse.setStatus(TransferStatus.FAILED);

        AccountBalanceResponse balance = new AccountBalanceResponse();
        balance.setBalance(BigDecimal.valueOf(1000));

        when(accountClient.getBalance(payerId)).thenReturn(balance);
        when(customerClient.existsById(payerId)).thenReturn(true);
        when(cardClient.getCardIdByCustomerId(payeeId)).thenReturn(99L);
        when(transferMapper.convertToEntity(any(), any(), any(), any(), any()))
        .thenReturn(transferEntity);
        when(transferRepository.save(any(Transfer.class)))
        .thenReturn(savedTransfer);
        when(transferMapper.convertToResponse(any()))
        .thenReturn(transferResponse);

        TransferResponse response = transferService.createTransfer(request);

        assertThat(response.getStatus()).isEqualTo(TransferStatus.FAILED);

        verify(accountClient).getBalance(payerId);
        verify(customerClient).existsById(payerId);
        verify(cardClient).getCardIdByCustomerId(payeeId);
        verify(accountClient).debit(eq(payerId), any());
        verify(cardClient).credit(eq(99L), any());
        verify(transferRepository, times(2)).save(any(Transfer.class));
    }

    @Test
    void createTransfer_successful() {
        Long payerId = 1L;
        Long payeeId = 2L;
        BigDecimal amount = BigDecimal.valueOf(100);

        CreateTransferRequest request = new CreateTransferRequest();
        request.setPayerId(payerId);
        request.setPayeeId(payeeId);
        request.setAmount(amount);
        request.setType(TransferType.ACCOUNT_TO_CARD);

        AccountBalanceResponse balance = new AccountBalanceResponse();
        balance.setBalance(BigDecimal.valueOf(1000));

        Transfer transferEntity = new Transfer();
        transferEntity.setStatus(TransferStatus.PENDING);

        Transfer savedTransfer = new Transfer();
        savedTransfer.setStatus(TransferStatus.COMPLETED);

        TransferResponse transferResponse = new TransferResponse();
        transferResponse.setStatus(TransferStatus.COMPLETED);

        when(customerClient.existsById(payerId)).thenReturn(true);
        when(accountClient.getBalance(payerId)).thenReturn(balance);
        when(cardClient.getCardIdByCustomerId(payeeId)).thenReturn(99L);

        when(transferMapper.convertToEntity(eq(request), any(), any(), any(), any()))
                .thenReturn(transferEntity);
        when(transferRepository.save(any(Transfer.class)))
                .thenReturn(savedTransfer);
        when(transferMapper.convertToResponse(any()))
                .thenReturn(transferResponse);

        TransferResponse response = transferService.createTransfer(request);

        assertThat(response.getStatus()).isEqualTo(TransferStatus.COMPLETED);

        verify(transferRepository, times(2)).save(any(Transfer.class));
    }


}
