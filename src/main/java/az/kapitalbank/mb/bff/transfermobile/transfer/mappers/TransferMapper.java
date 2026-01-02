package az.kapitalbank.mb.bff.transfermobile.transfer.mappers;

import az.kapitalbank.mb.bff.transfermobile.transfer.dtos.requests.CreateTransferRequest;
import az.kapitalbank.mb.bff.transfermobile.transfer.dtos.responses.TransferResponse;
import az.kapitalbank.mb.bff.transfermobile.transfer.entities.Transfer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import java.util.List;


@Mapper(componentModel = "spring")
public interface TransferMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", constant = "PENDING")
    @Mapping(target = "createdAt", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "tariff", ignore = true)       // will set in service
    @Mapping(target = "commission", ignore = true)   // will set in service
    @Mapping(target = "totalAmount", ignore = true)  // will set in service
    Transfer convertToEntity(CreateTransferRequest request);

    TransferResponse convertToResponse(Transfer transfer);

    List<TransferResponse> convertToResponseList(List<Transfer> transfers);
}
