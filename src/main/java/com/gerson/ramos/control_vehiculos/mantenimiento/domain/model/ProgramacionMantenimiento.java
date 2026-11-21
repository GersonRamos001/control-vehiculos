package com.gerson.ramos.control_vehiculos.mantenimiento.domain.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import com.gerson.ramos.control_vehiculos.configuracion.domain.model.ParametrosMantenimiento;
import com.gerson.ramos.control_vehiculos.shared.domain.Validaciones;

/**
 * Cuándo corresponde el siguiente mantenimiento de un vehículo, calculado a partir de su último mantenimiento
 * y de los parámetros globales: próximo kilometraje = km + intervalo; fecha de referencia = fecha + meses.
 * Se calcula al consultar, no se guarda.
 *
 * @param diasRestantes días desde hoy hasta la fecha de referencia; negativo si ya se venció
 */
public record ProgramacionMantenimiento(Mantenimiento ultimo, long proximoKm, LocalDate fechaReferencia,
		long diasRestantes) {

	public static ProgramacionMantenimiento calcular(Mantenimiento ultimo, ParametrosMantenimiento parametros,
			LocalDate hoy) {
		Validaciones.requerir(ultimo, "El último mantenimiento");
		Validaciones.requerir(parametros, "Los parámetros de mantenimiento");
		Validaciones.requerir(hoy, "La fecha actual");
		long proximoKm = (long) ultimo.kilometraje() + parametros.intervaloKm();
		LocalDate fechaReferencia = ultimo.fecha().plusMonths(parametros.intervaloMeses());
		return new ProgramacionMantenimiento(ultimo, proximoKm, fechaReferencia,
				ChronoUnit.DAYS.between(hoy, fechaReferencia));
	}

	public boolean vencidaPorFecha() {
		return diasRestantes < 0;
	}
}
