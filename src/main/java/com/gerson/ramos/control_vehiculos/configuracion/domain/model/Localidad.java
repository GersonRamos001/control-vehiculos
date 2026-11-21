package com.gerson.ramos.control_vehiculos.configuracion.domain.model;

import org.jspecify.annotations.Nullable;

import com.gerson.ramos.control_vehiculos.shared.domain.Validaciones;

/** Localidad (sucursal, sede o zona) a la que se asigna un vehículo. Catálogo administrable. */
public final class Localidad {

	static final int LONGITUD_MAXIMA_NOMBRE = 100;

	private final @Nullable LocalidadId id;
	private final CodigoCatalogo codigo;
	private String nombre;
	private boolean activo;

	private Localidad(@Nullable LocalidadId id, CodigoCatalogo codigo, String nombre, boolean activo) {
		this.id = id;
		this.codigo = Validaciones.requerir(codigo, "El código");
		this.nombre = Validaciones.requerirTexto(nombre, "El nombre", LONGITUD_MAXIMA_NOMBRE);
		this.activo = activo;
	}

	/** Localidad nueva, aún sin id (lo asigna la persistencia). Nace activa. */
	public static Localidad crear(CodigoCatalogo codigo, String nombre) {
		return new Localidad(null, codigo, nombre, true);
	}

	/** Reconstruye una localidad ya persistida. */
	public static Localidad reconstruir(LocalidadId id, CodigoCatalogo codigo, String nombre, boolean activo) {
		return new Localidad(Validaciones.requerir(id, "El id"), codigo, nombre, activo);
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

	public @Nullable LocalidadId id() {
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
