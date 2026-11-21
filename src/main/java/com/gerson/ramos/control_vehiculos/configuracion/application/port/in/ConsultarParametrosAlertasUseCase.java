package com.gerson.ramos.control_vehiculos.configuracion.application.port.in;

import com.gerson.ramos.control_vehiculos.configuracion.domain.model.ParametrosAlertas;

/** Consulta de los umbrales de alerta (los usan los módulos de kilometraje y alertas). */
public interface ConsultarParametrosAlertasUseCase {

	ParametrosAlertas obtener();
}
