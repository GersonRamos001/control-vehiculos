-- Parámetros globales de mantenimiento: el próximo mantenimiento es siempre "km registrado + intervalo_km" y
-- "fecha + intervalo_meses", sin importar la rutina (10K/20K/30K) realizada. Una sola fila (id = 1).
CREATE TABLE parametros_mantenimiento (
    id               BIGINT      NOT NULL,
    intervalo_km     INT         NOT NULL,
    intervalo_meses  INT         NOT NULL,
    created_at       DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at       DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_parametros_mantenimiento PRIMARY KEY (id),
    CONSTRAINT ck_parametros_fila_unica CHECK (id = 1),
    CONSTRAINT ck_parametros_intervalo_km CHECK (intervalo_km > 0),
    CONSTRAINT ck_parametros_intervalo_meses CHECK (intervalo_meses > 0)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

INSERT INTO parametros_mantenimiento (id, intervalo_km, intervalo_meses) VALUES (1, 10000, 6);

-- La rutina (10K/20K/30K) es solo el tipo de mantenimiento realizado: el intervalo ya no es suyo.
ALTER TABLE rutina_mantenimiento DROP CHECK ck_rutina_intervalo_km;
ALTER TABLE rutina_mantenimiento DROP CHECK ck_rutina_intervalo_meses;
ALTER TABLE rutina_mantenimiento
    DROP COLUMN intervalo_km,
    DROP COLUMN intervalo_meses;
