-- Lecturas semanales del odómetro. Una lectura por vehículo y día: registrar otra para la misma fecha la reemplaza
-- (lo hace la aplicación actualizando la fila existente).
CREATE TABLE lectura_kilometraje (
    id           BIGINT      NOT NULL AUTO_INCREMENT,
    vehiculo_id  BIGINT      NOT NULL,
    fecha        DATE        NOT NULL,
    kilometraje  INT         NOT NULL,
    created_at   DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at   DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_lectura_kilometraje PRIMARY KEY (id),
    CONSTRAINT ck_lectura_kilometraje CHECK (kilometraje >= 0),
    CONSTRAINT fk_lectura_vehiculo FOREIGN KEY (vehiculo_id) REFERENCES vehiculo (id),
    -- Garantiza una lectura por vehículo y fecha; también sirve al historial y a "última lectura".
    CONSTRAINT uk_lectura_vehiculo_fecha UNIQUE (vehiculo_id, fecha)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
