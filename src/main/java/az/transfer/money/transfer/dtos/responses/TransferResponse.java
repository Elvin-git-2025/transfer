package az.transfer.money.transfer.dtos.responses;

import az.transfer.money.transfer.enums.TransferStatus;
import az.transfer.money.transfer.enums.TransferType;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TransferResponse {
    Long id;
    BigDecimal amount;
    BigDecimal commission;
    BigDecimal tariff;
    BigDecimal totalAmount;
    TransferType type;
    String payee;
    Long payeeId;
    LocalDateTime createdAt;
    TransferStatus status;
}
