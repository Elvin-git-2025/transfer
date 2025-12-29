package az.kapitalbank.mb.bff.transfermobile.transfer.mappers;

import az.kapitalbank.mb.bff.transfermobile.transfer.dtos.requests.CreateTransferRequest;
import az.kapitalbank.mb.bff.transfermobile.transfer.dtos.responses.TransferResponse;
import az.kapitalbank.mb.bff.transfermobile.transfer.entities.Transfer;
import org.mapstruct.Mapper;
import java.util.List;

@Mapper(componentModel = "spring")
public interface TransferMapper {

    Transfer convertToEntity(CreateTransferRequest request);

    TransferResponse convertToResponse(Transfer transfer);

    List<TransferResponse> convertToResponseList(List<Transfer> transfers);
}
