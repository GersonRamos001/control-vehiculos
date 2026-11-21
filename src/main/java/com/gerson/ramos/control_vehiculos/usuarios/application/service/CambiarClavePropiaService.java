package com.gerson.ramos.control_vehiculos.usuarios.application.service;

import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gerson.ramos.control_vehiculos.shared.domain.ConflictoDeDatosException;
import com.gerson.ramos.control_vehiculos.shared.domain.RecursoNoEncontradoException;
import com.gerson.ramos.control_vehiculos.shared.domain.ReglaDeNegocioException;
import com.gerson.ramos.control_vehiculos.usuarios.application.port.in.CambiarClavePropiaUseCase;
import com.gerson.ramos.control_vehiculos.usuarios.application.port.out.CodificadorDeClavePort;
import com.gerson.ramos.control_vehiculos.usuarios.application.port.out.UsuarioRepositoryPort;
import com.gerson.ramos.control_vehiculos.usuarios.domain.model.PoliticaDeClave;
import com.gerson.ramos.control_vehiculos.usuarios.domain.model.Usuario;

@Service
@Transactional
public class CambiarClavePropiaService implements CambiarClavePropiaUseCase {

	private final UsuarioRepositoryPort usuarios;
	private final CodificadorDeClavePort codificador;

	CambiarClavePropiaService(UsuarioRepositoryPort usuarios, CodificadorDeClavePort codificador) {
		this.usuarios = usuarios;
		this.codificador = codificador;
	}

	@Override
	public void cambiar(String username, String claveActual, String claveNueva) {
		Usuario usuario = usuarios.buscarPorUsername(username.strip().toLowerCase(Locale.ROOT))
			.orElseThrow(() -> new RecursoNoEncontradoException("Usuario", username));
		if (!codificador.coincide(claveActual, usuario.hashClave())) {
			throw new ConflictoDeDatosException("claveActual", "La contraseña actual no es correcta");
		}
		PoliticaDeClave.validar(claveNueva, usuario.username());
		if (claveNueva.equals(claveActual)) {
			throw new ReglaDeNegocioException("La contraseña nueva debe ser distinta de la actual");
		}
		usuario.cambiarHashClave(codificador.codificar(claveNueva));
		usuarios.guardar(usuario);
	}
}
