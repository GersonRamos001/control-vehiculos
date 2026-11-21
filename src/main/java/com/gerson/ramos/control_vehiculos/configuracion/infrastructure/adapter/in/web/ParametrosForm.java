package com.gerson.ramos.control_vehiculos.configuracion.infrastructure.adapter.in.web;

import com.gerson.ramos.control_vehiculos.configuracion.domain.model.ParametrosMantenimiento;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

/** Formulario de los parámetros globales de mantenimiento. */
@Getter
@Setter
public class ParametrosForm {

	@NotNull(message = "El intervalo en kilómetros es obligatorio")
	@Positive(message = "El intervalo en kilómetros debe ser mayor que cero")
	private Integer intervaloKm;

	@NotNull(message = "El intervalo en meses es obligatorio")
	@Positive(message = "El intervalo en meses debe ser mayor que cero")
	private Integer intervaloMeses;

	static ParametrosForm desde(ParametrosMantenimiento parametros) {
		ParametrosForm form = new ParametrosForm();
		form.intervaloKm = parametros.intervaloKm();
		form.intervaloMeses = parametros.intervaloMeses();
		return form;
	}
}
