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
        url = "${card.service.url}"
)
public interface CardClient {

    @GetMapping("/api/v1/card/customer/{customerId}")
    Long getCardIdByCustomerId(@PathVariable Long customerId);


    @GetMapping("/api/v1/card/{cardId}/exists")
    boolean exists(@PathVariable Long cardId);

    @GetMapping("/api/v1/card/{cardId}/balance")
    AccountBalanceResponse getBalance(@PathVariable Long cardId);

    @PostMapping("/api/v1/card/{cardId}/debit")
    void debit(
            @PathVariable Long cardId,
            @RequestBody DebitAccountRequest request
    );

    @PostMapping("/api/v1/card/{cardId}/credit")
    void credit(
            @PathVariable Long cardId,
            @RequestBody CreditAccountRequest request
    );
}