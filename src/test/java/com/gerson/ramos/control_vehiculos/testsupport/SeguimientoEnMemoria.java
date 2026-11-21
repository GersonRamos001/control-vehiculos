package com.gerson.ramos.control_vehiculos.testsupport;

import java.util.ArrayList;
import java.util.List;

import com.gerson.ramos.control_vehiculos.kilometraje.application.port.in.ConsultarSeguimientoUseCase;
import com.gerson.ramos.control_vehiculos.kilometraje.application.port.in.FilaSeguimiento;
import com.gerson.ramos.control_vehiculos.shared.domain.RecursoNoEncontradoException;

/** Puerto de seguimiento simulado: los tests fijan las filas que "calculó" el módulo de kilometraje. */
public class SeguimientoEnMemoria implements ConsultarSeguimientoUseCase {

	private final List<FilaSeguimiento> filas = new ArrayList<>();

	public SeguimientoEnMemoria con(FilaSeguimiento fila) {
		filas.add(fila);
		return this;
	}

	@Override
	public List<FilaSeguimiento> seguimientoSemanal() {
		return List.copyOf(filas);
	}

	@Override
	public FilaSeguimiento seguimientoDe(long vehiculoId) {
		return filas.stream().filter(f -> f.vehiculoId() == vehiculoId).findFirst()
			.orElseThrow(() -> new RecursoNoEncontradoException("Vehículo", vehiculoId));
	}
}
