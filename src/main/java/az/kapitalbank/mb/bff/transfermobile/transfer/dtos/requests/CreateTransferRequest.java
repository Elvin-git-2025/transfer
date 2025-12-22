package az.kapitalbank.mb.bff.transfermobile.transfer.dtos.requests;

import az.kapitalbank.mb.bff.transfermobile.transfer.enums.TransferType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateTransferRequest {

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    BigDecimal amount;

    @NotNull(message = "Payee id is required")
    Long payeeId;

    @NotNull(message = "Payer id is required")
    Long payerId;

    @NotNull(message = "Transfer type is required")
    TransferType type;

    @NotBlank(message = "Payee is required")
    String payee;
}
