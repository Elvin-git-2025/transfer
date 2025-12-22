package az.kapitalbank.mb.bff.transfermobile.transfer.globalExceptionHandler;

import az.kapitalbank.mb.bff.transfermobile.transfer.exceptions.InvalidTransferException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class InvalidExceptionHandler {
    @ExceptionHandler(InvalidTransferException.class)
    public ResponseEntity<String> handleInvalidTransferException(InvalidTransferException ex) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(ex.getMessage());
    }
}
