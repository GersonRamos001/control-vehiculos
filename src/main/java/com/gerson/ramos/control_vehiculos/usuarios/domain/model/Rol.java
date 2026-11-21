package com.gerson.ramos.control_vehiculos.usuarios.domain.model;

/**
 * Roles fijos del sistema. Los permisos por pantalla se aplican en la capa web (SecurityConfig); aquí solo se
 * describe qué puede hacer cada rol en términos de negocio.
 */
public enum Rol {

	/** Todo, incluida la administración de usuarios y la configuración. */
	ADMINISTRADOR("Administrador"),

	/** Registra vehículos, mantenimientos y lecturas; consulta alertas y reportes. */
	OPERADOR("Operador"),

	/** Solo consulta: lista de vehículos, programación, alertas y reportes. */
	CONSULTA("Consulta");

	private final String etiqueta;

	Rol(String etiqueta) {
		this.etiqueta = etiqueta;
	}

	public String etiqueta() {
		return etiqueta;
	}

	public boolean esAdministrador() {
		return this == ADMINISTRADOR;
	}

	/** Puede registrar y modificar datos operativos (vehículos, mantenimientos, lecturas). */
	public boolean puedeEscribir() {
		return this != CONSULTA;
	}
}
