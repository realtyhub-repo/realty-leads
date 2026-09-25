package service.leads.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import service.leads.dto.response.AgenteInternalResponse;
import service.leads.dto.response.OficinaResponse;
import service.leads.dto.response.PropiedadCoordenadasResponse;
import service.leads.entity.DistributionRules;
import service.leads.repository.DistributionRulesRepository;
import service.leads.service.AgenteClientService;
import service.leads.service.PropiedadClientService;
import service.leads.service.UsuarioClientService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/prueba")
@RequiredArgsConstructor
public class Prueba {

    private final UsuarioClientService usuarioClientService;
    private final AgenteClientService agenteClientService;
    private final PropiedadClientService propiedadClientService;
    private final DistributionRulesRepository distributionRulesRepository;

    @GetMapping("/1")
    public ResponseEntity<List<OficinaResponse>> listar(){
        return ResponseEntity.ok(usuarioClientService.listarOficinas());
    }

    @GetMapping("/2")
    public ResponseEntity<List<AgenteInternalResponse>> listarAgentes(){

        List<UUID> oficinaIds = List.of(
                UUID.fromString("ad2b57ca-bd6c-405a-87d5-a7009c137f49"),
                UUID.fromString("870744ac-8e40-49c7-97ed-2d82da95f2a7"),
                UUID.fromString("34e7385c-ed5e-4b4a-8912-ac95d55f0e15"),
                UUID.fromString("7644ab69-42fd-42d3-9c1c-19c6edb6575b"),
                UUID.fromString("98fb2b5d-814e-440c-9eab-10ca2dc34724")
        );


        return ResponseEntity.ok(agenteClientService.listarAgentesInterno(oficinaIds));
    }


    @GetMapping("/3")
    public ResponseEntity<Void> propiedadID(){


        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}
