package service.leads.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class EstadoTerminalException extends RuntimeException {
    public EstadoTerminalException(String message) {
        super(message);
    }
}
