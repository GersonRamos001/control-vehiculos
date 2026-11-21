package com.gerson.ramos.control_vehiculos.kilometraje.infrastructure.adapter.in.web;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * Formulario de la captura semanal: una fecha común y el kilometraje por id de vehículo
 * ({@code kilometrajes[5]=123456}). Las casillas vacías llegan como {@code null} y se ignoran.
 */
@Getter
@Setter
public class SeguimientoSemanalForm {

	@NotNull(message = "La fecha de la lectura es obligatoria")
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
	private LocalDate fecha;

	private Map<Long, Integer> kilometrajes = new LinkedHashMap<>();

	static SeguimientoSemanalForm para(LocalDate fechaInicial) {
		SeguimientoSemanalForm form = new SeguimientoSemanalForm();
		form.fecha = fechaInicial;
		return form;
	}
}
