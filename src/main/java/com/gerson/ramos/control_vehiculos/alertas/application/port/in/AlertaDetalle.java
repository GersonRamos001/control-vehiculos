package com.gerson.ramos.control_vehiculos.alertas.application.port.in;

import org.jspecify.annotations.Nullable;

import com.gerson.ramos.control_vehiculos.alertas.domain.model.Severidad;
import com.gerson.ramos.control_vehiculos.alertas.domain.model.TipoAlerta;

/** Una alerta lista para mostrar: el vehículo, qué pasa (mensaje legible) y los km/días restantes si aplican. */
public record AlertaDetalle(long vehiculoId, String placa, String codigoEquipo, String localidad, TipoAlerta tipo,
		Severidad severidad, String mensaje, @Nullable Long kmRestantes, @Nullable Long diasRestantes) {
}
