package com.gerson.ramos.control_vehiculos.kilometraje.application.service;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

import com.gerson.ramos.control_vehiculos.configuracion.application.service.GestionarParametrosAlertasService;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.CodigoCatalogo;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.Localidad;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.TipoUso;
import com.gerson.ramos.control_vehiculos.kilometraje.application.port.in.RegistrarLecturaComando;
import com.gerson.ramos.control_vehiculos.kilometraje.application.port.in.ResultadoRegistro;
import com.gerson.ramos.control_vehiculos.testsupport.InMemoryLecturaKilometrajeRepository;
import com.gerson.ramos.control_vehiculos.testsupport.InMemoryLocalidadRepository;
import com.gerson.ramos.control_vehiculos.testsupport.InMemoryParametrosAlertasRepository;
import com.gerson.ramos.control_vehiculos.testsupport.InMemoryTipoUsoRepository;
import com.gerson.ramos.control_vehiculos.testsupport.InMemoryVehiculoRepository;
import com.gerson.ramos.control_vehiculos.testsupport.ProgramacionEnMemoria;
import com.gerson.ramos.control_vehiculos.testsupport.VehiculosEnMemoria;
import com.gerson.ramos.control_vehiculos.vehiculo.domain.model.CodigoEquipo;
import com.gerson.ramos.control_vehiculos.vehiculo.domain.model.Placa;
import com.gerson.ramos.control_vehiculos.vehiculo.domain.model.Vehiculo;

/**
 * Escenario común de los tests de servicios de kilometraje: repositorios en memoria, reloj fijo en {@link #HOY}, la
 * programación de mantenimiento simulada y tres vehículos (dos activos, uno inactivo).
 */
class EscenarioKilometraje {

	static final LocalDate HOY = LocalDate.of(2026, 11, 21);

	final Clock reloj = Clock.fixed(Instant.parse("2026-11-21T12:00:00Z"), ZoneOffset.UTC);

	final InMemoryVehiculoRepository vehiculosRepo = new InMemoryVehiculoRepository();
	final InMemoryLocalidadRepository localidades = new InMemoryLocalidadRepository();
	final InMemoryTipoUsoRepository tiposUso = new InMemoryTipoUsoRepository();
	final InMemoryLecturaKilometrajeRepository lecturasRepo = new InMemoryLecturaKilometrajeRepository();

	final VehiculosEnMemoria vehiculos = new VehiculosEnMemoria(vehiculosRepo, localidades);
	final ProgramacionEnMemoria programacion = new ProgramacionEnMemoria(vehiculos);

	final RegistrarLecturaService registrar = new RegistrarLecturaService(lecturasRepo, vehiculos, programacion, reloj);
	final RegistrarLecturasSemanalesService lote = new RegistrarLecturasSemanalesService(registrar);
	final EliminarUltimaLecturaService eliminarUltima = new EliminarUltimaLecturaService(lecturasRepo, vehiculos);
	final ConsultarLecturasService historial = new ConsultarLecturasService(lecturasRepo, vehiculos);
	final InMemoryParametrosAlertasRepository parametrosAlertasRepo = new InMemoryParametrosAlertasRepository();
	final GestionarParametrosAlertasService parametrosAlertas = new GestionarParametrosAlertasService(parametrosAlertasRepo);
	final ConsultarSeguimientoService seguimiento = new ConsultarSeguimientoService(programacion, lecturasRepo, parametrosAlertas,
			reloj);

	final Vehiculo vehiculoActivo;
	final Vehiculo otroActivo;
	final Vehiculo vehiculoInactivo;

	EscenarioKilometraje() {
		Localidad sps = localidades.guardar(Localidad.crear(new CodigoCatalogo("SPS"), "San Pedro Sula"));
		TipoUso operativo = tiposUso.guardar(TipoUso.crear(new CodigoCatalogo("OPERATIVO"), "Operativo"));

		vehiculoActivo = vehiculosRepo.guardar(
				Vehiculo.registrar(new Placa("ABC-123"), new CodigoEquipo("EQ-001"), sps.id(), operativo.id()));
		otroActivo = vehiculosRepo.guardar(
				Vehiculo.registrar(new Placa("DEF-456"), new CodigoEquipo("EQ-002"), sps.id(), operativo.id()));
		Vehiculo inactivo = Vehiculo.registrar(new Placa("XYZ-999"), new CodigoEquipo("EQ-003"), sps.id(), operativo.id());
		inactivo.desactivar();
		vehiculoInactivo = vehiculosRepo.guardar(inactivo);
	}

	long idActivo() {
		return vehiculoActivo.id().valor();
	}

	long idOtro() {
		return otroActivo.id().valor();
	}

	ResultadoRegistro registrarLectura(long vehiculoId, LocalDate fecha, int kilometraje) {
		return registrar.registrar(new RegistrarLecturaComando(vehiculoId, fecha, kilometraje));
	}

	ResultadoRegistro registrarLectura(LocalDate fecha, int kilometraje) {
		return registrarLectura(idActivo(), fecha, kilometraje);
	}
}
