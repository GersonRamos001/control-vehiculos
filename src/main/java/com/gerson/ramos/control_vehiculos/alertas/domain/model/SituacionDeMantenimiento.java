package com.gerson.ramos.control_vehiculos.alertas.domain.model;

import org.jspecify.annotations.Nullable;

/**
 * Lo que las reglas necesitan saber de un vehículo: cuánto falta para su siguiente mantenimiento (negativo = ya se
 * pasó), si no se puede programar por falta de mantenimientos y si le falta una lectura de odómetro reciente.
 * Son datos simples a propósito: el dominio no depende de los modelos de otros módulos.
 */
public record SituacionDeMantenimiento(@Nullable Long kmRestantes, @Nullable Long diasRestantes, boolean sinProgramacion,
		boolean lecturaPendiente) {
}
