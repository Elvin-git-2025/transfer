package az.kapitalbank.mb.bff.transfermobile.transfer.entities;

import az.kapitalbank.mb.bff.transfermobile.customer.entities.Customer;
import az.kapitalbank.mb.bff.transfermobile.transfer.enums.Type;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Entity
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Transfer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    BigDecimal amount;
    @ManyToOne
    @JoinColumn(name = "customer_id")
    Customer customer;
    @Enumerated(EnumType.STRING)
    Type type;
    BigDecimal tariff;
    BigDecimal commission;
    String payee;
}
