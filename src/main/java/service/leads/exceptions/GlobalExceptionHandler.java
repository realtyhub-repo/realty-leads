package service.leads.exceptions;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import service.leads.dto.internal.ErrorResponse;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler(ContextoUsuarioInvalidoException.class)
    public ResponseEntity<ErrorResponse> handleContextoUsuarioInvalidoException(ContextoUsuarioInvalidoException ex){
        return construirRespuesta(HttpStatus.UNAUTHORIZED, "CONTEXTO_INVALIDO", ex.getMessage());
    }

    @ExceptionHandler(AccesoNoAutorizadoException.class)
    public ResponseEntity<ErrorResponse> handleAccesoNoAutorizadoException(AccesoNoAutorizadoException ex){
        return construirRespuesta(HttpStatus.FORBIDDEN, "ACCESO_NO_AUTORIZADO", ex.getMessage());
    }

    @ExceptionHandler(HttpClientErrorException.class)
    public ResponseEntity<ErrorResponse> handleHttpClientErrorException(HttpClientErrorException ex){
        return construirRespuesta(HttpStatus.BAD_REQUEST, "ERROR_CLIENTE_HTTP", ex.getMessage());
    }

    @ExceptionHandler(PropiedadNoEncontradaException.class)
    public ResponseEntity<ErrorResponse> handlePropiedadNoEncontradaException(PropiedadNoEncontradaException ex){
        return construirRespuesta(HttpStatus.NOT_FOUND, "PROPIEDAD_NO_ENCONTRADA", ex.getMessage());
    }

    // Ya no hay "lead huérfano": si nadie está libre a esa hora, no se crea el lead.
    @ExceptionHandler(SinAgentesDisponiblesException.class)
    public ResponseEntity<ErrorResponse> handleSinAgentesDisponiblesException(SinAgentesDisponiblesException ex){
        return construirRespuesta(HttpStatus.CONFLICT, "SIN_AGENTES_DISPONIBLES", ex.getMessage());
    }

    @ExceptionHandler(LeadNoEncontradoException.class)
    public ResponseEntity<ErrorResponse> handleLeadNoEncontradoException( LeadNoEncontradoException  ex){
        return construirRespuesta(HttpStatus.NOT_FOUND, "LEAD_NO_ENCONTRADO", ex.getMessage());
    }

    @ExceptionHandler(EstadoTerminalException.class)
    public ResponseEntity<ErrorResponse> handleEstadoTerminalException(EstadoTerminalException ex){
        return construirRespuesta(HttpStatus.CONFLICT, "ESTADO_TERMINAL", ex.getMessage());
    }

    @ExceptionHandler(LeadDuplicadoException.class)
    public ResponseEntity<ErrorResponse> handleLeadDuplicadoException(LeadDuplicadoException ex){
        return construirRespuesta(HttpStatus.CONFLICT, "LEAD_DUPLICADO", ex.getMessage());
    }

    // --- Errores que vienen de Visitas ---

    @ExceptionHandler(AgenteOcupadoException.class)
    public ResponseEntity<ErrorResponse> handleAgenteOcupadoException(AgenteOcupadoException ex){
        return construirRespuesta(HttpStatus.CONFLICT, "AGENTE_OCUPADO", ex.getMessage());
    }

    @ExceptionHandler(PropiedadOcupadaException.class)
    public ResponseEntity<ErrorResponse> handlePropiedadOcupadaException(PropiedadOcupadaException ex){
        return construirRespuesta(HttpStatus.CONFLICT, "PROPIEDAD_OCUPADA", ex.getMessage());
    }

    @ExceptionHandler(VisitaYaExisteException.class)
    public ResponseEntity<ErrorResponse> handleVisitaYaExisteException(VisitaYaExisteException ex){
        return construirRespuesta(HttpStatus.CONFLICT, "VISITA_YA_EXISTE", ex.getMessage());
    }

    @ExceptionHandler(FechaVisitaInvalidaException.class)
    public ResponseEntity<ErrorResponse> handleFechaVisitaInvalidaException(FechaVisitaInvalidaException ex){
        return construirRespuesta(HttpStatus.BAD_REQUEST, "FECHA_VISITA_INVALIDA", ex.getMessage());
    }

    @ExceptionHandler(VisitasNoDisponibleException.class)
    public ResponseEntity<ErrorResponse> handleVisitasNoDisponibleException(VisitasNoDisponibleException ex){
        return construirRespuesta(HttpStatus.SERVICE_UNAVAILABLE, "VISITAS_NO_DISPONIBLE", ex.getMessage());
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidacion(MethodArgumentNotValidException ex) {
        String mensaje = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .orElse("Solicitud inválida");
        return construirRespuesta(HttpStatus.BAD_REQUEST, "SOLICITUD_INVALIDA", mensaje);
    }

    @ExceptionHandler({
            MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class,
            HttpMessageNotReadableException.class
    })
    public ResponseEntity<ErrorResponse> handleSolicitudMalFormada(Exception ex) {
        return construirRespuesta(HttpStatus.BAD_REQUEST, "SOLICITUD_INVALIDA",
                "La solicitud tiene parámetros faltantes o con formato inválido");
    }

    private ResponseEntity<ErrorResponse> construirRespuesta(HttpStatus status, String codigo, String mensaje){
        ErrorResponse error = new ErrorResponse(codigo, mensaje, status.value(), LocalDateTime.now());
        return ResponseEntity.status(status).body(error);

    }

}
