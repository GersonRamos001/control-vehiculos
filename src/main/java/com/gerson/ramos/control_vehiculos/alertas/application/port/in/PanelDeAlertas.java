package com.gerson.ramos.control_vehiculos.alertas.application.port.in;

import java.util.List;

import com.gerson.ramos.control_vehiculos.alertas.domain.model.Severidad;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.ParametrosAlertas;

/** Todas las alertas vigentes (ordenadas: vencidas, próximas, informativas; luego por placa), su resumen y los umbrales usados. */
public record PanelDeAlertas(List<AlertaDetalle> alertas, ResumenDeAlertas resumen, ParametrosAlertas umbrales) {

	public List<AlertaDetalle> deSeveridad(Severidad severidad) {
		return alertas.stream().filter(alerta -> alerta.severidad() == severidad).toList();
	}
}
