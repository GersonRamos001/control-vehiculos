package com.gerson.ramos.control_vehiculos.testsupport;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.orm.ObjectOptimisticLockingFailureException;

import com.gerson.ramos.control_vehiculos.vehiculo.application.port.out.VehiculoRepositoryPort;
import com.gerson.ramos.control_vehiculos.vehiculo.domain.model.CodigoEquipo;
import com.gerson.ramos.control_vehiculos.vehiculo.domain.model.Placa;
import com.gerson.ramos.control_vehiculos.vehiculo.domain.model.Vehiculo;
import com.gerson.ramos.control_vehiculos.vehiculo.domain.model.VehiculoId;

/**
 * Implementación en memoria del puerto, para probar servicios sin Spring ni base de datos. Devuelve copias y imita
 * la versión (empieza en 0 y sube con cada actualización) y el bloqueo optimista del adaptador real.
 */
public class InMemoryVehiculoRepository implements VehiculoRepositoryPort {

	private final Map<Long, Vehiculo> datos = new LinkedHashMap<>();
	private long secuencia = 0;

	@Override
	public Vehiculo guardar(Vehiculo vehiculo) {
		long id;
		long version;
		if (vehiculo.id() == null) {
			id = ++secuencia;
			version = 0;
		}
		else {
			id = vehiculo.id().valor();
			Vehiculo existente = datos.get(id);
			if (existente == null || !existente.version().equals(vehiculo.version())) {
				throw new ObjectOptimisticLockingFailureException(Vehiculo.class, id);
			}
			version = existente.version() + 1;
		}
		Vehiculo guardado = Vehiculo.reconstruir(new VehiculoId(id), vehiculo.placa(), vehiculo.codigoEquipo(),
				vehiculo.localidadId(), vehiculo.tipoUsoId(), vehiculo.ultimaRutinaId(), vehiculo.activo(), version);
		datos.put(id, guardado);
		return copia(guardado);
	}

	@Override
	public Optional<Vehiculo> buscarPorId(VehiculoId id) {
		return Optional.ofNullable(datos.get(id.valor())).map(InMemoryVehiculoRepository::copia);
	}

	@Override
	public Optional<Vehiculo> buscarPorPlaca(Placa placa) {
		return datos.values().stream().filter(v -> v.placa().equals(placa)).findFirst().map(InMemoryVehiculoRepository::copia);
	}

	@Override
	public boolean existePorPlaca(Placa placa) {
		return datos.values().stream().anyMatch(v -> v.placa().equals(placa));
	}

	@Override
	public boolean existePorCodigoEquipo(CodigoEquipo codigoEquipo) {
		return datos.values().stream().anyMatch(v -> v.codigoEquipo().equals(codigoEquipo));
	}

	@Override
	public List<Vehiculo> listarActivos() {
		return datos.values().stream().filter(Vehiculo::activo).sorted(porPlaca()).map(InMemoryVehiculoRepository::copia).toList();
	}

	@Override
	public List<Vehiculo> listarTodos() {
		return datos.values().stream().sorted(porPlaca()).map(InMemoryVehiculoRepository::copia).toList();
	}

	private static Comparator<Vehiculo> porPlaca() {
		return Comparator.comparing(v -> v.placa().valor());
	}

	private static Vehiculo copia(Vehiculo v) {
		return Vehiculo.reconstruir(v.id(), v.placa(), v.codigoEquipo(), v.localidadId(), v.tipoUsoId(),
				v.ultimaRutinaId(), v.activo(), v.version());
	}
}
