package com.gerson.ramos.control_vehiculos.mantenimiento.application.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gerson.ramos.control_vehiculos.configuracion.application.port.in.ConsultarCatalogosUseCase;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.RutinaMantenimiento;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.RutinaMantenimientoId;
import com.gerson.ramos.control_vehiculos.mantenimiento.application.port.in.ConsultarHistorialUseCase;
import com.gerson.ramos.control_vehiculos.mantenimiento.application.port.in.MantenimientoDetalle;
import com.gerson.ramos.control_vehiculos.mantenimiento.application.port.out.MantenimientoRepositoryPort;
import com.gerson.ramos.control_vehiculos.mantenimiento.domain.model.Mantenimiento;
import com.gerson.ramos.control_vehiculos.vehiculo.application.port.in.ConsultarVehiculosUseCase;
import com.gerson.ramos.control_vehiculos.vehiculo.domain.model.Vehiculo;

@Service
@Transactional(readOnly = true)
public class ConsultarHistorialService implements ConsultarHistorialUseCase {

	private final MantenimientoRepositoryPort mantenimientos;
	private final ConsultarVehiculosUseCase vehiculos;
	private final ConsultarCatalogosUseCase catalogos;

	ConsultarHistorialService(MantenimientoRepositoryPort mantenimientos, ConsultarVehiculosUseCase vehiculos,
			ConsultarCatalogosUseCase catalogos) {
		this.mantenimientos = mantenimientos;
		this.vehiculos = vehiculos;
		this.catalogos = catalogos;
	}

	@Override
	public List<MantenimientoDetalle> historialDe(long vehiculoId) {
		Vehiculo vehiculo = vehiculos.buscarPorId(vehiculoId);
		Map<RutinaMantenimientoId, String> rutinas = new HashMap<>();
		for (RutinaMantenimiento rutina : catalogos.listarRutinas(false)) {
			rutinas.put(rutina.id(), rutina.codigo().valor());
		}
		return mantenimientos.historialDe(vehiculo.id()).stream().map(m -> aDetalle(m, rutinas)).toList();
	}

	private static MantenimientoDetalle aDetalle(Mantenimiento mantenimiento, Map<RutinaMantenimientoId, String> rutinas) {
		return new MantenimientoDetalle(mantenimiento.id().valor(), mantenimiento.fecha(), mantenimiento.kilometraje(),
				mantenimiento.rutinaId().valor(), rutinas.getOrDefault(mantenimiento.rutinaId(), "?"),
				mantenimiento.observaciones());
	}
}
