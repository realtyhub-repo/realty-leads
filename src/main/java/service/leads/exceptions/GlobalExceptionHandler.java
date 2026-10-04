package service.leads.exceptions;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import service.leads.dto.internal.ErrorResponse;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler(ContextoUsuarioInvalidoException.class)
    public ResponseEntity<ErrorResponse> handleContextoUsuarioInvalidoException(ContextoUsuarioInvalidoException ex){
        return construirRespuesta(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler(AccesoNoAutorizadoException.class)
    public ResponseEntity<ErrorResponse> handleAccesoNoAutorizadoException(AccesoNoAutorizadoException ex){
        return construirRespuesta(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    @ExceptionHandler(HttpClientErrorException.class)
    public ResponseEntity<ErrorResponse> handleHttpClientErrorException(HttpClientErrorException ex){
        return construirRespuesta(HttpStatus.BAD_REQUEST,ex.getMessage());
    }

    @ExceptionHandler(PropiedadNoEncontradaException.class)
    public ResponseEntity<ErrorResponse> handlePropiedadNoEncontradaException(PropiedadNoEncontradaException ex){
        return construirRespuesta(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(SinAgentesDisponiblesException.class)
    public ResponseEntity<ErrorResponse> handleSinAgentesDisponiblesException(SinAgentesDisponiblesException ex){
        return construirRespuesta(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(LeadNoEncontradoException.class)
    public ResponseEntity<ErrorResponse> handleLeadNoEncontradoException( LeadNoEncontradoException  ex){
        return construirRespuesta(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(EstadoTerminalException.class)
    public ResponseEntity<ErrorResponse> handleEstadoTerminalException(EstadoTerminalException ex){
        return construirRespuesta(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(LeadDuplicadoException.class)
    public ResponseEntity<ErrorResponse> handleLeadDuplicadoException(LeadDuplicadoException ex){
        return construirRespuesta(HttpStatus.CONFLICT, ex.getMessage());
    }

    private ResponseEntity<ErrorResponse> construirRespuesta(HttpStatus status, String mensaje){
        ErrorResponse error = new ErrorResponse(mensaje, status.value(), LocalDateTime.now());
        return ResponseEntity.status(status).body(error);

    }

}
