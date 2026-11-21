package com.gerson.ramos.control_vehiculos.usuarios.domain.model;

import com.gerson.ramos.control_vehiculos.shared.domain.Validaciones;

public record UsuarioId(long valor) {

	public UsuarioId {
		Validaciones.requerirIdPositivo(valor, "El id de usuario");
	}
}
