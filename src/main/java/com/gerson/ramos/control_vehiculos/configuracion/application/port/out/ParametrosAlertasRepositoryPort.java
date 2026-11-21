package com.gerson.ramos.control_vehiculos.configuracion.application.port.out;

import com.gerson.ramos.control_vehiculos.configuracion.domain.model.ParametrosAlertas;

/** Puerto de salida: persistencia de los umbrales de alerta (siempre existen, son una sola fila). */
public interface ParametrosAlertasRepositoryPort {

	ParametrosAlertas obtener();

	ParametrosAlertas guardar(ParametrosAlertas parametros);
}
