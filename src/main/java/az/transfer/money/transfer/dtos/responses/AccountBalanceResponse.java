package az.transfer.money.transfer.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class AccountBalanceResponse {
    private BigDecimal balance;
}
