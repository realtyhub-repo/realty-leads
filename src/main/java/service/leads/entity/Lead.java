package service.leads.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "lead")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Lead {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, name = "propiedad_id")
    private UUID propiedadId;

    @Column(nullable = false, name = "clientee_id")
    private UUID clientId;

    @Column(nullable = true, name = "agente_id")
    private UUID agenteId;

    @Enumerated(EnumType.STRING)
    @Column( nullable = false, name = "canal_origen")
    @Builder.Default
    private CanalOrigen canalOrigen=CanalOrigen.WEB;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Estado estado;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;


    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate(){
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();

    }

}
