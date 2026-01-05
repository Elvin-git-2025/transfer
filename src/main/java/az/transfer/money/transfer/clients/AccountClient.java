package az.transfer.money.transfer.clients;

import az.transfer.money.transfer.dtos.requests.CreditAccountRequest;
import az.transfer.money.transfer.dtos.requests.DebitAccountRequest;
import az.transfer.money.transfer.dtos.responses.AccountBalanceResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;


@FeignClient(
        name = "account-service",
        url = "${account.service.url}",
        path = "/api/v1/accounts"
)
public interface AccountClient {

    @GetMapping("/{customerId}/balance")
    AccountBalanceResponse getBalance(
            @PathVariable("customerId") Long customerId
    );

    @PostMapping("/{customerId}/debit")
    void debit(
            @PathVariable("customerId") Long customerId,
            @RequestBody DebitAccountRequest request
    );

    @PostMapping("/{customerId}/credit")
    void credit(
            @PathVariable("customerId") Long customerId,
            @RequestBody CreditAccountRequest request
    );
}


