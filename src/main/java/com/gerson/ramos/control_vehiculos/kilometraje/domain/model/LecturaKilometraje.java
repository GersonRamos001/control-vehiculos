package com.gerson.ramos.control_vehiculos.kilometraje.domain.model;

import java.time.LocalDate;
import java.util.List;

import org.jspecify.annotations.Nullable;

import com.gerson.ramos.control_vehiculos.shared.domain.ConflictoDeDatosException;
import com.gerson.ramos.control_vehiculos.shared.domain.Validaciones;
import com.gerson.ramos.control_vehiculos.vehiculo.domain.model.VehiculoId;

/**
 * Lectura del odómetro de un vehículo en una fecha (se toma cada semana). Hay a lo sumo una por vehículo y fecha:
 * volver a registrar la misma fecha reemplaza el kilometraje ({@link #conKilometraje(int)}).
 *
 * <p>Los errores atribuibles a un campo del formulario se lanzan como {@link ConflictoDeDatosException} con el
 * nombre del campo.
 */
public final class LecturaKilometraje {

	private final @Nullable LecturaId id;
	private final VehiculoId vehiculoId;
	private final LocalDate fecha;
	private final int kilometraje;

	private LecturaKilometraje(@Nullable LecturaId id, VehiculoId vehiculoId, LocalDate fecha, int kilometraje) {
		this.id = id;
		this.vehiculoId = Validaciones.requerir(vehiculoId, "El vehículo");
		this.fecha = Validaciones.requerir(fecha, "La fecha");
		if (kilometraje < 0) {
			throw new ConflictoDeDatosException("kilometraje", "El kilometraje no puede ser negativo");
		}
		this.kilometraje = kilometraje;
	}

	/**
	 * Lectura nueva (sin id). La fecha no puede ser posterior a {@code hoy}.
	 *
	 * @param hoy fecha actual, recibida como parámetro para que el dominio no dependa del reloj del sistema
	 */
	public static LecturaKilometraje registrar(VehiculoId vehiculoId, LocalDate fecha, int kilometraje, LocalDate hoy) {
		if (Validaciones.requerir(fecha, "La fecha").isAfter(Validaciones.requerir(hoy, "La fecha actual"))) {
			throw new ConflictoDeDatosException("fecha", "La fecha de la lectura no puede ser futura");
		}
		return new LecturaKilometraje(null, vehiculoId, fecha, kilometraje);
	}

	/** Reconstruye una lectura ya persistida. */
	public static LecturaKilometraje reconstruir(LecturaId id, VehiculoId vehiculoId, LocalDate fecha, int kilometraje) {
		return new LecturaKilometraje(Validaciones.requerir(id, "El id"), vehiculoId, fecha, kilometraje);
	}

	/** La misma lectura (mismo id, vehículo y fecha) con otro kilometraje: así se corrige una lectura mal digitada. */
	public LecturaKilometraje conKilometraje(int nuevoKilometraje) {
		return new LecturaKilometraje(id, vehiculoId, fecha, nuevoKilometraje);
	}

	/**
	 * El odómetro solo crece con el tiempo: esta lectura no puede tener menos km que una lectura anterior ni más km que
	 * una posterior del mismo vehículo. Las lecturas de la misma fecha se ignoran (es la que se reemplaza).
	 */
	public void verificarCoherenciaDeOdometro(List<LecturaKilometraje> lecturasDelVehiculo) {
		for (LecturaKilometraje otra : lecturasDelVehiculo) {
			if (otra.fecha.isBefore(fecha) && otra.kilometraje > kilometraje) {
				throw new ConflictoDeDatosException("kilometraje", "El kilometraje es menor que el de la lectura del "
						+ otra.fecha + " (" + otra.kilometraje + " km)");
			}
			if (otra.fecha.isAfter(fecha) && otra.kilometraje < kilometraje) {
				throw new ConflictoDeDatosException("kilometraje", "El kilometraje es mayor que el de la lectura del "
						+ otra.fecha + " (" + otra.kilometraje + " km), que es posterior");
			}
		}
	}

	/** Si la lectura es del día del mantenimiento o posterior, no puede marcar menos km que el mantenimiento. */
	public void verificarContraMantenimiento(LocalDate fechaMantenimiento, int kilometrajeMantenimiento) {
		if (!fecha.isBefore(fechaMantenimiento) && kilometraje < kilometrajeMantenimiento) {
			throw new ConflictoDeDatosException("kilometraje", "El kilometraje es menor que el del último mantenimiento ("
					+ kilometrajeMantenimiento + " km el " + fechaMantenimiento + ")");
		}
	}

	public @Nullable LecturaId id() {
		return id;
	}

	public VehiculoId vehiculoId() {
		return vehiculoId;
	}

	public LocalDate fecha() {
		return fecha;
	}

	public int kilometraje() {
		return kilometraje;
	}
}
