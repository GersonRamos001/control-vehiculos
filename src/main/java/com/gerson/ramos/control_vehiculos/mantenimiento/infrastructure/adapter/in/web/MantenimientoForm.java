package com.gerson.ramos.control_vehiculos.mantenimiento.infrastructure.adapter.in.web;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/** Formulario de registro de un mantenimiento (el vehículo viene en la URL). */
@Getter
@Setter
public class MantenimientoForm {

	@NotNull(message = "La fecha es obligatoria")
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
	private LocalDate fecha;

	@NotNull(message = "El kilometraje es obligatorio")
	@PositiveOrZero(message = "El kilometraje no puede ser negativo")
	private Integer kilometraje;

	@NotNull(message = "Selecciona la rutina realizada")
	private Long rutinaId;

	@Size(max = 500, message = "Las observaciones no pueden exceder 500 caracteres")
	private String observaciones;

	static MantenimientoForm para(LocalDate fechaInicial) {
		MantenimientoForm form = new MantenimientoForm();
		form.fecha = fechaInicial;
		return form;
	}
}
