package service.leads.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import service.leads.dto.request.CambiarEstadoLeadRequest;
import service.leads.dto.request.CrearLeadRequest;
import service.leads.dto.response.LeadDetalleResponse;
import service.leads.dto.response.LeadResponse;
import service.leads.entity.Estado;
import service.leads.security.ContextoUsuario;
import service.leads.security.UsuarioActual;
import service.leads.service.LeadService;

import java.util.List;
import java.util.UUID;

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

    @PutMapping("/{id}/estado")
    public ResponseEntity<Void> cambiarEstado(
            @PathVariable UUID id,
            @Valid @RequestBody CambiarEstadoLeadRequest leadRequest,
            @UsuarioActual ContextoUsuario contextoUsuario
    ){

        leadService.cambiarEstado(id, leadRequest.nuevoEstado(),contextoUsuario.rol(), contextoUsuario.userId());

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/mis-leads")
    public ResponseEntity<List<LeadResponse>> listarMisLeads(
            @UsuarioActual ContextoUsuario contexto,
            @RequestParam(required = false) Estado estado) {

        return ResponseEntity.ok(leadService.listarMisLeads(contexto.userId(), estado));
    }


    @GetMapping("/{id}")
    public ResponseEntity<LeadDetalleResponse> obtenerDetalle(
            @PathVariable UUID id,
            @UsuarioActual ContextoUsuario contexto) {

        return ResponseEntity.ok(leadService.obtenerDetalle(id, contexto.rol(), contexto.userId()));
    }


}
