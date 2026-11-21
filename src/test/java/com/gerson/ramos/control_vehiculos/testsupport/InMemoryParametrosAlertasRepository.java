package com.gerson.ramos.control_vehiculos.testsupport;

import com.gerson.ramos.control_vehiculos.configuracion.application.port.out.ParametrosAlertasRepositoryPort;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.ParametrosAlertas;

/** Implementación en memoria del puerto; arranca con los valores iniciales: 1,000 km, 30 días y 7 días. */
public class InMemoryParametrosAlertasRepository implements ParametrosAlertasRepositoryPort {

	private ParametrosAlertas parametros = new ParametrosAlertas(1_000, 30, 7);

	@Override
	public ParametrosAlertas obtener() {
		return parametros;
	}

	@Override
	public ParametrosAlertas guardar(ParametrosAlertas nuevos) {
		parametros = nuevos;
		return parametros;
	}
}
