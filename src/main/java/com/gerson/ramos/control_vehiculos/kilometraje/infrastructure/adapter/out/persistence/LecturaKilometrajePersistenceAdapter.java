package com.gerson.ramos.control_vehiculos.kilometraje.infrastructure.adapter.out.persistence;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.gerson.ramos.control_vehiculos.kilometraje.application.port.out.LecturaKilometrajeRepositoryPort;
import com.gerson.ramos.control_vehiculos.kilometraje.domain.model.LecturaId;
import com.gerson.ramos.control_vehiculos.kilometraje.domain.model.LecturaKilometraje;
import com.gerson.ramos.control_vehiculos.shared.domain.RecursoNoEncontradoException;
import com.gerson.ramos.control_vehiculos.vehiculo.domain.model.VehiculoId;

@Repository
@Transactional(readOnly = true)
public class LecturaKilometrajePersistenceAdapter implements LecturaKilometrajeRepositoryPort {

	private final LecturaKilometrajeJpaRepository repositorio;
	private final LecturaKilometrajePersistenceMapper mapper;

	LecturaKilometrajePersistenceAdapter(LecturaKilometrajeJpaRepository repositorio, LecturaKilometrajePersistenceMapper mapper) {
		this.repositorio = repositorio;
		this.mapper = mapper;
	}

	@Override
	@Transactional
	public LecturaKilometraje guardar(LecturaKilometraje lectura) {
		LecturaId id = lectura.id();
		LecturaKilometrajeJpaEntity entidad;
		if (id == null) {
			entidad = mapper.aNuevaEntidad(lectura);
		}
		else {
			entidad = repositorio.findById(id.valor()).orElseThrow(() -> new RecursoNoEncontradoException("Lectura", id.valor()));
			entidad.actualizarKilometraje(lectura.kilometraje());
		}
		// saveAndFlush: la restricción única (vehículo, fecha) falla aquí y updated_at queda al día.
		return mapper.aDominio(repositorio.saveAndFlush(entidad));
	}

	@Override
	public Optional<LecturaKilometraje> buscarPorVehiculoYFecha(VehiculoId vehiculoId, LocalDate fecha) {
		return repositorio.findByVehiculoIdAndFecha(vehiculoId.valor(), fecha).map(mapper::aDominio);
	}

	@Override
	public List<LecturaKilometraje> historialDe(VehiculoId vehiculoId) {
		return repositorio.findByVehiculoIdOrderByFechaDesc(vehiculoId.valor()).stream().map(mapper::aDominio).toList();
	}

	@Override
	public Optional<LecturaKilometraje> ultimaDe(VehiculoId vehiculoId) {
		return repositorio.findFirstByVehiculoIdOrderByFechaDesc(vehiculoId.valor()).map(mapper::aDominio);
	}

	@Override
	public List<LecturaKilometraje> ultimasDeTodosLosVehiculos() {
		return repositorio.findUltimaDeCadaVehiculo().stream().map(mapper::aDominio).toList();
	}

	@Override
	@Transactional
	public void eliminar(LecturaId id) {
		repositorio.deleteById(id.valor());
		repositorio.flush();
	}
}
