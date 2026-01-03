package az.kapitalbank.mb.bff.transfermobile.transfer.mappers;

import az.kapitalbank.mb.bff.transfermobile.transfer.dtos.requests.CreateTransferRequest;
import az.kapitalbank.mb.bff.transfermobile.transfer.dtos.responses.TransferResponse;
import az.kapitalbank.mb.bff.transfermobile.transfer.entities.Transfer;
import az.kapitalbank.mb.bff.transfermobile.transfer.enums.TransferStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;


@Mapper(componentModel = "spring",
        imports = {LocalDateTime.class}
)
public interface TransferMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", source = "status")
    @Mapping(target = "tariff", source = "tariff")
    @Mapping(target = "commission", source = "commission")
    @Mapping(target = "totalAmount", source = "totalDebit")
    @Mapping(target = "createdAt", expression = "java(LocalDateTime.now())")
    Transfer convertToEntity(
            CreateTransferRequest request,
            BigDecimal tariff,
            BigDecimal commission,
            BigDecimal totalDebit,
            TransferStatus status
    );

    TransferResponse convertToResponse(Transfer transfer);

    List<TransferResponse> convertToResponseList(List<Transfer> transfers);
}
