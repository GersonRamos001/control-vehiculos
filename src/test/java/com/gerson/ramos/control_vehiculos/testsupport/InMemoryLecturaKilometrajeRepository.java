package com.gerson.ramos.control_vehiculos.testsupport;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.dao.DataIntegrityViolationException;

import com.gerson.ramos.control_vehiculos.kilometraje.application.port.out.LecturaKilometrajeRepositoryPort;
import com.gerson.ramos.control_vehiculos.kilometraje.domain.model.LecturaId;
import com.gerson.ramos.control_vehiculos.kilometraje.domain.model.LecturaKilometraje;
import com.gerson.ramos.control_vehiculos.shared.domain.RecursoNoEncontradoException;
import com.gerson.ramos.control_vehiculos.vehiculo.domain.model.VehiculoId;

/** Implementación en memoria del puerto; imita la restricción única (vehículo, fecha) de la base de datos. */
public class InMemoryLecturaKilometrajeRepository implements LecturaKilometrajeRepositoryPort {

	private static final Comparator<LecturaKilometraje> MAS_RECIENTE_PRIMERO = Comparator
		.comparing(LecturaKilometraje::fecha).reversed();

	private final Map<Long, LecturaKilometraje> datos = new LinkedHashMap<>();
	private long secuencia = 0;

	@Override
	public LecturaKilometraje guardar(LecturaKilometraje lectura) {
		long id;
		if (lectura.id() == null) {
			id = ++secuencia;
		}
		else {
			id = lectura.id().valor();
			if (!datos.containsKey(id)) {
				throw new RecursoNoEncontradoException("Lectura", id);
			}
		}
		boolean fechaRepetida = datos.entrySet().stream().anyMatch(entrada -> entrada.getKey() != id
				&& entrada.getValue().vehiculoId().equals(lectura.vehiculoId()) && entrada.getValue().fecha().equals(lectura.fecha()));
		if (fechaRepetida) {
			throw new DataIntegrityViolationException("uk_lectura_vehiculo_fecha");
		}
		LecturaKilometraje guardada = LecturaKilometraje.reconstruir(new LecturaId(id), lectura.vehiculoId(), lectura.fecha(),
				lectura.kilometraje());
		datos.put(id, guardada);
		return guardada;
	}

	@Override
	public Optional<LecturaKilometraje> buscarPorVehiculoYFecha(VehiculoId vehiculoId, LocalDate fecha) {
		return datos.values().stream().filter(l -> l.vehiculoId().equals(vehiculoId) && l.fecha().equals(fecha)).findFirst();
	}

	@Override
	public List<LecturaKilometraje> historialDe(VehiculoId vehiculoId) {
		return datos.values().stream().filter(l -> l.vehiculoId().equals(vehiculoId)).sorted(MAS_RECIENTE_PRIMERO).toList();
	}

	@Override
	public Optional<LecturaKilometraje> ultimaDe(VehiculoId vehiculoId) {
		return historialDe(vehiculoId).stream().findFirst();
	}

	@Override
	public List<LecturaKilometraje> ultimasDeTodosLosVehiculos() {
		Map<VehiculoId, LecturaKilometraje> ultimas = new LinkedHashMap<>();
		for (LecturaKilometraje lectura : datos.values()) {
			ultimas.merge(lectura.vehiculoId(), lectura, (a, b) -> b.fecha().isAfter(a.fecha()) ? b : a);
		}
		return List.copyOf(ultimas.values());
	}

	@Override
	public void eliminar(LecturaId id) {
		datos.remove(id.valor());
	}
}
