package com.gerson.ramos.control_vehiculos.mantenimiento.application.service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gerson.ramos.control_vehiculos.configuracion.application.port.in.ConsultarCatalogosUseCase;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.RutinaMantenimiento;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.RutinaMantenimientoId;
import com.gerson.ramos.control_vehiculos.mantenimiento.application.port.in.ConsultarMantenimientosRealizadosUseCase;
import com.gerson.ramos.control_vehiculos.mantenimiento.application.port.in.MantenimientoRealizado;
import com.gerson.ramos.control_vehiculos.mantenimiento.application.port.out.MantenimientoRepositoryPort;
import com.gerson.ramos.control_vehiculos.mantenimiento.domain.model.Mantenimiento;
import com.gerson.ramos.control_vehiculos.shared.domain.ReglaDeNegocioException;
import com.gerson.ramos.control_vehiculos.shared.domain.Validaciones;
import com.gerson.ramos.control_vehiculos.vehiculo.application.port.in.ConsultarVehiculosUseCase;
import com.gerson.ramos.control_vehiculos.vehiculo.application.port.in.FiltroEstado;
import com.gerson.ramos.control_vehiculos.vehiculo.application.port.in.VehiculoDetalle;

@Service
@Transactional(readOnly = true)
public class ConsultarMantenimientosRealizadosService implements ConsultarMantenimientosRealizadosUseCase {

	private final MantenimientoRepositoryPort mantenimientos;
	private final ConsultarVehiculosUseCase vehiculos;
	private final ConsultarCatalogosUseCase catalogos;

	ConsultarMantenimientosRealizadosService(MantenimientoRepositoryPort mantenimientos, ConsultarVehiculosUseCase vehiculos,
			ConsultarCatalogosUseCase catalogos) {
		this.mantenimientos = mantenimientos;
		this.vehiculos = vehiculos;
		this.catalogos = catalogos;
	}

	@Override
	public List<MantenimientoRealizado> entre(LocalDate desde, LocalDate hasta) {
		if (Validaciones.requerir(desde, "La fecha inicial").isAfter(Validaciones.requerir(hasta, "La fecha final"))) {
			throw new ReglaDeNegocioException("La fecha inicial no puede ser posterior a la final");
		}

		// Se incluyen los vehículos inactivos: el historial de lo que se les hizo no se pierde al darlos de baja.
		Map<Long, VehiculoDetalle> flota = new HashMap<>();
		for (VehiculoDetalle vehiculo : vehiculos.listar(FiltroEstado.TODOS)) {
			flota.put(vehiculo.id(), vehiculo);
		}
		Map<RutinaMantenimientoId, String> rutinas = new HashMap<>();
		for (RutinaMantenimiento rutina : catalogos.listarRutinas(false)) {
			rutinas.put(rutina.id(), rutina.codigo().valor());
		}

		return mantenimientos.realizadosEntre(desde, hasta).stream().map(m -> aDetalle(m, flota, rutinas)).toList();
	}

	private static MantenimientoRealizado aDetalle(Mantenimiento mantenimiento, Map<Long, VehiculoDetalle> flota,
			Map<RutinaMantenimientoId, String> rutinas) {
		VehiculoDetalle vehiculo = flota.get(mantenimiento.vehiculoId().valor());
		return new MantenimientoRealizado(mantenimiento.id().valor(), mantenimiento.vehiculoId().valor(),
				vehiculo == null ? "?" : vehiculo.placa(), vehiculo == null ? "?" : vehiculo.codigoEquipo(),
				vehiculo == null ? 0 : vehiculo.localidadId(), vehiculo == null ? "?" : vehiculo.localidad(), mantenimiento.fecha(),
				mantenimiento.kilometraje(), mantenimiento.rutinaId().valor(),
				rutinas.getOrDefault(mantenimiento.rutinaId(), "?"), mantenimiento.observaciones());
	}
}
