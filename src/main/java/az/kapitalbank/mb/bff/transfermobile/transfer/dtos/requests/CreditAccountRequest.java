package az.kapitalbank.mb.bff.transfermobile.transfer.dtos.requests;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreditAccountRequest {

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal amount;
}