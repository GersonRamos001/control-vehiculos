package com.gerson.ramos.control_vehiculos.usuarios.infrastructure.adapter.out.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.gerson.ramos.control_vehiculos.shared.domain.RecursoNoEncontradoException;
import com.gerson.ramos.control_vehiculos.usuarios.application.port.out.UsuarioRepositoryPort;
import com.gerson.ramos.control_vehiculos.usuarios.domain.model.Rol;
import com.gerson.ramos.control_vehiculos.usuarios.domain.model.Usuario;
import com.gerson.ramos.control_vehiculos.usuarios.domain.model.UsuarioId;

@Repository
@Transactional(readOnly = true)
public class UsuarioPersistenceAdapter implements UsuarioRepositoryPort {

	private final UsuarioJpaRepository repositorio;
	private final UsuarioPersistenceMapper mapper;

	UsuarioPersistenceAdapter(UsuarioJpaRepository repositorio, UsuarioPersistenceMapper mapper) {
		this.repositorio = repositorio;
		this.mapper = mapper;
	}

	@Override
	@Transactional
	public Usuario guardar(Usuario usuario) {
		UsuarioId id = usuario.id();
		UsuarioJpaEntity entidad;
		if (id == null) {
			entidad = mapper.aNuevaEntidad(usuario);
		}
		else {
			entidad = repositorio.findById(id.valor()).orElseThrow(() -> new RecursoNoEncontradoException("Usuario", id.valor()));
			Long versionLeida = usuario.version();
			if (versionLeida != null && !versionLeida.equals(entidad.getVersion())) {
				// El usuario fue modificado por otra operación desde que se leyó.
				throw new ObjectOptimisticLockingFailureException(UsuarioJpaEntity.class, id.valor());
			}
			mapper.actualizar(usuario, entidad);
		}
		// saveAndFlush: las restricciones únicas fallan aquí y la versión devuelta es la vigente.
		return mapper.aDominio(repositorio.saveAndFlush(entidad));
	}

	@Override
	public Optional<Usuario> buscarPorId(UsuarioId id) {
		return repositorio.findById(id.valor()).map(mapper::aDominio);
	}

	@Override
	public Optional<Usuario> buscarPorUsername(String username) {
		return repositorio.findByUsername(username).map(mapper::aDominio);
	}

	@Override
	public boolean existePorUsername(String username) {
		return repositorio.existsByUsername(username);
	}

	@Override
	public List<Usuario> listarTodos() {
		return repositorio.findAllByOrderByUsername().stream().map(mapper::aDominio).toList();
	}

	@Override
	public long contar() {
		return repositorio.count();
	}

	@Override
	public long contarAdministradoresActivos() {
		return repositorio.countByRolAndActivoTrue(Rol.ADMINISTRADOR.name());
	}
}
