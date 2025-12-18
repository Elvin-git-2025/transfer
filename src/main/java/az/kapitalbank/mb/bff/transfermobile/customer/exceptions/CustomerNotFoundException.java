package az.kapitalbank.mb.bff.transfermobile.customer.exceptions;

public class CustomerNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public CustomerNotFoundException(Long id) {
        super("Customer not found with id: " + id);
    }
}

