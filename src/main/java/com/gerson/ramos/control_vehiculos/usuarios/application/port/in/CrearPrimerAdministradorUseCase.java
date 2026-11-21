package com.gerson.ramos.control_vehiculos.usuarios.application.port.in;

/** Arranque con la tabla de usuarios vacía: crea el administrador inicial. */
public interface CrearPrimerAdministradorUseCase {

	/**
	 * Crea un ADMINISTRADOR solo si todavía no existe ningún usuario.
	 *
	 * @return {@code true} si lo creó, {@code false} si ya había usuarios (no se valida ni se usa la contraseña)
	 */
	boolean crearSiNoHayUsuarios(String username, String clave);
}
