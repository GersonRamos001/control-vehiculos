package com.gerson.ramos.control_vehiculos.reportes.application.port.in;

import java.time.LocalDate;

import org.jspecify.annotations.Nullable;

import com.gerson.ramos.control_vehiculos.alertas.domain.model.Severidad;

/**
 * Un vehículo con mantenimiento vencido o próximo. {@code estado} es {@link Severidad#VENCIDA} o
 * {@link Severidad#PROXIMA}; {@code motivo} es el mensaje de la alerta; {@code fechaReferencia} es la fecha límite
 * del mantenimiento por tiempo.
 */
public record FilaPendiente(long vehiculoId, String placa, String codigoEquipo, String localidad, String tipoUso,
		Severidad estado, String motivo, @Nullable LocalDate ultimoMantenimientoFecha, @Nullable Integer ultimoMantenimientoKm,
		@Nullable Long proximoKm, @Nullable Long kmActual, @Nullable Long kmRestantes, @Nullable Long diasRestantes,
		@Nullable LocalDate fechaReferencia) {

	public boolean vencido() {
		return estado == Severidad.VENCIDA;
	}
}
