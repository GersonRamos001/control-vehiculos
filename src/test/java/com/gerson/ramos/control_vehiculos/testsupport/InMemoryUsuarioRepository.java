package com.gerson.ramos.control_vehiculos.testsupport;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.orm.ObjectOptimisticLockingFailureException;

import com.gerson.ramos.control_vehiculos.usuarios.application.port.out.UsuarioRepositoryPort;
import com.gerson.ramos.control_vehiculos.usuarios.domain.model.Rol;
import com.gerson.ramos.control_vehiculos.usuarios.domain.model.Usuario;
import com.gerson.ramos.control_vehiculos.usuarios.domain.model.UsuarioId;

/**
 * Implementación en memoria del puerto, para probar servicios sin Spring ni base de datos. Devuelve copias e imita la
 * versión (empieza en 0 y sube con cada actualización) y el bloqueo optimista del adaptador real.
 */
public class InMemoryUsuarioRepository implements UsuarioRepositoryPort {

	private final Map<Long, Usuario> datos = new LinkedHashMap<>();
	private long secuencia = 0;

	@Override
	public Usuario guardar(Usuario usuario) {
		long id;
		long version;
		if (usuario.id() == null) {
			id = ++secuencia;
			version = 0;
		}
		else {
			id = usuario.id().valor();
			Usuario existente = datos.get(id);
			if (existente == null || !existente.version().equals(usuario.version())) {
				throw new ObjectOptimisticLockingFailureException(Usuario.class, id);
			}
			version = existente.version() + 1;
		}
		Usuario guardado = reconstruir(new UsuarioId(id), usuario, version);
		datos.put(id, guardado);
		return copia(guardado);
	}

	@Override
	public Optional<Usuario> buscarPorId(UsuarioId id) {
		return Optional.ofNullable(datos.get(id.valor())).map(InMemoryUsuarioRepository::copia);
	}

	@Override
	public Optional<Usuario> buscarPorUsername(String username) {
		return datos.values().stream().filter(u -> u.username().equals(username)).findFirst().map(InMemoryUsuarioRepository::copia);
	}

	@Override
	public boolean existePorUsername(String username) {
		return datos.values().stream().anyMatch(u -> u.username().equals(username));
	}

	@Override
	public List<Usuario> listarTodos() {
		return datos.values().stream().sorted(Comparator.comparing(Usuario::username)).map(InMemoryUsuarioRepository::copia).toList();
	}

	@Override
	public long contar() {
		return datos.size();
	}

	@Override
	public long contarAdministradoresActivos() {
		return datos.values().stream().filter(u -> u.activo() && u.rol() == Rol.ADMINISTRADOR).count();
	}

	private static Usuario copia(Usuario u) {
		return reconstruir(u.id(), u, u.version());
	}

	private static Usuario reconstruir(UsuarioId id, Usuario u, long version) {
		return Usuario.reconstruir(id, u.username(), u.nombreCompleto(), u.rol(), u.activo(), u.hashClave(), u.intentosFallidos(),
				u.bloqueadoHasta(), u.ultimoAcceso(), version);
	}
}
