package az.transfer.money.transfer.exceptions;

import java.io.Serial;

public class TransferNotFoundException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public TransferNotFoundException(Long id) {
        super("Could not find transfer with id :" + id);
    }
}
