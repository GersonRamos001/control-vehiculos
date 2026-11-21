package com.gerson.ramos.control_vehiculos.usuarios.infrastructure.adapter.out.security;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.gerson.ramos.control_vehiculos.usuarios.application.port.out.CodificadorDeClavePort;

/** Usa el {@link PasswordEncoder} de la aplicación (BCrypt con prefijo {@code {bcrypt}}), el mismo que verifica el inicio de sesión. */
@Component
class CodificadorDeClaveAdapter implements CodificadorDeClavePort {

	private final PasswordEncoder codificador;

	CodificadorDeClaveAdapter(PasswordEncoder codificador) {
		this.codificador = codificador;
	}

	@Override
	public String codificar(String clave) {
		return codificador.encode(clave);
	}

	@Override
	public boolean coincide(String clave, String hash) {
		return codificador.matches(clave, hash);
	}
}
