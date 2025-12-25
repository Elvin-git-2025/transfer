package az.kapitalbank.mb.bff.transfermobile.transfer.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class AccountBalanceResponse {
    private BigDecimal balance;
}
