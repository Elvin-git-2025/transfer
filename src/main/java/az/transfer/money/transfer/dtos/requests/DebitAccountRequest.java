package az.transfer.money.transfer.dtos.requests;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DebitAccountRequest {

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal amount;
}
