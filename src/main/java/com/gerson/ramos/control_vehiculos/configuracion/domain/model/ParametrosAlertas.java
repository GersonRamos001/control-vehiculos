package com.gerson.ramos.control_vehiculos.configuracion.domain.model;

import com.gerson.ramos.control_vehiculos.shared.domain.Validaciones;

/**
 * Umbrales de las alertas. Un mantenimiento está "próximo" cuando faltan {@code umbralKm} km o menos, o
 * {@code umbralDias} días o menos; un vehículo queda "pendiente de lectura" si su última lectura de odómetro tiene más
 * de {@code diasLecturaPendiente} días. Valores iniciales: 1,000 km, 30 días y 7 días.
 */
public record ParametrosAlertas(int umbralKm, int umbralDias, int diasLecturaPendiente) {

	public ParametrosAlertas {
		Validaciones.requerirPositivo(umbralKm, "El umbral en kilómetros");
		Validaciones.requerirPositivo(umbralDias, "El umbral en días");
		Validaciones.requerirPositivo(diasLecturaPendiente, "Los días para lectura pendiente");
	}
}
