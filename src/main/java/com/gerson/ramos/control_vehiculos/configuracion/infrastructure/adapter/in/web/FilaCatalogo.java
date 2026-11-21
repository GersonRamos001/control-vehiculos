package com.gerson.ramos.control_vehiculos.configuracion.infrastructure.adapter.in.web;

/** Fila de un catálogo simple (código + nombre) lista para la plantilla, con sus URLs ya construidas. */
public record FilaCatalogo(long id, String codigo, String nombre, boolean activo, String urlEditar, String urlCambioEstado) {

	static FilaCatalogo de(String base, long id, String codigo, String nombre, boolean activo) {
		return new FilaCatalogo(id, codigo, nombre, activo, base + "/" + id + "/editar",
				base + "/" + id + (activo ? "/desactivar" : "/activar"));
	}
}
