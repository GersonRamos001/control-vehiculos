package com.gerson.ramos.control_vehiculos.configuracion.application.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gerson.ramos.control_vehiculos.configuracion.application.port.in.ConsultarCatalogosUseCase;
import com.gerson.ramos.control_vehiculos.configuracion.application.port.out.LocalidadRepositoryPort;
import com.gerson.ramos.control_vehiculos.configuracion.application.port.out.RutinaMantenimientoRepositoryPort;
import com.gerson.ramos.control_vehiculos.configuracion.application.port.out.TipoUsoRepositoryPort;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.Localidad;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.LocalidadId;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.RutinaMantenimiento;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.RutinaMantenimientoId;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.TipoUso;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.TipoUsoId;

@Service
@Transactional(readOnly = true)
public class ConsultarCatalogosService implements ConsultarCatalogosUseCase {

	private final LocalidadRepositoryPort localidades;
	private final TipoUsoRepositoryPort tiposUso;
	private final RutinaMantenimientoRepositoryPort rutinas;

	public ConsultarCatalogosService(LocalidadRepositoryPort localidades, TipoUsoRepositoryPort tiposUso,
			RutinaMantenimientoRepositoryPort rutinas) {
		this.localidades = localidades;
		this.tiposUso = tiposUso;
		this.rutinas = rutinas;
	}

	@Override
	public List<Localidad> listarLocalidades(boolean soloActivas) {
		return soloActivas ? localidades.listarActivas() : localidades.listarTodas();
	}

	@Override
	public List<TipoUso> listarTiposUso(boolean soloActivos) {
		return soloActivos ? tiposUso.listarActivos() : tiposUso.listarTodos();
	}

	@Override
	public List<RutinaMantenimiento> listarRutinas(boolean soloActivas) {
		return soloActivas ? rutinas.listarActivas() : rutinas.listarTodas();
	}

	@Override
	public Optional<Localidad> buscarLocalidad(LocalidadId id) {
		return localidades.buscarPorId(id);
	}

	@Override
	public Optional<TipoUso> buscarTipoUso(TipoUsoId id) {
		return tiposUso.buscarPorId(id);
	}

	@Override
	public Optional<RutinaMantenimiento> buscarRutina(RutinaMantenimientoId id) {
		return rutinas.buscarPorId(id);
	}
}
