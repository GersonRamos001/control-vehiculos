package com.gerson.ramos.control_vehiculos.usuarios.infrastructure.adapter.in.security;

import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.gerson.ramos.control_vehiculos.shared.domain.ReglaDeNegocioException;
import com.gerson.ramos.control_vehiculos.usuarios.application.port.in.CrearPrimerAdministradorUseCase;
import com.gerson.ramos.control_vehiculos.usuarios.infrastructure.config.SeguridadProperties;

/**
 * Al arrancar con la tabla de usuarios vacía crea el administrador inicial con {@code app.security.usuario} y
 * {@code app.security.password}. Si ya hay usuarios no hace nada (esas propiedades dejan de usarse).
 */
@Component
class PrimerAdministradorRunner implements ApplicationRunner {

	private static final Logger log = LoggerFactory.getLogger(PrimerAdministradorRunner.class);

	private final CrearPrimerAdministradorUseCase primerAdministrador;
	private final SeguridadProperties propiedades;

	PrimerAdministradorRunner(CrearPrimerAdministradorUseCase primerAdministrador, SeguridadProperties propiedades) {
		this.primerAdministrador = primerAdministrador;
		this.propiedades = propiedades;
	}

	@Override
	public void run(ApplicationArguments argumentos) {
		try {
			if (primerAdministrador.crearSiNoHayUsuarios(propiedades.usuario(), propiedades.password())) {
				log.info("No había usuarios: se creó el administrador inicial '{}'. Cambie su contraseña desde la aplicación.",
						propiedades.usuario().strip().toLowerCase(Locale.ROOT));
			}
		}
		catch (ReglaDeNegocioException excepcion) {
			throw new IllegalStateException("No hay usuarios en la base de datos y las credenciales del administrador inicial "
					+ "(APP_SECURITY_USUARIO y APP_SECURITY_PASSWORD) no son válidas: " + excepcion.getMessage(), excepcion);
		}
	}
}
