package service.leads.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class AgenteOcupadoException extends RuntimeException {
    public AgenteOcupadoException(String message) {
        super(message);
    }
}
