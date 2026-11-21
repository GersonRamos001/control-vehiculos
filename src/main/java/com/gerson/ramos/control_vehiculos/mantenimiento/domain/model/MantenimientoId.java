package com.gerson.ramos.control_vehiculos.mantenimiento.domain.model;

import com.gerson.ramos.control_vehiculos.shared.domain.Validaciones;

public record MantenimientoId(long valor) {

	public MantenimientoId {
		Validaciones.requerirIdPositivo(valor, "El id de mantenimiento");
	}
}
