package az.kapitalbank.mb.bff.transfermobile.transfer.clients;

import az.kapitalbank.mb.bff.transfermobile.transfer.dtos.requests.CreditAccountRequest;
import az.kapitalbank.mb.bff.transfermobile.transfer.dtos.requests.DebitAccountRequest;
import az.kapitalbank.mb.bff.transfermobile.transfer.dtos.responses.AccountBalanceResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;


@FeignClient(
        name = "account-service",
        url = "${account.service.url}"
)
public interface AccountClient {

    @GetMapping("/api/v1/accounts/{customerId}/balance")
    AccountBalanceResponse getBalance(
            @PathVariable Long customerId
    );

    @PostMapping("/api/v1/accounts/{customerId}/debit")
    void debit(
            @PathVariable Long customerId,
            @RequestBody DebitAccountRequest request
    );

    @PostMapping("/api/v1/accounts/{customerId}/credit")
    default void credit(
            @PathVariable Long customerId,
            @RequestBody CreditAccountRequest request
    ) {


    }
}

