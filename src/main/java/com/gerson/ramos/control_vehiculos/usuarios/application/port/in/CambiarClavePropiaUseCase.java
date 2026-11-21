package com.gerson.ramos.control_vehiculos.usuarios.application.port.in;

/** Cada usuario cambia su propia contraseña, confirmando la actual. */
public interface CambiarClavePropiaUseCase {

	void cambiar(String username, String claveActual, String claveNueva);
}
