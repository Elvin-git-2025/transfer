package az.kapitalbank.mb.bff.transfermobile.transfer.globalExceptionHandler;

import az.kapitalbank.mb.bff.transfermobile.transfer.exceptions.InvalidTransferException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class InvalidExceptionHandler {
    @ExceptionHandler(InvalidTransferException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public Map<String, String> handleInvalidTransfer(
            InvalidTransferException ex) {
        return Map.of("error", ex.getMessage());
    }
}
