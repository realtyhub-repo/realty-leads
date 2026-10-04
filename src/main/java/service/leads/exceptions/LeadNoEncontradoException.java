package service.leads.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class LeadNoEncontradoException extends RuntimeException {
    public LeadNoEncontradoException(String message) {
        super(message);
    }
}
