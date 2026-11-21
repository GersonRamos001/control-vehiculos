package com.gerson.ramos.control_vehiculos.configuracion.application.port.in;

import com.gerson.ramos.control_vehiculos.configuracion.domain.model.ParametrosAlertas;

public interface ActualizarParametrosAlertasUseCase {

	/** @throws com.gerson.ramos.control_vehiculos.shared.domain.ReglaDeNegocioException si algún valor no es positivo */
	ParametrosAlertas actualizar(int umbralKm, int umbralDias, int diasLecturaPendiente);
}
