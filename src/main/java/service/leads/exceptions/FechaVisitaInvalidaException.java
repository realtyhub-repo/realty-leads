package service.leads.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class FechaVisitaInvalidaException extends RuntimeException {
    public FechaVisitaInvalidaException(String message) {
        super(message);
    }
}
