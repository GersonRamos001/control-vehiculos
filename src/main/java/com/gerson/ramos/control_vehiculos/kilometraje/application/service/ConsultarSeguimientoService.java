package com.gerson.ramos.control_vehiculos.kilometraje.application.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gerson.ramos.control_vehiculos.configuracion.application.port.in.ConsultarParametrosAlertasUseCase;
import com.gerson.ramos.control_vehiculos.kilometraje.application.port.in.ConsultarSeguimientoUseCase;
import com.gerson.ramos.control_vehiculos.kilometraje.application.port.in.FilaSeguimiento;
import com.gerson.ramos.control_vehiculos.kilometraje.application.port.out.LecturaKilometrajeRepositoryPort;
import com.gerson.ramos.control_vehiculos.kilometraje.domain.model.LecturaKilometraje;
import com.gerson.ramos.control_vehiculos.mantenimiento.application.port.in.ConsultarProgramacionUseCase;
import com.gerson.ramos.control_vehiculos.mantenimiento.application.port.in.FilaProgramacion;
import com.gerson.ramos.control_vehiculos.vehiculo.domain.model.VehiculoId;

@Service
@Transactional(readOnly = true)
public class ConsultarSeguimientoService implements ConsultarSeguimientoUseCase {

	private static final Comparator<FilaSeguimiento> ORDEN = Comparator
		.comparing((FilaSeguimiento fila) -> !fila.pendiente())
		.thenComparing(FilaSeguimiento::placa);

	private final ConsultarProgramacionUseCase programacion;
	private final LecturaKilometrajeRepositoryPort lecturas;
	private final ConsultarParametrosAlertasUseCase parametros;
	private final Clock reloj;

	ConsultarSeguimientoService(ConsultarProgramacionUseCase programacion, LecturaKilometrajeRepositoryPort lecturas,
			ConsultarParametrosAlertasUseCase parametros, Clock reloj) {
		this.programacion = programacion;
		this.lecturas = lecturas;
		this.parametros = parametros;
		this.reloj = reloj;
	}

	@Override
	public List<FilaSeguimiento> seguimientoSemanal() {
		LocalDate hoy = LocalDate.now(reloj);
		int diasLecturaPendiente = parametros.obtener().diasLecturaPendiente();
		Map<VehiculoId, LecturaKilometraje> ultimas = new HashMap<>();
		for (LecturaKilometraje lectura : lecturas.ultimasDeTodosLosVehiculos()) {
			ultimas.put(lectura.vehiculoId(), lectura);
		}
		return programacion.programacionDeVehiculosActivos().stream()
			.map(fila -> fila(fila, ultimas.get(new VehiculoId(fila.vehiculoId())), hoy, diasLecturaPendiente))
			.sorted(ORDEN)
			.toList();
	}

	@Override
	public FilaSeguimiento seguimientoDe(long vehiculoId) {
		FilaProgramacion fila = programacion.programacionDe(vehiculoId);
		@Nullable LecturaKilometraje ultima = lecturas.ultimaDe(new VehiculoId(vehiculoId)).orElse(null);
		return fila(fila, ultima, LocalDate.now(reloj), parametros.obtener().diasLecturaPendiente());
	}

	private static FilaSeguimiento fila(FilaProgramacion programacion, @Nullable LecturaKilometraje ultima, LocalDate hoy,
			int diasLecturaPendiente) {
		@Nullable Integer kmMantenimiento = programacion.ultimoKilometraje();
		@Nullable Integer kmLectura = ultima == null ? null : ultima.kilometraje();

		// El odómetro solo crece: el km actual es el mayor entre la última lectura y el último mantenimiento.
		@Nullable Long kmActual = null;
		if (kmLectura != null || kmMantenimiento != null) {
			kmActual = (long) Math.max(kmLectura == null ? Integer.MIN_VALUE : kmLectura,
					kmMantenimiento == null ? Integer.MIN_VALUE : kmMantenimiento);
		}
		@Nullable Long proximoKm = programacion.proximoKm();
		@Nullable Long kmRestantes = (proximoKm != null && kmActual != null) ? proximoKm - kmActual : null;

		@Nullable Long diasSinLectura = ultima == null ? null : ChronoUnit.DAYS.between(ultima.fecha(), hoy);
		boolean pendiente = diasSinLectura == null || diasSinLectura > diasLecturaPendiente;

		return new FilaSeguimiento(programacion.vehiculoId(), programacion.placa(), programacion.codigoEquipo(),
				programacion.localidad(), programacion.ultimaFecha(), kmMantenimiento, proximoKm,
				programacion.diasRestantes(), ultima == null ? null : ultima.fecha(), kmLectura, diasSinLectura, kmActual,
				kmRestantes, pendiente);
	}
}
