package com.gerson.ramos.control_vehiculos.testsupport;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.gerson.ramos.control_vehiculos.mantenimiento.application.port.out.MantenimientoRepositoryPort;
import com.gerson.ramos.control_vehiculos.mantenimiento.domain.model.Mantenimiento;
import com.gerson.ramos.control_vehiculos.mantenimiento.domain.model.MantenimientoId;
import com.gerson.ramos.control_vehiculos.shared.domain.ReglaDeNegocioException;
import com.gerson.ramos.control_vehiculos.vehiculo.domain.model.VehiculoId;

/** Implementación en memoria del puerto, para probar servicios sin Spring ni base de datos. Solo agrega registros. */
public class InMemoryMantenimientoRepository implements MantenimientoRepositoryPort {

	private static final Comparator<Mantenimiento> MAS_RECIENTE_PRIMERO = Comparator
		.comparing(Mantenimiento::fecha).thenComparing(m -> m.id().valor()).reversed();

	private final Map<Long, Mantenimiento> datos = new LinkedHashMap<>();
	private long secuencia = 0;

	@Override
	public Mantenimiento guardar(Mantenimiento mantenimiento) {
		if (mantenimiento.id() != null) {
			throw new ReglaDeNegocioException("El historial es de solo agregar");
		}
		long id = ++secuencia;
		Mantenimiento guardado = Mantenimiento.reconstruir(new MantenimientoId(id), mantenimiento.vehiculoId(),
				mantenimiento.rutinaId(), mantenimiento.fecha(), mantenimiento.kilometraje(), mantenimiento.observaciones());
		datos.put(id, guardado);
		return guardado;
	}

	@Override
	public List<Mantenimiento> historialDe(VehiculoId vehiculoId) {
		return datos.values().stream().filter(m -> m.vehiculoId().equals(vehiculoId)).sorted(MAS_RECIENTE_PRIMERO).toList();
	}

	@Override
	public Optional<Mantenimiento> ultimoDe(VehiculoId vehiculoId) {
		return historialDe(vehiculoId).stream().findFirst();
	}

	@Override
	public List<Mantenimiento> realizadosEntre(LocalDate desde, LocalDate hasta) {
		return datos.values().stream()
			.filter(m -> !m.fecha().isBefore(desde) && !m.fecha().isAfter(hasta))
			.sorted(MAS_RECIENTE_PRIMERO)
			.toList();
	}

	@Override
	public List<Mantenimiento> ultimosDeTodosLosVehiculos() {
		Map<VehiculoId, Mantenimiento> ultimos = new LinkedHashMap<>();
		for (Mantenimiento m : new ArrayList<>(datos.values())) {
			ultimos.merge(m.vehiculoId(), m, (a, b) -> b.esMasRecienteQue(a) ? b : a);
		}
		return List.copyOf(ultimos.values());
	}
}
