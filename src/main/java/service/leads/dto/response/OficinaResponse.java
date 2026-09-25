package service.leads.dto.response;

import lombok.*;
import service.leads.dto.internal.TipoOficina;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OficinaResponse {
    private UUID id;
    private String nombre;
    private String region;
    private Double latitud;
    private Double longitud;
    private TipoOficina tipo;
    private UUID oficinaCentralId;
    private UUID gerenteId;
}
