package com.gerson.ramos.control_vehiculos.usuarios.application.service;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gerson.ramos.control_vehiculos.shared.domain.ConflictoDeConcurrenciaException;
import com.gerson.ramos.control_vehiculos.shared.domain.ConflictoDeDatosException;
import com.gerson.ramos.control_vehiculos.shared.domain.RecursoNoEncontradoException;
import com.gerson.ramos.control_vehiculos.shared.domain.ReglaDeNegocioException;
import com.gerson.ramos.control_vehiculos.usuarios.application.port.in.ActualizarUsuarioComando;
import com.gerson.ramos.control_vehiculos.usuarios.application.port.in.CrearPrimerAdministradorUseCase;
import com.gerson.ramos.control_vehiculos.usuarios.application.port.in.CrearUsuarioComando;
import com.gerson.ramos.control_vehiculos.usuarios.application.port.in.GestionarUsuariosUseCase;
import com.gerson.ramos.control_vehiculos.usuarios.application.port.out.CodificadorDeClavePort;
import com.gerson.ramos.control_vehiculos.usuarios.application.port.out.SesionesPort;
import com.gerson.ramos.control_vehiculos.usuarios.application.port.out.UsuarioRepositoryPort;
import com.gerson.ramos.control_vehiculos.usuarios.domain.model.PoliticaDeClave;
import com.gerson.ramos.control_vehiculos.usuarios.domain.model.Rol;
import com.gerson.ramos.control_vehiculos.usuarios.domain.model.Usuario;
import com.gerson.ramos.control_vehiculos.usuarios.domain.model.UsuarioId;

@Service
@Transactional
public class GestionarUsuariosService implements GestionarUsuariosUseCase, CrearPrimerAdministradorUseCase {

	private static final String NOMBRE_DEL_PRIMER_ADMINISTRADOR = "Administrador";

	private final UsuarioRepositoryPort usuarios;
	private final CodificadorDeClavePort codificador;
	private final SesionesPort sesiones;

	GestionarUsuariosService(UsuarioRepositoryPort usuarios, CodificadorDeClavePort codificador, SesionesPort sesiones) {
		this.usuarios = usuarios;
		this.codificador = codificador;
		this.sesiones = sesiones;
	}

	@Override
	public Usuario crear(CrearUsuarioComando comando) {
		String username = Usuario.normalizarUsername(comando.username());
		if (usuarios.existePorUsername(username)) {
			throw new ConflictoDeDatosException("username", "Ya existe un usuario con el nombre " + username);
		}
		PoliticaDeClave.validar(comando.clave(), username);
		return usuarios.guardar(Usuario.crear(username, comando.nombreCompleto(), comando.rol(), codificador.codificar(comando.clave())));
	}

	@Override
	public Usuario actualizar(ActualizarUsuarioComando comando) {
		Usuario usuario = cargar(comando.id());
		if (!Objects.equals(usuario.version(), comando.version())) {
			throw new ConflictoDeConcurrenciaException("El usuario " + usuario.username());
		}
		if (usuario.rol().esAdministrador() && !comando.rol().esAdministrador()) {
			if (esElMismo(usuario, comando.actor())) {
				throw new ReglaDeNegocioException("No puedes quitarte a ti mismo el rol de administrador");
			}
			exigirOtroAdministradorActivo(usuario);
		}
		Rol rolAnterior = usuario.rol();
		usuario.corregirDatos(comando.nombreCompleto(), comando.rol());
		Usuario guardado = usuarios.guardar(usuario);
		if (rolAnterior != guardado.rol()) {
			sesiones.cerrarSesionesDe(guardado.username());
		}
		return guardado;
	}

	@Override
	public Usuario cambiarEstado(long id, boolean activo, String actor) {
		Usuario usuario = cargar(id);
		if (activo == usuario.activo()) {
			return usuario;
		}
		if (activo) {
			usuario.activar();
			return usuarios.guardar(usuario);
		}
		if (esElMismo(usuario, actor)) {
			throw new ReglaDeNegocioException("No puedes desactivar tu propia cuenta");
		}
		exigirOtroAdministradorActivo(usuario);
		usuario.desactivar();
		Usuario guardado = usuarios.guardar(usuario);
		sesiones.cerrarSesionesDe(guardado.username());
		return guardado;
	}

	@Override
	public Usuario desbloquear(long id) {
		Usuario usuario = cargar(id);
		usuario.desbloquear();
		return usuarios.guardar(usuario);
	}

	@Override
	public Usuario restablecerClave(long id, String nuevaClave, String actor) {
		Usuario usuario = cargar(id);
		if (esElMismo(usuario, actor)) {
			throw new ReglaDeNegocioException("Para cambiar tu propia contraseña usa la opción «Cambiar mi contraseña»");
		}
		PoliticaDeClave.validar(nuevaClave, usuario.username());
		usuario.cambiarHashClave(codificador.codificar(nuevaClave));
		usuario.desbloquear();
		Usuario guardado = usuarios.guardar(usuario);
		sesiones.cerrarSesionesDe(guardado.username());
		return guardado;
	}

	@Override
	@Transactional(readOnly = true)
	public Usuario buscar(long id) {
		return cargar(id);
	}

	@Override
	@Transactional(readOnly = true)
	public List<Usuario> listar() {
		return usuarios.listarTodos();
	}

	@Override
	public boolean crearSiNoHayUsuarios(String username, String clave) {
		if (usuarios.contar() > 0) {
			return false;
		}
		String normalizado = Usuario.normalizarUsername(username);
		PoliticaDeClave.validar(clave, normalizado);
		usuarios.guardar(Usuario.crear(normalizado, NOMBRE_DEL_PRIMER_ADMINISTRADOR, Rol.ADMINISTRADOR, codificador.codificar(clave)));
		return true;
	}

	private Usuario cargar(long id) {
		return usuarios.buscarPorId(new UsuarioId(id)).orElseThrow(() -> new RecursoNoEncontradoException("Usuario", id));
	}

	private static boolean esElMismo(Usuario usuario, String actor) {
		return usuario.username().equalsIgnoreCase(actor);
	}

	/** El usuario es un administrador activo que dejaría de serlo: debe haber otro que siga activo. */
	private void exigirOtroAdministradorActivo(Usuario usuario) {
		if (usuario.rol().esAdministrador() && usuario.activo() && usuarios.contarAdministradoresActivos() <= 1) {
			throw new ReglaDeNegocioException("Debe quedar al menos un administrador activo");
		}
	}
}
