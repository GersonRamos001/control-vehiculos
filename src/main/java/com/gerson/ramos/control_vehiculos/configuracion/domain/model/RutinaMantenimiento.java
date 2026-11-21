package com.gerson.ramos.control_vehiculos.configuracion.domain.model;

import org.jspecify.annotations.Nullable;

import com.gerson.ramos.control_vehiculos.shared.domain.Validaciones;

/**
 * Tipo de mantenimiento preventivo realizado (10K, 20K, 30K). Catálogo administrable. Los intervalos de km y meses
 * para programar el siguiente mantenimiento son globales: ver {@link ParametrosMantenimiento}.
 */
public final class RutinaMantenimiento {

	static final int LONGITUD_MAXIMA_NOMBRE = 100;

	private final @Nullable RutinaMantenimientoId id;
	private final CodigoCatalogo codigo;
	private String nombre;
	private boolean activo;

	private RutinaMantenimiento(@Nullable RutinaMantenimientoId id, CodigoCatalogo codigo, String nombre, boolean activo) {
		this.id = id;
		this.codigo = Validaciones.requerir(codigo, "El código");
		this.nombre = Validaciones.requerirTexto(nombre, "El nombre", LONGITUD_MAXIMA_NOMBRE);
		this.activo = activo;
	}

	/** Rutina nueva, aún sin id (lo asigna la persistencia). Nace activa. */
	public static RutinaMantenimiento crear(CodigoCatalogo codigo, String nombre) {
		return new RutinaMantenimiento(null, codigo, nombre, true);
	}

	/** Reconstruye una rutina ya persistida. */
	public static RutinaMantenimiento reconstruir(RutinaMantenimientoId id, CodigoCatalogo codigo, String nombre,
			boolean activo) {
		return new RutinaMantenimiento(Validaciones.requerir(id, "El id"), codigo, nombre, activo);
	}

	public void renombrar(String nuevoNombre) {
		this.nombre = Validaciones.requerirTexto(nuevoNombre, "El nombre", LONGITUD_MAXIMA_NOMBRE);
	}

	public void activar() {
		this.activo = true;
	}

	public void desactivar() {
		this.activo = false;
	}

	public @Nullable RutinaMantenimientoId id() {
		return id;
	}

	public CodigoCatalogo codigo() {
		return codigo;
	}

	public String nombre() {
		return nombre;
	}

	public boolean activo() {
		return activo;
	}
}
