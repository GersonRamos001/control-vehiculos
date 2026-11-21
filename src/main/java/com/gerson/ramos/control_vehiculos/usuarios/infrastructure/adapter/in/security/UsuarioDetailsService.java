package com.gerson.ramos.control_vehiculos.usuarios.infrastructure.adapter.in.security;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.gerson.ramos.control_vehiculos.usuarios.application.port.in.ConsultarUsuarioParaAccesoUseCase;
import com.gerson.ramos.control_vehiculos.usuarios.application.port.in.CredencialesDeAcceso;

/**
 * Entrega a Spring Security las credenciales guardadas. Un usuario desactivado o bloqueado se devuelve con ese estado:
 * Spring Security rechaza el inicio de sesión sin comprobar la contraseña (y por eso esos intentos no cuentan como fallos).
 */
@Service
class UsuarioDetailsService implements UserDetailsService {

	private final ConsultarUsuarioParaAccesoUseCase usuarios;

	UsuarioDetailsService(ConsultarUsuarioParaAccesoUseCase usuarios) {
		this.usuarios = usuarios;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		CredencialesDeAcceso credenciales = usuarios.buscarPorUsername(username)
			.orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
		return User.withUsername(credenciales.username())
			.password(credenciales.hashClave())
			.roles(credenciales.rol().name())
			.disabled(!credenciales.activo())
			.accountLocked(credenciales.bloqueado())
			.build();
	}
}
