package com.gerson.ramos.control_vehiculos.usuarios.application.port.in;

/** Lo que el inicio de sesión reporta al módulo: contraseñas incorrectas (para el bloqueo) y accesos correctos. */
public interface RegistrarAutenticacionUseCase {

	/** Cuenta un intento fallido; si no existe el usuario o está desactivado no hace nada. */
	void registrarIntentoFallido(String username);

	/** Reinicia los intentos y anota el último acceso. */
	void registrarAccesoCorrecto(String username);
}
