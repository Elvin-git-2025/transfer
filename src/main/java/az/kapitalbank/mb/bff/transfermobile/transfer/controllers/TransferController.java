package az.kapitalbank.mb.bff.transfermobile.transfer.controllers;

import az.kapitalbank.mb.bff.transfermobile.transfer.dtos.requests.CreateTransferRequest;
import az.kapitalbank.mb.bff.transfermobile.transfer.dtos.responses.TransferResponse;
import az.kapitalbank.mb.bff.transfermobile.transfer.enums.TransferStatus;
import az.kapitalbank.mb.bff.transfermobile.transfer.services.TransferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.List;

@RestController
@RequestMapping("/api/v1/transfer")
@RequiredArgsConstructor
public class TransferController {
    private final TransferService transferService;

    @PostMapping("/create")
    public TransferResponse createTransfer(
            @RequestBody @Valid CreateTransferRequest request) {
        return transferService.createTransfer(request);
    }

    @GetMapping("/by-id/{id}")
    public TransferResponse getTransfer(@PathVariable Long id) {
        return transferService.getTransferById(id);
    }

    @GetMapping("/customer/{payeeId}")
    public List<TransferResponse> getTransfersByCustomer(
            @PathVariable Long payeeId) {
        return transferService.getTransfersByPayeeId(payeeId);
    }

    @DeleteMapping("/cancel")
    public TransferResponse cancelTransfer(@RequestParam("id") Long id) {
        return transferService.cancelTransfer(id);
    }

    @PutMapping("/{id}/status")
    public TransferResponse updateTransfer(@PathVariable Long id,
                                           @RequestParam TransferStatus transferStatus) {
        return transferService.updateTransfer(id, transferStatus);
    }

}
