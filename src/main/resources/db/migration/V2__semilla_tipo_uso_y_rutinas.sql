-- Datos base del negocio: tipos de uso y rutinas preventivas (cada 10,000 km o 6 meses).
-- Las localidades no se siembran: las administra el módulo Configuración.

INSERT INTO tipo_uso (codigo, nombre) VALUES
    ('OPERATIVO', 'Operativo'),
    ('RELEVO',    'Relevo');

INSERT INTO rutina_mantenimiento (codigo, nombre, intervalo_km, intervalo_meses) VALUES
    ('10K', 'Mantenimiento preventivo 10,000 km', 10000, 6),
    ('20K', 'Mantenimiento preventivo 20,000 km', 20000, 6),
    ('30K', 'Mantenimiento preventivo 30,000 km', 30000, 6);
