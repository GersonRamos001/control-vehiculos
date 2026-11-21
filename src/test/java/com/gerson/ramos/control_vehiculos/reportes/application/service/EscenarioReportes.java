package com.gerson.ramos.control_vehiculos.reportes.application.service;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

import org.jspecify.annotations.Nullable;

import com.gerson.ramos.control_vehiculos.alertas.application.port.in.AlertaDetalle;
import com.gerson.ramos.control_vehiculos.alertas.domain.model.Severidad;
import com.gerson.ramos.control_vehiculos.alertas.domain.model.TipoAlerta;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.CodigoCatalogo;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.Localidad;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.TipoUso;
import com.gerson.ramos.control_vehiculos.kilometraje.application.port.in.FilaSeguimiento;
import com.gerson.ramos.control_vehiculos.mantenimiento.application.port.in.MantenimientoRealizado;
import com.gerson.ramos.control_vehiculos.testsupport.AlertasEnMemoria;
import com.gerson.ramos.control_vehiculos.testsupport.InMemoryLocalidadRepository;
import com.gerson.ramos.control_vehiculos.testsupport.InMemoryTipoUsoRepository;
import com.gerson.ramos.control_vehiculos.testsupport.InMemoryVehiculoRepository;
import com.gerson.ramos.control_vehiculos.testsupport.MantenimientosRealizadosEnMemoria;
import com.gerson.ramos.control_vehiculos.testsupport.SeguimientoEnMemoria;
import com.gerson.ramos.control_vehiculos.testsupport.VehiculosEnMemoria;
import com.gerson.ramos.control_vehiculos.vehiculo.domain.model.CodigoEquipo;
import com.gerson.ramos.control_vehiculos.vehiculo.domain.model.Placa;
import com.gerson.ramos.control_vehiculos.vehiculo.domain.model.Vehiculo;

/**
 * Escenario común de los tests de reportes: reloj fijo en {@link #HOY}, dos localidades (San Pedro Sula y Tegucigalpa),
 * dos tipos de uso y los puertos de otros módulos simulados. Cada test crea los vehículos que necesita.
 */
class EscenarioReportes {

	static final LocalDate HOY = LocalDate.of(2026, 11, 21);

	final Clock reloj = Clock.fixed(Instant.parse("2026-11-21T12:00:00Z"), ZoneOffset.UTC);

	final InMemoryVehiculoRepository vehiculosRepo = new InMemoryVehiculoRepository();
	final InMemoryLocalidadRepository localidadesRepo = new InMemoryLocalidadRepository();
	final InMemoryTipoUsoRepository tiposUsoRepo = new InMemoryTipoUsoRepository();

	final VehiculosEnMemoria vehiculos = new VehiculosEnMemoria(vehiculosRepo, localidadesRepo);
	final SeguimientoEnMemoria seguimiento = new SeguimientoEnMemoria();
	final AlertasEnMemoria alertas = new AlertasEnMemoria();
	final MantenimientosRealizadosEnMemoria realizados = new MantenimientosRealizadosEnMemoria();

	final ReportePendientesService pendientes = new ReportePendientesService(alertas, seguimiento, vehiculos, reloj);
	final ReporteRealizadosService reporteRealizados = new ReporteRealizadosService(realizados, reloj);
	final ReporteVencimientosService vencimientos = new ReporteVencimientosService(seguimiento, vehiculos, reloj);

	final Localidad sanPedro = localidadesRepo.guardar(Localidad.crear(new CodigoCatalogo("SPS"), "San Pedro Sula"));
	final Localidad tegucigalpa = localidadesRepo.guardar(Localidad.crear(new CodigoCatalogo("TGU"), "Tegucigalpa"));
	final TipoUso operativo = tiposUsoRepo.guardar(TipoUso.crear(new CodigoCatalogo("OPERATIVO"), "Operativo"));
	final TipoUso relevo = tiposUsoRepo.guardar(TipoUso.crear(new CodigoCatalogo("RELEVO"), "Relevo"));

	Vehiculo vehiculo(String placa, String codigoEquipo, Localidad localidad, TipoUso tipoUso) {
		return vehiculosRepo.guardar(Vehiculo.registrar(new Placa(placa), new CodigoEquipo(codigoEquipo), localidad.id(), tipoUso.id()));
	}

	Vehiculo vehiculoInactivo(String placa, String codigoEquipo, Localidad localidad, TipoUso tipoUso) {
		Vehiculo vehiculo = Vehiculo.registrar(new Placa(placa), new CodigoEquipo(codigoEquipo), localidad.id(), tipoUso.id());
		vehiculo.desactivar();
		return vehiculosRepo.guardar(vehiculo);
	}

	/** Fila de seguimiento con mantenimiento (último a 100,000 km el 2026-05-01, próximo a 110,000) o sin él. */
	void seguimientoDe(Vehiculo vehiculo, Localidad localidad, @Nullable Long kmRestantes, @Nullable Long diasRestantes,
			boolean conMantenimiento) {
		seguimiento.con(new FilaSeguimiento(vehiculo.id().valor(), vehiculo.placa().valor(), vehiculo.codigoEquipo().valor(),
				localidad.nombre(), conMantenimiento ? LocalDate.of(2026, 5, 1) : null,
				conMantenimiento ? Integer.valueOf(100_000) : null, conMantenimiento ? Long.valueOf(110_000) : null, diasRestantes,
				LocalDate.of(2026, 11, 19), 107_500, 2L, conMantenimiento ? Long.valueOf(107_500) : null, kmRestantes, false));
	}

	void alertaDe(Vehiculo vehiculo, Localidad localidad, Severidad severidad, String mensaje, @Nullable Long kmRestantes,
			@Nullable Long diasRestantes) {
		TipoAlerta tipo = switch (severidad) {
			case VENCIDA -> TipoAlerta.MANTENIMIENTO_VENCIDO;
			case PROXIMA -> TipoAlerta.MANTENIMIENTO_PROXIMO;
			case INFORMATIVA -> TipoAlerta.LECTURA_PENDIENTE;
		};
		alertas.con(new AlertaDetalle(vehiculo.id().valor(), vehiculo.placa().valor(), vehiculo.codigoEquipo().valor(),
				localidad.nombre(), tipo, severidad, mensaje, kmRestantes, diasRestantes));
	}

	/** Mantenimiento ya realizado (fila del historial de la flota). */
	static MantenimientoRealizado realizado(long id, String placa, Localidad localidad, LocalDate fecha, int kilometraje,
			long rutinaId, String rutina, @Nullable String observaciones) {
		return new MantenimientoRealizado(id, id, placa, "EQ-" + id, localidad.id().valor(), localidad.nombre(), fecha, kilometraje,
				rutinaId, rutina, observaciones);
	}
}
