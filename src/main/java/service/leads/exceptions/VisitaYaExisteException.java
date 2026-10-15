package service.leads.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class VisitaYaExisteException extends RuntimeException {
    public VisitaYaExisteException(String message) {
        super(message);
    }
}
