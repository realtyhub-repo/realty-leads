CREATE TABLE distribution_rules (
                                    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                    peso_zona        DOUBLE PRECISION NOT NULL,
                                    peso_carga       DOUBLE PRECISION NOT NULL,
                                    peso_conversion  DOUBLE PRECISION NOT NULL,
                                    created_at       TIMESTAMP NOT NULL DEFAULT NOW(),
                                    updated_at       TIMESTAMP
);