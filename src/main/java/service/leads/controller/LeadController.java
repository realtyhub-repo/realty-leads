package service.leads.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import service.leads.dto.request.CrearLeadRequest;
import service.leads.dto.response.LeadResponse;
import service.leads.security.ContextoUsuario;
import service.leads.security.UsuarioActual;
import service.leads.service.LeadService;

@RestController
@RequestMapping("/leads")
@RequiredArgsConstructor
public class LeadController {

    private final LeadService leadService;


    @PostMapping
    public ResponseEntity<LeadResponse> crear(
            @UsuarioActual ContextoUsuario contexto,
            @Valid @RequestBody CrearLeadRequest request) {

        LeadResponse response = leadService.crear(contexto.userId(), contexto.rol(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

}
