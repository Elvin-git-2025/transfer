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
import java.util.List;
import java.util.Optional;

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

    @Test
    void getTransferById_whenIdNotFound_throwsException() {
        Long transferId = 999L;
        when(transferRepository.findById(transferId)).thenReturn(java.util.Optional.empty());

        assertThatThrownBy(() -> transferService.getTransferById(transferId))
                .isInstanceOf(az.transfer.money.transfer.exceptions.TransferNotFoundException.class);
    }

    @Test
    void getTransferById_whenIdIsNull_throwsException() {
        Long transferId = 999L;
        when(transferRepository.findById(transferId)).thenReturn(java.util.Optional.empty());
        assertThatThrownBy(() -> transferService.getTransferById(transferId))
                .isInstanceOf(az.transfer.money.transfer.exceptions.TransferNotFoundException.class);
    }

    @Test
    void getTransferById_successful() {
        Long id = 1L;
        Transfer transfer = new Transfer();
        transfer.setId(id);
        TransferResponse expected = new TransferResponse();

        when(transferRepository.findById(id)).thenReturn(java.util.Optional.of(transfer));
        when(transferMapper.convertToResponse(transfer)).thenReturn(expected);

        TransferResponse result = transferService.getTransferById(id);

        assertThat(result).isNotNull();
        verify(transferRepository).findById(id);
    }

    @Test
    void updateTransfer_successful() {
        Long id = 1L;
        TransferStatus transferStatus = TransferStatus.PENDING;

        Transfer existingTransfer = new Transfer();
        existingTransfer.setId(id);
        existingTransfer.setStatus(transferStatus);

        TransferResponse expected = new TransferResponse();
        expected.setStatus(transferStatus);

        when(transferRepository.findById(id)).thenReturn(java.util.Optional.of(existingTransfer));
        when(transferRepository.save(any(Transfer.class))).thenReturn(existingTransfer);
        when(transferMapper.convertToResponse(any())).thenReturn(expected);

        TransferResponse response = transferService.updateTransfer(id, transferStatus);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(transferStatus);
        assertThat(existingTransfer.getStatus()).isEqualTo(transferStatus);

        verify(transferRepository).findById(id);
        verify(transferRepository).save(any(Transfer.class));
    }

    @Test
    void updateTransfer_whenNotPending_throwsException() {
        Long id = 1L;
        TransferStatus currentStatus = TransferStatus.COMPLETED;
        TransferStatus targetStatus = TransferStatus.CANCELLED;

        Transfer existingTransfer = new Transfer();
        existingTransfer.setId(id);
        existingTransfer.setStatus(currentStatus);

        when(transferRepository.findById(id)).thenReturn(Optional.of(existingTransfer));

        assertThatThrownBy(() -> transferService.updateTransfer(id, targetStatus))
                .isInstanceOf(az.transfer.money.transfer.exceptions.InvalidTransferException.class)
                .hasMessage("Only PENDING transfers can change status");

        verify(transferRepository).findById(id);
        verify(transferRepository, never()).save(any(Transfer.class));
    }

    @Test
    void cancelTransfer_successful() {
        Long id = 1L;
        Long payerId = 10L;
        BigDecimal amount = BigDecimal.valueOf(50.00);

        Transfer existingTransfer = new Transfer();
        existingTransfer.setId(id);
        existingTransfer.setPayerId(payerId);
        existingTransfer.setAmount(amount);
        existingTransfer.setStatus(TransferStatus.PENDING);

        TransferResponse expected = new TransferResponse();
        expected.setStatus(TransferStatus.CANCELLED);

        when(transferRepository.findById(id)).thenReturn(Optional.of(existingTransfer));
        when(transferRepository.save(existingTransfer)).thenReturn(existingTransfer);
        when(transferMapper.convertToResponse(existingTransfer)).thenReturn(expected);

        TransferResponse response = transferService.cancelTransfer(id);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(TransferStatus.CANCELLED);
        assertThat(existingTransfer.getStatus()).isEqualTo(TransferStatus.CANCELLED);

        verify(cardClient).credit(eq(payerId), any(CreditAccountRequest.class));
        verify(transferRepository).findById(id);
        verify(transferRepository).save(existingTransfer);
    }

    @Test
    void cancelTransfer_notPending_throwsException() {
        Long id = 1L;
        Transfer existingTransfer = new Transfer();
        existingTransfer.setId(id);
        existingTransfer.setStatus(TransferStatus.COMPLETED);

        TransferResponse expected = new TransferResponse();
        expected.setStatus(TransferStatus.COMPLETED);

        when(transferRepository.findById(id)).thenReturn(Optional.of(existingTransfer));

        assertThatThrownBy(() -> transferService.cancelTransfer(id))
                .isInstanceOf(az.transfer.money.transfer.exceptions.InvalidTransferException.class)
                .hasMessage("Only PENDING transfers can be cancelled");

        verify(transferRepository).findById(id);
        verify(cardClient, never()).credit(anyLong(), any());
        verify(transferRepository, never()).save(any());
    }

    @Test
    void getAllTransfers_successful() {
        Transfer existingTransfer = new Transfer();
        existingTransfer.setId(1L);
        Transfer existingTransfer2 = new Transfer();
        existingTransfer2.setId(2L);
        List<Transfer> transfers = List.of(existingTransfer, existingTransfer2);

        TransferResponse response1 = new TransferResponse();
        TransferResponse response2 = new TransferResponse();
        List<TransferResponse> expectedResponses = List.of(response1, response2);

        when(transferRepository.findAll()).thenReturn(transfers);

        when(transferMapper.convertToResponseList(transfers)).thenReturn(expectedResponses);

        List<TransferResponse> actualResponses = transferService.getAllTransfers();

        assertThat(actualResponses).isNotNull();
        assertThat(actualResponses.size()).isEqualTo(2);
        assertThat(actualResponses).isEqualTo(expectedResponses);

        verify(transferRepository).findAll();
        verify(transferMapper).convertToResponseList(transfers);
    }
}
