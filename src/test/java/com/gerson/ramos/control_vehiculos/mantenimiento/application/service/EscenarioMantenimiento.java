package com.gerson.ramos.control_vehiculos.mantenimiento.application.service;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

import com.gerson.ramos.control_vehiculos.configuracion.application.service.ConsultarCatalogosService;
import com.gerson.ramos.control_vehiculos.configuracion.application.service.GestionarParametrosMantenimientoService;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.CodigoCatalogo;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.Localidad;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.RutinaMantenimiento;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.TipoUso;
import com.gerson.ramos.control_vehiculos.mantenimiento.application.port.in.RegistrarMantenimientoComando;
import com.gerson.ramos.control_vehiculos.mantenimiento.domain.model.Mantenimiento;
import com.gerson.ramos.control_vehiculos.testsupport.InMemoryLocalidadRepository;
import com.gerson.ramos.control_vehiculos.testsupport.InMemoryMantenimientoRepository;
import com.gerson.ramos.control_vehiculos.testsupport.InMemoryParametrosMantenimientoRepository;
import com.gerson.ramos.control_vehiculos.testsupport.InMemoryRutinaMantenimientoRepository;
import com.gerson.ramos.control_vehiculos.testsupport.InMemoryTipoUsoRepository;
import com.gerson.ramos.control_vehiculos.testsupport.InMemoryVehiculoRepository;
import com.gerson.ramos.control_vehiculos.testsupport.VehiculosEnMemoria;
import com.gerson.ramos.control_vehiculos.vehiculo.domain.model.CodigoEquipo;
import com.gerson.ramos.control_vehiculos.vehiculo.domain.model.Placa;
import com.gerson.ramos.control_vehiculos.vehiculo.domain.model.Vehiculo;

/**
 * Escenario común de los tests de servicios de mantenimiento: repositorios en memoria, reloj fijo en {@link #HOY},
 * un vehículo activo y uno inactivo, y las rutinas 10K y 20K activas y 30K inactiva.
 */
class EscenarioMantenimiento {

	static final LocalDate HOY = LocalDate.of(2026, 11, 21);

	final Clock reloj = Clock.fixed(Instant.parse("2026-11-21T12:00:00Z"), ZoneOffset.UTC);

	final InMemoryVehiculoRepository vehiculosRepo = new InMemoryVehiculoRepository();
	final InMemoryLocalidadRepository localidades = new InMemoryLocalidadRepository();
	final InMemoryTipoUsoRepository tiposUso = new InMemoryTipoUsoRepository();
	final InMemoryRutinaMantenimientoRepository rutinas = new InMemoryRutinaMantenimientoRepository();
	final InMemoryMantenimientoRepository mantenimientosRepo = new InMemoryMantenimientoRepository();
	final InMemoryParametrosMantenimientoRepository parametrosRepo = new InMemoryParametrosMantenimientoRepository();

	final VehiculosEnMemoria vehiculos = new VehiculosEnMemoria(vehiculosRepo, localidades);
	final ConsultarCatalogosService catalogos = new ConsultarCatalogosService(localidades, tiposUso, rutinas);
	final GestionarParametrosMantenimientoService parametros = new GestionarParametrosMantenimientoService(parametrosRepo);

	final RegistrarMantenimientoService registrar = new RegistrarMantenimientoService(mantenimientosRepo, vehiculos,
			vehiculos, catalogos, reloj);
	final ConsultarHistorialService historial = new ConsultarHistorialService(mantenimientosRepo, vehiculos, catalogos);
	final ConsultarProgramacionService programacion = new ConsultarProgramacionService(mantenimientosRepo, vehiculos,
			catalogos, parametros, reloj);
	final ConsultarMantenimientosRealizadosService realizados = new ConsultarMantenimientosRealizadosService(mantenimientosRepo,
			vehiculos, catalogos);

	final Vehiculo vehiculoActivo;
	final Vehiculo vehiculoInactivo;
	final RutinaMantenimiento rutina10k;
	final RutinaMantenimiento rutina20k;
	final RutinaMantenimiento rutina30kInactiva;

	EscenarioMantenimiento() {
		Localidad sps = localidades.guardar(Localidad.crear(new CodigoCatalogo("SPS"), "San Pedro Sula"));
		TipoUso operativo = tiposUso.guardar(TipoUso.crear(new CodigoCatalogo("OPERATIVO"), "Operativo"));

		rutina10k = rutinas.guardar(RutinaMantenimiento.crear(new CodigoCatalogo("10K"), "Preventivo 10K"));
		rutina20k = rutinas.guardar(RutinaMantenimiento.crear(new CodigoCatalogo("20K"), "Preventivo 20K"));
		RutinaMantenimiento r30k = RutinaMantenimiento.crear(new CodigoCatalogo("30K"), "Preventivo 30K");
		r30k.desactivar();
		rutina30kInactiva = rutinas.guardar(r30k);

		vehiculoActivo = vehiculosRepo.guardar(
				Vehiculo.registrar(new Placa("ABC-123"), new CodigoEquipo("EQ-001"), sps.id(), operativo.id()));
		Vehiculo inactivo = Vehiculo.registrar(new Placa("XYZ-999"), new CodigoEquipo("EQ-002"), sps.id(), operativo.id());
		inactivo.desactivar();
		vehiculoInactivo = vehiculosRepo.guardar(inactivo);
	}

	long idVehiculo() {
		return vehiculoActivo.id().valor();
	}

	RegistrarMantenimientoComando comando(LocalDate fecha, int kilometraje, RutinaMantenimiento rutina) {
		return new RegistrarMantenimientoComando(idVehiculo(), fecha, kilometraje, rutina.id().valor(), null);
	}

	/** Registra un mantenimiento válido para el vehículo activo. */
	Mantenimiento registrarMantenimiento(LocalDate fecha, int kilometraje, RutinaMantenimiento rutina) {
		return registrar.registrar(comando(fecha, kilometraje, rutina));
	}
}
