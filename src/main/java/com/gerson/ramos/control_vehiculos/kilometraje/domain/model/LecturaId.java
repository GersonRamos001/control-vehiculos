package com.gerson.ramos.control_vehiculos.kilometraje.domain.model;

import com.gerson.ramos.control_vehiculos.shared.domain.Validaciones;

public record LecturaId(long valor) {

	public LecturaId {
		Validaciones.requerirIdPositivo(valor, "El id de lectura");
	}
}
