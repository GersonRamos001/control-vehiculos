package com.gerson.ramos.control_vehiculos.configuracion.infrastructure.adapter.in.web;

import com.gerson.ramos.control_vehiculos.configuracion.domain.model.ParametrosAlertas;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

/** Formulario de los umbrales de alerta. */
@Getter
@Setter
public class ParametrosAlertasForm {

	@NotNull(message = "El umbral en kilómetros es obligatorio")
	@Positive(message = "El umbral en kilómetros debe ser mayor que cero")
	private Integer umbralKm;

	@NotNull(message = "El umbral en días es obligatorio")
	@Positive(message = "El umbral en días debe ser mayor que cero")
	private Integer umbralDias;

	@NotNull(message = "Los días para lectura pendiente son obligatorios")
	@Positive(message = "Los días para lectura pendiente deben ser mayores que cero")
	private Integer diasLecturaPendiente;

	static ParametrosAlertasForm desde(ParametrosAlertas parametros) {
		ParametrosAlertasForm form = new ParametrosAlertasForm();
		form.umbralKm = parametros.umbralKm();
		form.umbralDias = parametros.umbralDias();
		form.diasLecturaPendiente = parametros.diasLecturaPendiente();
		return form;
	}
}
