package com.gerson.ramos.control_vehiculos.configuracion.domain.model;

import org.jspecify.annotations.Nullable;

import com.gerson.ramos.control_vehiculos.shared.domain.Validaciones;

/** Tipo de uso de un vehículo (OPERATIVO, RELEVO). Catálogo administrable. */
public final class TipoUso {

	static final int LONGITUD_MAXIMA_NOMBRE = 100;

	private final @Nullable TipoUsoId id;
	private final CodigoCatalogo codigo;
	private String nombre;
	private boolean activo;

	private TipoUso(@Nullable TipoUsoId id, CodigoCatalogo codigo, String nombre, boolean activo) {
		this.id = id;
		this.codigo = Validaciones.requerir(codigo, "El código");
		this.nombre = Validaciones.requerirTexto(nombre, "El nombre", LONGITUD_MAXIMA_NOMBRE);
		this.activo = activo;
	}

	/** Tipo de uso nuevo, aún sin id (lo asigna la persistencia). Nace activo. */
	public static TipoUso crear(CodigoCatalogo codigo, String nombre) {
		return new TipoUso(null, codigo, nombre, true);
	}

	/** Reconstruye un tipo de uso ya persistido. */
	public static TipoUso reconstruir(TipoUsoId id, CodigoCatalogo codigo, String nombre, boolean activo) {
		return new TipoUso(Validaciones.requerir(id, "El id"), codigo, nombre, activo);
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

	public @Nullable TipoUsoId id() {
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
