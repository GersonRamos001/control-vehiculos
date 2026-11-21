package com.gerson.ramos.control_vehiculos.configuracion.domain.model;

import com.gerson.ramos.control_vehiculos.shared.domain.Validaciones;

public record RutinaMantenimientoId(long valor) {

	public RutinaMantenimientoId {
		Validaciones.requerirIdPositivo(valor, "El id de rutina de mantenimiento");
	}
}
