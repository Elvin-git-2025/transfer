package az.transfer.money.transfer.exceptions;

import java.io.Serial;

public class CustomerNotFoundException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public CustomerNotFoundException(Long id) {
        super("Customer not found with id: " + id);
    }
}

