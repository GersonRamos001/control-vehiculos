package com.gerson.ramos.control_vehiculos.usuarios.application.port.out;

/** Sesiones web abiertas. */
public interface SesionesPort {

	/** Invalida las sesiones abiertas del usuario: su siguiente petición lo lleva al inicio de sesión. */
	void cerrarSesionesDe(String username);
}
