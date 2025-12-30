package az.kapitalbank.mb.bff.transfermobile.transfer.clients;

import az.kapitalbank.mb.bff.transfermobile.transfer.dtos.requests.CreditAccountRequest;
import az.kapitalbank.mb.bff.transfermobile.transfer.dtos.requests.DebitAccountRequest;
import az.kapitalbank.mb.bff.transfermobile.transfer.dtos.responses.AccountBalanceResponse;
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

    @GetMapping("/api/v1/cards/{cardId}/exists")
    boolean exists(@PathVariable Long cardId);

    @GetMapping("/api/v1/cards/{cardId}/balance")
    AccountBalanceResponse getBalance(@PathVariable Long cardId);

    @PostMapping("/api/v1/cards/{cardId}/debit")
    void debit(
            @PathVariable Long cardId,
            @RequestBody DebitAccountRequest request
    );

    @PostMapping("/api/v1/cards/{cardId}/credit")
    void credit(
            @PathVariable Long cardId,
            @RequestBody CreditAccountRequest request
    );
}