package az.kapitalbank.mb.bff.transfermobile.transfer.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;


@FeignClient(
        name = "customer-service",
        url = "${customer.service.url}"
)
public interface CustomerClient {

    @PostMapping("/accounts/{id}/debit")
    void debit(@PathVariable Long id,
               @RequestParam BigDecimal amount);

    @GetMapping("/api/v1/customers/{id}/exists")
    boolean existsById(@PathVariable Long id);
}
