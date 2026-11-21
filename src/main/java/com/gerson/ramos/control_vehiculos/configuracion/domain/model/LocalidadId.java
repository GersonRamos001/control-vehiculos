package com.gerson.ramos.control_vehiculos.configuracion.domain.model;

import com.gerson.ramos.control_vehiculos.shared.domain.Validaciones;

public record LocalidadId(long valor) {

	public LocalidadId {
		Validaciones.requerirIdPositivo(valor, "El id de localidad");
	}
}
