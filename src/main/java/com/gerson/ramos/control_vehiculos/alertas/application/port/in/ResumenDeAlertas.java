package com.gerson.ramos.control_vehiculos.alertas.application.port.in;

import java.util.List;

import com.gerson.ramos.control_vehiculos.alertas.domain.model.Severidad;

/** Cuántas alertas hay por severidad. {@link #activas()} (vencidas + próximas) es lo que muestra la barra de navegación. */
public record ResumenDeAlertas(int vencidas, int proximas, int informativas) {

	public static ResumenDeAlertas de(List<AlertaDetalle> alertas) {
		return new ResumenDeAlertas(contar(alertas, Severidad.VENCIDA), contar(alertas, Severidad.PROXIMA),
				contar(alertas, Severidad.INFORMATIVA));
	}

	public int activas() {
		return vencidas + proximas;
	}

	public int total() {
		return vencidas + proximas + informativas;
	}

	private static int contar(List<AlertaDetalle> alertas, Severidad severidad) {
		return (int) alertas.stream().filter(alerta -> alerta.severidad() == severidad).count();
	}
}
