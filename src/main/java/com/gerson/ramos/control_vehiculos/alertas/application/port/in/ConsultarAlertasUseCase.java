package com.gerson.ramos.control_vehiculos.alertas.application.port.in;

public interface ConsultarAlertasUseCase {

	/**
	 * Calcula las alertas vigentes de los vehículos activos a partir de su seguimiento actual y de los umbrales
	 * configurados. No se guardan: desaparecen cuando se registra el mantenimiento o la lectura que las resuelve.
	 */
	PanelDeAlertas consultar();
}
