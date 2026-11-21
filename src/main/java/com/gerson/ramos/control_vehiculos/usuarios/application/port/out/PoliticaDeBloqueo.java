package com.gerson.ramos.control_vehiculos.usuarios.application.port.out;

import java.time.Duration;

import com.gerson.ramos.control_vehiculos.shared.domain.Validaciones;

/** Cuántas contraseñas incorrectas seguidas bloquean la cuenta y por cuánto tiempo (la configuración la aporta la infraestructura). */
public record PoliticaDeBloqueo(int maximoIntentos, Duration duracion) {

	public PoliticaDeBloqueo {
		Validaciones.requerirPositivo(maximoIntentos, "El máximo de intentos");
		if (duracion.isNegative() || duracion.isZero()) {
			throw new IllegalArgumentException("La duración del bloqueo debe ser mayor que cero");
		}
	}
}
