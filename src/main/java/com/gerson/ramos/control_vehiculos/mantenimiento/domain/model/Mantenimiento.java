package com.gerson.ramos.control_vehiculos.mantenimiento.domain.model;

import java.time.LocalDate;
import java.util.List;

import org.jspecify.annotations.Nullable;

import com.gerson.ramos.control_vehiculos.configuracion.domain.model.RutinaMantenimientoId;
import com.gerson.ramos.control_vehiculos.shared.domain.ConflictoDeDatosException;
import com.gerson.ramos.control_vehiculos.shared.domain.Validaciones;
import com.gerson.ramos.control_vehiculos.vehiculo.domain.model.VehiculoId;

/**
 * Registro de un mantenimiento preventivo realizado a un vehículo: cuándo (fecha), con qué lectura de odómetro
 * (kilometraje) y qué rutina (10K, 20K, 30K). Es un hecho histórico: no se modifica una vez registrado.
 *
 * <p>Los errores atribuibles a un campo del formulario se lanzan como {@link ConflictoDeDatosException} con el
 * nombre del campo.
 */
public final class Mantenimiento {

	public static final int LONGITUD_MAXIMA_OBSERVACIONES = 500;

	private final @Nullable MantenimientoId id;
	private final VehiculoId vehiculoId;
	private final RutinaMantenimientoId rutinaId;
	private final LocalDate fecha;
	private final int kilometraje;
	private final @Nullable String observaciones;

	private Mantenimiento(@Nullable MantenimientoId id, VehiculoId vehiculoId, RutinaMantenimientoId rutinaId,
			LocalDate fecha, int kilometraje, @Nullable String observaciones) {
		this.id = id;
		this.vehiculoId = Validaciones.requerir(vehiculoId, "El vehículo");
		this.rutinaId = Validaciones.requerir(rutinaId, "La rutina");
		this.fecha = Validaciones.requerir(fecha, "La fecha");
		if (kilometraje < 0) {
			throw new ConflictoDeDatosException("kilometraje", "El kilometraje no puede ser negativo");
		}
		this.kilometraje = kilometraje;
		this.observaciones = normalizar(observaciones);
	}

	/**
	 * Mantenimiento nuevo (sin id). La fecha no puede ser posterior a {@code hoy}.
	 *
	 * @param hoy fecha actual, recibida como parámetro para que el dominio no dependa del reloj del sistema
	 */
	public static Mantenimiento registrar(VehiculoId vehiculoId, RutinaMantenimientoId rutinaId, LocalDate fecha,
			int kilometraje, @Nullable String observaciones, LocalDate hoy) {
		if (Validaciones.requerir(fecha, "La fecha").isAfter(Validaciones.requerir(hoy, "La fecha actual"))) {
			throw new ConflictoDeDatosException("fecha", "La fecha del mantenimiento no puede ser futura");
		}
		return new Mantenimiento(null, vehiculoId, rutinaId, fecha, kilometraje, observaciones);
	}

	/** Reconstruye un mantenimiento ya persistido. */
	public static Mantenimiento reconstruir(MantenimientoId id, VehiculoId vehiculoId, RutinaMantenimientoId rutinaId,
			LocalDate fecha, int kilometraje, @Nullable String observaciones) {
		return new Mantenimiento(Validaciones.requerir(id, "El id"), vehiculoId, rutinaId, fecha, kilometraje,
				observaciones);
	}

	/**
	 * El odómetro solo crece con el tiempo: este mantenimiento no puede tener menos km que uno anterior (o de la
	 * misma fecha) ni más km que uno posterior (o de la misma fecha) del mismo vehículo.
	 */
	public void verificarCoherenciaDeOdometro(List<Mantenimiento> historialDelVehiculo) {
		for (Mantenimiento otro : historialDelVehiculo) {
			if (!otro.fecha.isAfter(fecha) && otro.kilometraje > kilometraje) {
				throw new ConflictoDeDatosException("kilometraje", "El kilometraje es menor que el registrado el "
						+ otro.fecha + " (" + otro.kilometraje + " km)");
			}
			if (!otro.fecha.isBefore(fecha) && otro.kilometraje < kilometraje) {
				throw new ConflictoDeDatosException("kilometraje", "El kilometraje es mayor que el registrado el "
						+ otro.fecha + " (" + otro.kilometraje + " km), que es posterior o de la misma fecha");
			}
		}
	}

	/** Más reciente = mayor fecha; con la misma fecha, mayor id (un mantenimiento sin id aún es el más nuevo). */
	public boolean esMasRecienteQue(Mantenimiento otro) {
		int porFecha = fecha.compareTo(otro.fecha);
		if (porFecha != 0) {
			return porFecha > 0;
		}
		return ordenDeRegistro() > otro.ordenDeRegistro();
	}

	private long ordenDeRegistro() {
		return id == null ? Long.MAX_VALUE : id.valor();
	}

	private static @Nullable String normalizar(@Nullable String observaciones) {
		if (observaciones == null || observaciones.isBlank()) {
			return null;
		}
		return Validaciones.requerirTexto(observaciones, "Las observaciones", LONGITUD_MAXIMA_OBSERVACIONES);
	}

	public @Nullable MantenimientoId id() {
		return id;
	}

	public VehiculoId vehiculoId() {
		return vehiculoId;
	}

	public RutinaMantenimientoId rutinaId() {
		return rutinaId;
	}

	public LocalDate fecha() {
		return fecha;
	}

	public int kilometraje() {
		return kilometraje;
	}

	public @Nullable String observaciones() {
		return observaciones;
	}
}
