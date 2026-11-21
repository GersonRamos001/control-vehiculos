-- Catálogos (módulo Configuración) y tabla de vehículos (módulo Vehículo).
-- Los timestamps los llena la auditoría JPA; el DEFAULT cubre inserciones directas (p. ej. semillas).

CREATE TABLE localidad (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    codigo      VARCHAR(30)  NOT NULL,
    nombre      VARCHAR(100) NOT NULL,
    activo      BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at  DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_localidad PRIMARY KEY (id),
    CONSTRAINT uk_localidad_codigo UNIQUE (codigo)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE tipo_uso (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    codigo      VARCHAR(30)  NOT NULL,
    nombre      VARCHAR(100) NOT NULL,
    activo      BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at  DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_tipo_uso PRIMARY KEY (id),
    CONSTRAINT uk_tipo_uso_codigo UNIQUE (codigo)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE rutina_mantenimiento (
    id               BIGINT       NOT NULL AUTO_INCREMENT,
    codigo           VARCHAR(30)  NOT NULL,
    nombre           VARCHAR(100) NOT NULL,
    intervalo_km     INT          NOT NULL,
    intervalo_meses  INT          NOT NULL,
    activo           BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at       DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at       DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_rutina_mantenimiento PRIMARY KEY (id),
    CONSTRAINT uk_rutina_mantenimiento_codigo UNIQUE (codigo),
    CONSTRAINT ck_rutina_intervalo_km CHECK (intervalo_km > 0),
    CONSTRAINT ck_rutina_intervalo_meses CHECK (intervalo_meses > 0)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE vehiculo (
    id                BIGINT       NOT NULL AUTO_INCREMENT,
    placa             VARCHAR(10)  NOT NULL,
    codigo_equipo     VARCHAR(30)  NOT NULL,
    localidad_id      BIGINT       NOT NULL,
    tipo_uso_id       BIGINT       NOT NULL,
    ultima_rutina_id  BIGINT       NULL,
    activo            BOOLEAN      NOT NULL DEFAULT TRUE,
    version           BIGINT       NOT NULL DEFAULT 0,
    created_at        DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at        DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_vehiculo PRIMARY KEY (id),
    CONSTRAINT uk_vehiculo_placa UNIQUE (placa),
    CONSTRAINT uk_vehiculo_codigo_equipo UNIQUE (codigo_equipo),
    CONSTRAINT fk_vehiculo_localidad FOREIGN KEY (localidad_id) REFERENCES localidad (id),
    CONSTRAINT fk_vehiculo_tipo_uso FOREIGN KEY (tipo_uso_id) REFERENCES tipo_uso (id),
    CONSTRAINT fk_vehiculo_ultima_rutina FOREIGN KEY (ultima_rutina_id) REFERENCES rutina_mantenimiento (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
