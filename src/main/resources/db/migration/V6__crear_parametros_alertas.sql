-- Umbrales de las alertas. Una sola fila (id = 1), igual que parametros_mantenimiento.
--   umbral_km:               un mantenimiento está "próximo" cuando faltan este número de km o menos.
--   umbral_dias:             ... o este número de días o menos.
--   dias_lectura_pendiente:  un vehículo queda "pendiente de lectura" si su última lectura tiene más días que estos.
CREATE TABLE parametros_alertas (
    id                      BIGINT      NOT NULL,
    umbral_km               INT         NOT NULL,
    umbral_dias             INT         NOT NULL,
    dias_lectura_pendiente  INT         NOT NULL,
    created_at              DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at              DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_parametros_alertas PRIMARY KEY (id),
    CONSTRAINT ck_parametros_alertas_fila_unica CHECK (id = 1),
    CONSTRAINT ck_parametros_alertas_umbral_km CHECK (umbral_km > 0),
    CONSTRAINT ck_parametros_alertas_umbral_dias CHECK (umbral_dias > 0),
    CONSTRAINT ck_parametros_alertas_dias_lectura CHECK (dias_lectura_pendiente > 0)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

INSERT INTO parametros_alertas (id, umbral_km, umbral_dias, dias_lectura_pendiente) VALUES (1, 1000, 30, 7);
