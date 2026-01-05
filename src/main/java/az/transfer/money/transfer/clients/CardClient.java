package az.transfer.money.transfer.clients;

import az.transfer.money.transfer.dtos.requests.CreditAccountRequest;
import az.transfer.money.transfer.dtos.requests.DebitAccountRequest;
import az.transfer.money.transfer.dtos.responses.AccountBalanceResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "card-service",
        url = "${card.service.url}",
        path = "/api/v1/card"
)
public interface CardClient {

    @GetMapping("/customer/{customerId}")
    Long getCardIdByCustomerId(@PathVariable Long customerId);


    @GetMapping("/{cardId}/exists")
    boolean exists(@PathVariable Long cardId);

    @GetMapping("/{cardId}/balance")
    AccountBalanceResponse getBalance(@PathVariable Long cardId);

    @PostMapping("/{cardId}/debit")
    void debit(
            @PathVariable Long cardId,
            @RequestBody DebitAccountRequest request
    );

    @PostMapping("/{cardId}/credit")
    void credit(
            @PathVariable Long cardId,
            @RequestBody CreditAccountRequest request
    );
}