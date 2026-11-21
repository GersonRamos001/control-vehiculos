package com.gerson.ramos.control_vehiculos.reportes.application.port.in;

import java.util.List;

import com.gerson.ramos.control_vehiculos.mantenimiento.application.port.in.MantenimientoRealizado;
import com.gerson.ramos.control_vehiculos.reportes.domain.model.RangoDeFechas;

/**
 * Mantenimientos realizados en el período efectivo ({@code rango}, ya con los valores por defecto aplicados), del más
 * reciente al más antiguo, con el desglose por rutina y por localidad (de mayor a menor cantidad).
 */
public record ReporteRealizados(RangoDeFechas rango, List<MantenimientoRealizado> filas, List<ConteoPorClave> porRutina,
		List<ConteoPorClave> porLocalidad) {

	public int total() {
		return filas.size();
	}
}
