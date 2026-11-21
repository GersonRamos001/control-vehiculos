-- Historial de mantenimientos preventivos realizados. Solo se agregan registros (no se editan ni se borran).
CREATE TABLE mantenimiento (
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    vehiculo_id    BIGINT       NOT NULL,
    rutina_id      BIGINT       NOT NULL,
    fecha          DATE         NOT NULL,
    kilometraje    INT          NOT NULL,
    observaciones  VARCHAR(500) NULL,
    created_at     DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at     DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_mantenimiento PRIMARY KEY (id),
    CONSTRAINT ck_mantenimiento_kilometraje CHECK (kilometraje >= 0),
    CONSTRAINT fk_mantenimiento_vehiculo FOREIGN KEY (vehiculo_id) REFERENCES vehiculo (id),
    CONSTRAINT fk_mantenimiento_rutina FOREIGN KEY (rutina_id) REFERENCES rutina_mantenimiento (id),
    -- Sirve a "historial de un vehículo" y "último mantenimiento por vehículo".
    INDEX ix_mantenimiento_vehiculo_fecha (vehiculo_id, fecha, id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
