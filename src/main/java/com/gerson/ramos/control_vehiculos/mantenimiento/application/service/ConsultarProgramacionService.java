package com.gerson.ramos.control_vehiculos.mantenimiento.application.service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gerson.ramos.control_vehiculos.configuracion.application.port.in.ConsultarCatalogosUseCase;
import com.gerson.ramos.control_vehiculos.configuracion.application.port.in.ConsultarParametrosMantenimientoUseCase;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.Localidad;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.ParametrosMantenimiento;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.RutinaMantenimiento;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.RutinaMantenimientoId;
import com.gerson.ramos.control_vehiculos.mantenimiento.application.port.in.ConsultarProgramacionUseCase;
import com.gerson.ramos.control_vehiculos.mantenimiento.application.port.in.FilaProgramacion;
import com.gerson.ramos.control_vehiculos.mantenimiento.application.port.out.MantenimientoRepositoryPort;
import com.gerson.ramos.control_vehiculos.mantenimiento.domain.model.Mantenimiento;
import com.gerson.ramos.control_vehiculos.mantenimiento.domain.model.ProgramacionMantenimiento;
import com.gerson.ramos.control_vehiculos.vehiculo.application.port.in.ConsultarVehiculosUseCase;
import com.gerson.ramos.control_vehiculos.vehiculo.application.port.in.FiltroEstado;
import com.gerson.ramos.control_vehiculos.vehiculo.application.port.in.VehiculoDetalle;
import com.gerson.ramos.control_vehiculos.vehiculo.domain.model.Vehiculo;
import com.gerson.ramos.control_vehiculos.vehiculo.domain.model.VehiculoId;

@Service
@Transactional(readOnly = true)
public class ConsultarProgramacionService implements ConsultarProgramacionUseCase {

	/** Los vehículos sin mantenimientos registrados van después de todos los que sí tienen programación. */
	private static final Comparator<FilaProgramacion> ORDEN = Comparator
		.comparing((FilaProgramacion fila) -> fila.diasRestantes() == null)
		.thenComparing(fila -> fila.diasRestantes() == null ? 0L : fila.diasRestantes())
		.thenComparing(FilaProgramacion::placa);

	private final MantenimientoRepositoryPort mantenimientos;
	private final ConsultarVehiculosUseCase vehiculos;
	private final ConsultarCatalogosUseCase catalogos;
	private final ConsultarParametrosMantenimientoUseCase parametros;
	private final Clock reloj;

	ConsultarProgramacionService(MantenimientoRepositoryPort mantenimientos, ConsultarVehiculosUseCase vehiculos,
			ConsultarCatalogosUseCase catalogos, ConsultarParametrosMantenimientoUseCase parametros, Clock reloj) {
		this.mantenimientos = mantenimientos;
		this.vehiculos = vehiculos;
		this.catalogos = catalogos;
		this.parametros = parametros;
		this.reloj = reloj;
	}

	@Override
	public List<FilaProgramacion> programacionDeVehiculosActivos() {
		LocalDate hoy = LocalDate.now(reloj);
		ParametrosMantenimiento parametrosVigentes = parametros.obtener();
		Map<RutinaMantenimientoId, String> rutinas = codigosDeRutinas();

		Map<VehiculoId, Mantenimiento> ultimos = new HashMap<>();
		for (Mantenimiento ultimo : mantenimientos.ultimosDeTodosLosVehiculos()) {
			ultimos.put(ultimo.vehiculoId(), ultimo);
		}

		return vehiculos.listar(FiltroEstado.ACTIVOS).stream()
			.map(v -> fila(v.id(), v.placa(), v.codigoEquipo(), v.localidad(), ultimos.get(new VehiculoId(v.id())),
					parametrosVigentes, rutinas, hoy))
			.sorted(ORDEN)
			.toList();
	}

	@Override
	public FilaProgramacion programacionDe(long vehiculoId) {
		Vehiculo vehiculo = vehiculos.buscarPorId(vehiculoId);
		String localidad = catalogos.buscarLocalidad(vehiculo.localidadId()).map(Localidad::nombre).orElse("?");
		@Nullable Mantenimiento ultimo = mantenimientos.ultimoDe(vehiculo.id()).orElse(null);
		return fila(vehiculoId, vehiculo.placa().valor(), vehiculo.codigoEquipo().valor(), localidad, ultimo,
				parametros.obtener(), codigosDeRutinas(), LocalDate.now(reloj));
	}

	private Map<RutinaMantenimientoId, String> codigosDeRutinas() {
		Map<RutinaMantenimientoId, String> rutinas = new HashMap<>();
		for (RutinaMantenimiento rutina : catalogos.listarRutinas(false)) {
			rutinas.put(rutina.id(), rutina.codigo().valor());
		}
		return rutinas;
	}

	private static FilaProgramacion fila(long vehiculoId, String placa, String codigoEquipo, String localidad,
			@Nullable Mantenimiento ultimo, ParametrosMantenimiento parametros,
			Map<RutinaMantenimientoId, String> rutinas, LocalDate hoy) {
		if (ultimo == null) {
			return new FilaProgramacion(vehiculoId, placa, codigoEquipo, localidad, null, null, null, null, null, null);
		}
		ProgramacionMantenimiento programacion = ProgramacionMantenimiento.calcular(ultimo, parametros, hoy);
		return new FilaProgramacion(vehiculoId, placa, codigoEquipo, localidad, ultimo.fecha(), ultimo.kilometraje(),
				rutinas.getOrDefault(ultimo.rutinaId(), "?"), programacion.proximoKm(), programacion.fechaReferencia(),
				programacion.diasRestantes());
	}
}
