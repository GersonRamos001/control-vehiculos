package com.gerson.ramos.control_vehiculos.kilometraje.application.port.in;

import java.util.Map;

/**
 * Resultado de la captura semanal. Las filas inválidas no impiden guardar las válidas; {@code errores} las
 * identifica por id de vehículo con el motivo.
 */
public record ResultadoLote(int nuevas, int reemplazadas, int sinCambios, Map<Long, String> errores) {

	public int registradas() {
		return nuevas + reemplazadas;
	}

	public boolean huboErrores() {
		return !errores.isEmpty();
	}
}
