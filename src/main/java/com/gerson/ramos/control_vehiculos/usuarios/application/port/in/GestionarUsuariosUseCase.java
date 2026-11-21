package com.gerson.ramos.control_vehiculos.usuarios.application.port.in;

import java.util.List;

import com.gerson.ramos.control_vehiculos.usuarios.domain.model.Usuario;

/** Administración de usuarios (solo para el rol ADMINISTRADOR). {@code actor} es el nombre de usuario de quien actúa. */
public interface GestionarUsuariosUseCase {

	Usuario crear(CrearUsuarioComando comando);

	Usuario actualizar(ActualizarUsuarioComando comando);

	/** Activa o desactiva. Nadie puede desactivarse a sí mismo y siempre debe quedar un administrador activo. */
	Usuario cambiarEstado(long id, boolean activo, String actor);

	/** Levanta el bloqueo por intentos fallidos. */
	Usuario desbloquear(long id);

	/** Asigna una contraseña nueva a otro usuario (la propia se cambia con {@link CambiarClavePropiaUseCase}). */
	Usuario restablecerClave(long id, String nuevaClave, String actor);

	Usuario buscar(long id);

	List<Usuario> listar();
}
