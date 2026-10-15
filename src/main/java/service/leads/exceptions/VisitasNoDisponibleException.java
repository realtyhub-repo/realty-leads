package service.leads.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
public class VisitasNoDisponibleException extends RuntimeException {
    public VisitasNoDisponibleException(String message) {
        super(message);
    }
}
