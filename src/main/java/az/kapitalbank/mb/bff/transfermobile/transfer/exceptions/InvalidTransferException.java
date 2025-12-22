package az.kapitalbank.mb.bff.transfermobile.transfer.exceptions;

import java.io.Serial;

public class InvalidTransferException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public InvalidTransferException(String message) {
        super(message);
    }
}
