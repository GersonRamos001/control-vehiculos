package com.gerson.ramos.control_vehiculos.usuarios.infrastructure.adapter.out.security;

import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.gerson.ramos.control_vehiculos.usuarios.application.port.out.SesionesPort;

/**
 * Marca como expiradas las sesiones registradas del usuario. El filtro de sesiones concurrentes de Spring Security las
 * rechaza en la siguiente petición y lleva al usuario al inicio de sesión.
 */
@Component
class SesionesAdapter implements SesionesPort {

	private final SessionRegistry registro;

	SesionesAdapter(SessionRegistry registro) {
		this.registro = registro;
	}

	@Override
	public void cerrarSesionesDe(String username) {
		for (Object principal : registro.getAllPrincipals()) {
			if (principal instanceof UserDetails detalles && detalles.getUsername().equalsIgnoreCase(username)) {
				for (SessionInformation sesion : registro.getAllSessions(principal, false)) {
					sesion.expireNow();
				}
			}
		}
	}
}
