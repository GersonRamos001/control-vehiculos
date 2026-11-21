package com.gerson.ramos.control_vehiculos.testsupport;

import com.gerson.ramos.control_vehiculos.configuracion.application.port.out.ParametrosMantenimientoRepositoryPort;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.ParametrosMantenimiento;

/** Implementación en memoria del puerto; arranca con los valores del negocio: 10,000 km y 6 meses. */
public class InMemoryParametrosMantenimientoRepository implements ParametrosMantenimientoRepositoryPort {

	private ParametrosMantenimiento parametros = new ParametrosMantenimiento(10_000, 6);

	@Override
	public ParametrosMantenimiento obtener() {
		return parametros;
	}

	@Override
	public ParametrosMantenimiento guardar(ParametrosMantenimiento nuevos) {
		parametros = nuevos;
		return parametros;
	}
}
