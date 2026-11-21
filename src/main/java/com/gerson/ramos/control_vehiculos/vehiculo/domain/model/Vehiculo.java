package com.gerson.ramos.control_vehiculos.vehiculo.domain.model;

import org.jspecify.annotations.Nullable;

import com.gerson.ramos.control_vehiculos.configuracion.domain.model.LocalidadId;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.RutinaMantenimientoId;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.TipoUsoId;
import com.gerson.ramos.control_vehiculos.shared.domain.ReglaDeNegocioException;
import com.gerson.ramos.control_vehiculos.shared.domain.Validaciones;

/**
 * Agregado raíz del módulo de vehículos. Se identifica por placa y código de equipo (ambos únicos),
 * pertenece a una localidad y tiene un tipo de uso. "Desactivar" es una baja lógica: un vehículo inactivo
 * no puede modificarse hasta reactivarse.
 *
 * <p>Las referencias a otros agregados (localidad, tipo de uso, rutina) son por id, nunca por objeto.
 * {@code version} es la versión de concurrencia optimista; es nula mientras el vehículo no se ha persistido.
 */
public final class Vehiculo {

	private final @Nullable VehiculoId id;
	private Placa placa;
	private CodigoEquipo codigoEquipo;
	private LocalidadId localidadId;
	private TipoUsoId tipoUsoId;
	private @Nullable RutinaMantenimientoId ultimaRutinaId;
	private boolean activo;
	private final @Nullable Long version;

	private Vehiculo(@Nullable VehiculoId id, Placa placa, CodigoEquipo codigoEquipo, LocalidadId localidadId,
			TipoUsoId tipoUsoId, @Nullable RutinaMantenimientoId ultimaRutinaId, boolean activo,
			@Nullable Long version) {
		this.id = id;
		this.placa = Validaciones.requerir(placa, "La placa");
		this.codigoEquipo = Validaciones.requerir(codigoEquipo, "El código de equipo");
		this.localidadId = Validaciones.requerir(localidadId, "La localidad");
		this.tipoUsoId = Validaciones.requerir(tipoUsoId, "El tipo de uso");
		this.ultimaRutinaId = ultimaRutinaId;
		this.activo = activo;
		this.version = version;
	}

	/** Vehículo nuevo, sin id (lo asigna la persistencia), activo y sin rutina de mantenimiento registrada. */
	public static Vehiculo registrar(Placa placa, CodigoEquipo codigoEquipo, LocalidadId localidadId,
			TipoUsoId tipoUsoId) {
		return new Vehiculo(null, placa, codigoEquipo, localidadId, tipoUsoId, null, true, null);
	}

	/** Reconstruye un vehículo ya persistido. */
	public static Vehiculo reconstruir(VehiculoId id, Placa placa, CodigoEquipo codigoEquipo, LocalidadId localidadId,
			TipoUsoId tipoUsoId, @Nullable RutinaMantenimientoId ultimaRutinaId, boolean activo, long version) {
		return new Vehiculo(Validaciones.requerir(id, "El id"), placa, codigoEquipo, localidadId, tipoUsoId,
				ultimaRutinaId, activo, version);
	}

	public void corregirIdentificacion(Placa nuevaPlaca, CodigoEquipo nuevoCodigoEquipo) {
		asegurarActivo();
		this.placa = Validaciones.requerir(nuevaPlaca, "La placa");
		this.codigoEquipo = Validaciones.requerir(nuevoCodigoEquipo, "El código de equipo");
	}

	public void reasignarLocalidad(LocalidadId nuevaLocalidadId) {
		asegurarActivo();
		this.localidadId = Validaciones.requerir(nuevaLocalidadId, "La localidad");
	}

	public void cambiarTipoUso(TipoUsoId nuevoTipoUsoId) {
		asegurarActivo();
		this.tipoUsoId = Validaciones.requerir(nuevoTipoUsoId, "El tipo de uso");
	}

	/** Registra la última rutina preventiva realizada (10K, 20K o 30K). */
	public void registrarRutinaRealizada(RutinaMantenimientoId rutinaId) {
		asegurarActivo();
		this.ultimaRutinaId = Validaciones.requerir(rutinaId, "La rutina de mantenimiento");
	}

	public void desactivar() {
		this.activo = false;
	}

	public void activar() {
		this.activo = true;
	}

	private void asegurarActivo() {
		if (!activo) {
			throw new ReglaDeNegocioException("El vehículo " + placa.valor() + " está inactivo y no puede modificarse");
		}
	}

	public @Nullable VehiculoId id() {
		return id;
	}

	public Placa placa() {
		return placa;
	}

	public CodigoEquipo codigoEquipo() {
		return codigoEquipo;
	}

	public LocalidadId localidadId() {
		return localidadId;
	}

	public TipoUsoId tipoUsoId() {
		return tipoUsoId;
	}

	public @Nullable RutinaMantenimientoId ultimaRutinaId() {
		return ultimaRutinaId;
	}

	public boolean activo() {
		return activo;
	}

	public @Nullable Long version() {
		return version;
	}
}
