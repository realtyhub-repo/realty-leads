package service.leads.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class PropiedadOcupadaException extends RuntimeException {
    public PropiedadOcupadaException(String message) {
        super(message);
    }
}
