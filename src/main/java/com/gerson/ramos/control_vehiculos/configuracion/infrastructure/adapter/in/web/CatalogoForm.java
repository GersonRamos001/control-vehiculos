package com.gerson.ramos.control_vehiculos.configuracion.infrastructure.adapter.in.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/** Formulario de catálogos simples (localidades y tipos de uso). El código solo se edita al crear. */
@Getter
@Setter
public class CatalogoForm {

	@NotBlank(message = "El código es obligatorio")
	@Size(max = 30, message = "El código no puede exceder 30 caracteres")
	private String codigo;

	@NotBlank(message = "El nombre es obligatorio")
	@Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
	private String nombre;

	static CatalogoForm vacio() {
		return new CatalogoForm();
	}

	static CatalogoForm de(String codigo, String nombre) {
		CatalogoForm form = new CatalogoForm();
		form.codigo = codigo;
		form.nombre = nombre;
		return form;
	}
}
