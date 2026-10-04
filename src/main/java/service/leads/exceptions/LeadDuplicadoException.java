package service.leads.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class LeadDuplicadoException extends RuntimeException {
    public LeadDuplicadoException(String message) {
        super(message);
    }
}
