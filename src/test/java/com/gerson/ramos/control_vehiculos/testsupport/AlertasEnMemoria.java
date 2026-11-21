package com.gerson.ramos.control_vehiculos.testsupport;

import java.util.ArrayList;
import java.util.List;

import com.gerson.ramos.control_vehiculos.alertas.application.port.in.AlertaDetalle;
import com.gerson.ramos.control_vehiculos.alertas.application.port.in.ConsultarAlertasUseCase;
import com.gerson.ramos.control_vehiculos.alertas.application.port.in.PanelDeAlertas;
import com.gerson.ramos.control_vehiculos.alertas.application.port.in.ResumenDeAlertas;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.ParametrosAlertas;

/** Puerto de alertas simulado: los tests fijan las alertas "ya calculadas" (en el orden en que las entrega el módulo real). */
public class AlertasEnMemoria implements ConsultarAlertasUseCase {

	private final List<AlertaDetalle> alertas = new ArrayList<>();

	public AlertasEnMemoria con(AlertaDetalle alerta) {
		alertas.add(alerta);
		return this;
	}

	@Override
	public PanelDeAlertas consultar() {
		return new PanelDeAlertas(List.copyOf(alertas), ResumenDeAlertas.de(alertas), new ParametrosAlertas(1_000, 30, 7));
	}
}
