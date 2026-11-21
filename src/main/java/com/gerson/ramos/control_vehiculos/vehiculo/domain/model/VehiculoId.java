package com.gerson.ramos.control_vehiculos.vehiculo.domain.model;

import com.gerson.ramos.control_vehiculos.shared.domain.Validaciones;

public record VehiculoId(long valor) {

	public VehiculoId {
		Validaciones.requerirIdPositivo(valor, "El id de vehículo");
	}
}
