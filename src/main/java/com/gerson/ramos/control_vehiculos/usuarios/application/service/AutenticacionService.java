package com.gerson.ramos.control_vehiculos.usuarios.application.service;

import java.time.Clock;
import java.time.Instant;
import java.util.Locale;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gerson.ramos.control_vehiculos.usuarios.application.port.in.ConsultarUsuarioParaAccesoUseCase;
import com.gerson.ramos.control_vehiculos.usuarios.application.port.in.CredencialesDeAcceso;
import com.gerson.ramos.control_vehiculos.usuarios.application.port.in.RegistrarAutenticacionUseCase;
import com.gerson.ramos.control_vehiculos.usuarios.application.port.out.PoliticaDeBloqueo;
import com.gerson.ramos.control_vehiculos.usuarios.application.port.out.UsuarioRepositoryPort;
import com.gerson.ramos.control_vehiculos.usuarios.domain.model.Usuario;

/** Lado del módulo que usa el inicio de sesión: credenciales, intentos fallidos con bloqueo y último acceso. */
@Service
@Transactional
public class AutenticacionService implements ConsultarUsuarioParaAccesoUseCase, RegistrarAutenticacionUseCase {

	private final UsuarioRepositoryPort usuarios;
	private final PoliticaDeBloqueo politica;
	private final Clock reloj;

	AutenticacionService(UsuarioRepositoryPort usuarios, PoliticaDeBloqueo politica, Clock reloj) {
		this.usuarios = usuarios;
		this.politica = politica;
		this.reloj = reloj;
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<CredencialesDeAcceso> buscarPorUsername(String username) {
		Instant ahora = reloj.instant();
		return buscar(username).map(u -> new CredencialesDeAcceso(u.username(), u.hashClave(), u.rol(), u.activo(), u.estaBloqueado(ahora)));
	}

	@Override
	public void registrarIntentoFallido(String username) {
		buscar(username).filter(Usuario::activo).ifPresent(usuario -> {
			usuario.registrarIntentoFallido(reloj.instant(), politica.maximoIntentos(), politica.duracion());
			usuarios.guardar(usuario);
		});
	}

	@Override
	public void registrarAccesoCorrecto(String username) {
		buscar(username).ifPresent(usuario -> {
			usuario.registrarAcceso(reloj.instant());
			usuarios.guardar(usuario);
		});
	}

	private Optional<Usuario> buscar(String username) {
		return usuarios.buscarPorUsername(username.strip().toLowerCase(Locale.ROOT));
	}
}
