package az.kapitalbank.mb.bff.transfermobile.transfer.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

@FeignClient(
        name = "account-service",
        url = "${account.service.url}"
)
public interface AccountClient {

    @GetMapping("/api/v1/accounts/{customerId}/balance")
    BigDecimal getBalance(@PathVariable Long customerId);

    @PostMapping("/api/v1/accounts/{customerId}/debit")
    void debit(@PathVariable Long customerId,
               @RequestParam BigDecimal amount);

    @PostMapping("/api/v1/accounts/{customerId}/credit")
    void credit(@PathVariable Long customerId,
                @RequestParam BigDecimal amount);
}