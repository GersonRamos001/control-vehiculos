package com.gerson.ramos.control_vehiculos.usuarios.infrastructure.adapter.in.web;

import lombok.Getter;
import lombok.Setter;

/** Contraseña nueva con su confirmación; {@code claveActual} solo se usa al cambiar la propia contraseña. */
@Getter
@Setter
public class ClaveForm {

	private String claveActual;

	private String clave;

	private String confirmacion;
}
