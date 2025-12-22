package az.kapitalbank.mb.bff.transfermobile.transfer.mappers;

import az.kapitalbank.mb.bff.transfermobile.transfer.dtos.requests.CreateTransferRequest;
import az.kapitalbank.mb.bff.transfermobile.transfer.dtos.responses.TransferResponse;
import az.kapitalbank.mb.bff.transfermobile.transfer.entities.Transfer;
import az.kapitalbank.mb.bff.transfermobile.transfer.enums.TransferStatus;
import org.mapstruct.Mapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Mapper(componentModel = "spring")
public interface TransferMapper {

    TransferResponse convertToResponse(Transfer transfer);

    Transfer convertToEntity(CreateTransferRequest request,
                             BigDecimal tariff,
                             BigDecimal commission,
                             TransferStatus status,
                             LocalDateTime createdAt);

    List<TransferResponse> convertToResponseList(List<Transfer> transfers);
}
