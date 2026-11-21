package com.gerson.ramos.control_vehiculos.reportes.infrastructure.adapter.in.web;

import com.gerson.ramos.control_vehiculos.reportes.application.port.in.EstadoPendiente;
import com.gerson.ramos.control_vehiculos.reportes.application.port.in.FiltroPendientes;

import lombok.Getter;
import lombok.Setter;

/** Parámetros (GET) del reporte de vehículos pendientes; sin parámetros equivale a "todos". */
@Getter
@Setter
public class FiltroPendientesForm {

	private EstadoPendiente estado = EstadoPendiente.TODOS;

	private Long localidadId;

	private Long tipoUsoId;

	FiltroPendientes aFiltro() {
		return new FiltroPendientes(estado == null ? EstadoPendiente.TODOS : estado, localidadId, tipoUsoId);
	}
}
