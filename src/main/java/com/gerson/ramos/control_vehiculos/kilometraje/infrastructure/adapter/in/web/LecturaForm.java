package com.gerson.ramos.control_vehiculos.kilometraje.infrastructure.adapter.in.web;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

/** Formulario de una lectura individual (el vehículo viene en la URL). */
@Getter
@Setter
public class LecturaForm {

	@NotNull(message = "La fecha es obligatoria")
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
	private LocalDate fecha;

	@NotNull(message = "El kilometraje es obligatorio")
	@PositiveOrZero(message = "El kilometraje no puede ser negativo")
	private Integer kilometraje;

	static LecturaForm para(LocalDate fechaInicial) {
		LecturaForm form = new LecturaForm();
		form.fecha = fechaInicial;
		return form;
	}
}
