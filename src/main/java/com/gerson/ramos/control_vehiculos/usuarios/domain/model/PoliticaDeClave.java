package com.gerson.ramos.control_vehiculos.usuarios.domain.model;

import org.jspecify.annotations.Nullable;

import com.gerson.ramos.control_vehiculos.shared.domain.ReglaDeNegocioException;

/** Reglas de las contraseñas: de 8 a 72 caracteres (72 es el límite de BCrypt) y distintas del nombre de usuario. */
public final class PoliticaDeClave {

	public static final int LONGITUD_MINIMA = 8;
	public static final int LONGITUD_MAXIMA = 72;

	private PoliticaDeClave() {
	}

	/** Lanza {@link ReglaDeNegocioException} si la contraseña no cumple las reglas. */
	public static void validar(@Nullable String clave, String username) {
		if (clave == null || clave.isEmpty()) {
			throw new ReglaDeNegocioException("La contraseña es obligatoria");
		}
		if (clave.length() < LONGITUD_MINIMA) {
			throw new ReglaDeNegocioException("La contraseña debe tener al menos " + LONGITUD_MINIMA + " caracteres");
		}
		if (clave.length() > LONGITUD_MAXIMA) {
			throw new ReglaDeNegocioException("La contraseña no puede exceder " + LONGITUD_MAXIMA + " caracteres");
		}
		if (clave.equalsIgnoreCase(username)) {
			throw new ReglaDeNegocioException("La contraseña no puede ser igual al nombre de usuario");
		}
	}
}
