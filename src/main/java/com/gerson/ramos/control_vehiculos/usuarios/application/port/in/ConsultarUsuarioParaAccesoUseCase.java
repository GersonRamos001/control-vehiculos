package com.gerson.ramos.control_vehiculos.usuarios.application.port.in;

import java.util.Optional;

public interface ConsultarUsuarioParaAccesoUseCase {

	/** Busca por nombre de usuario sin distinguir mayúsculas. */
	Optional<CredencialesDeAcceso> buscarPorUsername(String username);
}
