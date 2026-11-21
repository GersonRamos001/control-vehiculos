package com.gerson.ramos.control_vehiculos.usuarios.application.port.out;

import java.util.List;
import java.util.Optional;

import com.gerson.ramos.control_vehiculos.usuarios.domain.model.Usuario;
import com.gerson.ramos.control_vehiculos.usuarios.domain.model.UsuarioId;

public interface UsuarioRepositoryPort {

	/** Inserta o actualiza. Una actualización con versión distinta de la guardada falla con bloqueo optimista. */
	Usuario guardar(Usuario usuario);

	Optional<Usuario> buscarPorId(UsuarioId id);

	/** El nombre de usuario ya viene en minúsculas. */
	Optional<Usuario> buscarPorUsername(String username);

	boolean existePorUsername(String username);

	/** Todos, ordenados por nombre de usuario. */
	List<Usuario> listarTodos();

	long contar();

	long contarAdministradoresActivos();
}
