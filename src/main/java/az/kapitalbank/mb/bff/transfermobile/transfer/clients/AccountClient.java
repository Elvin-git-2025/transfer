package az.kapitalbank.mb.bff.transfermobile.transfer.clients;

import az.kapitalbank.mb.bff.transfermobile.transfer.dtos.requests.CreditAccountRequest;
import az.kapitalbank.mb.bff.transfermobile.transfer.dtos.requests.DebitAccountRequest;
import az.kapitalbank.mb.bff.transfermobile.transfer.dtos.responses.AccountBalanceResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;


@FeignClient(
        name = "account-service",
        url = "${account.service.url}"
)
public interface AccountClient {

    @GetMapping("/api/v1/accounts/{customerId}/balance")
    AccountBalanceResponse getBalance(
            @PathVariable("customerId") Long customerId
    );

    @PostMapping("/api/v1/accounts/{customerId}/debit")
    void debit(
            @PathVariable("customerId") Long customerId,
            @RequestBody DebitAccountRequest request
    );

    @PostMapping("/api/v1/accounts/{customerId}/credit")
    void credit(
            @PathVariable("customerId") Long customerId,
            @RequestBody CreditAccountRequest request
    );
}


