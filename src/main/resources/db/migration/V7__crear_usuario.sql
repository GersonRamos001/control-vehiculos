-- Usuarios de la aplicación (módulo Usuarios). Sin semilla: el primer administrador lo crea el arranque con las
-- credenciales de app.security.usuario / app.security.password cuando esta tabla está vacía (necesita calcular el hash).
--   username:           en minúsculas, único.
--   rol:                ADMINISTRADOR | OPERADOR | CONSULTA (texto, no ENUM nativo, para poder agregar roles sin alterar la columna).
--   clave_hash:         hash con prefijo de algoritmo, p. ej. {bcrypt}...; nunca la contraseña en claro.
--   intentos_fallidos:  contraseñas incorrectas seguidas; bloqueado_hasta: la cuenta no entra antes de esa hora.
--   ultimo_acceso:      último inicio de sesión correcto.
CREATE TABLE usuario (
    id                 BIGINT       NOT NULL AUTO_INCREMENT,
    username           VARCHAR(30)  NOT NULL,
    nombre_completo    VARCHAR(100) NOT NULL,
    rol                VARCHAR(20)  NOT NULL,
    clave_hash         VARCHAR(100) NOT NULL,
    activo             BOOLEAN      NOT NULL DEFAULT TRUE,
    intentos_fallidos  INT          NOT NULL DEFAULT 0,
    bloqueado_hasta    DATETIME(6)  NULL,
    ultimo_acceso      DATETIME(6)  NULL,
    version            BIGINT       NOT NULL DEFAULT 0,
    created_at         DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at         DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_usuario PRIMARY KEY (id),
    CONSTRAINT uk_usuario_username UNIQUE (username),
    CONSTRAINT ck_usuario_rol CHECK (rol IN ('ADMINISTRADOR', 'OPERADOR', 'CONSULTA')),
    CONSTRAINT ck_usuario_intentos CHECK (intentos_fallidos >= 0)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
