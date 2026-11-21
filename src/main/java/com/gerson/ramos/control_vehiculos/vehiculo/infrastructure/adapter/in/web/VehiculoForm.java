package com.gerson.ramos.control_vehiculos.vehiculo.infrastructure.adapter.in.web;

import com.gerson.ramos.control_vehiculos.vehiculo.domain.model.Vehiculo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Formulario de alta/edición de vehículos. Es una clase con getters/setters (no un record) para que el enlace de
 * datos y {@code th:field} de Thymeleaf funcionen con propiedades JavaBean estándar.
 */
@Getter
@Setter
public class VehiculoForm {

	/** Versión que el usuario leyó (bloqueo optimista); nula en el alta. */
	private Long version;

	@NotBlank(message = "La placa es obligatoria")
	@Size(max = 10, message = "La placa no puede exceder 10 caracteres")
	private String placa;

	@NotBlank(message = "El código de equipo es obligatorio")
	@Size(max = 30, message = "El código de equipo no puede exceder 30 caracteres")
	private String codigoEquipo;

	@NotNull(message = "Selecciona una localidad")
	private Long localidadId;

	@NotNull(message = "Selecciona un tipo de uso")
	private Long tipoUsoId;

	private Long rutinaId;

	static VehiculoForm vacio() {
		return new VehiculoForm();
	}

	static VehiculoForm desde(Vehiculo vehiculo) {
		VehiculoForm form = new VehiculoForm();
		form.version = vehiculo.version();
		form.placa = vehiculo.placa().valor();
		form.codigoEquipo = vehiculo.codigoEquipo().valor();
		form.localidadId = vehiculo.localidadId().valor();
		form.tipoUsoId = vehiculo.tipoUsoId().valor();
		form.rutinaId = vehiculo.ultimaRutinaId() == null ? null : vehiculo.ultimaRutinaId().valor();
		return form;
	}
}
