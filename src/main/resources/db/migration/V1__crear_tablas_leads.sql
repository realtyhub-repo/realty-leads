CREATE TABLE distribution_rules (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    peso_zona        DOUBLE PRECISION NOT NULL,
    peso_carga       DOUBLE PRECISION NOT NULL,
    peso_conversion  DOUBLE PRECISION NOT NULL,
    created_at       TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMP
);

INSERT INTO distribution_rules (peso_zona, peso_carga, peso_conversion)
VALUES (0.4, 0.3, 0.3);

CREATE TABLE lead (
    id            UUID PRIMARY KEY,
    propiedad_id  UUID NOT NULL,
    cliente_id    UUID NOT NULL,
    agente_id     UUID,
    canal_origen  VARCHAR(50) NOT NULL DEFAULT 'WEB',
    estado        VARCHAR(50) NOT NULL,
    created_at    TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMP
);


CREATE UNIQUE INDEX idx_un_lead_activo_por_cliente_propiedad
    ON lead (cliente_id, propiedad_id)
    WHERE estado NOT IN ('CERRADO', 'DESCARTADO');

CREATE INDEX idx_lead_agente_id ON lead (agente_id);
CREATE INDEX idx_lead_estado ON lead (estado);
