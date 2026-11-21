package com.gerson.ramos.control_vehiculos.configuracion.application.port.in;

import com.gerson.ramos.control_vehiculos.configuracion.domain.model.ParametrosMantenimiento;

public interface ActualizarParametrosMantenimientoUseCase {

	/** @throws com.gerson.ramos.control_vehiculos.shared.domain.ReglaDeNegocioException si algún intervalo no es positivo */
	ParametrosMantenimiento actualizar(int intervaloKm, int intervaloMeses);
}
