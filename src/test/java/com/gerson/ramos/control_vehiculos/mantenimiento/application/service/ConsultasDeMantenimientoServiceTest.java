package com.gerson.ramos.control_vehiculos.mantenimiento.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.gerson.ramos.control_vehiculos.mantenimiento.application.port.in.FilaProgramacion;
import com.gerson.ramos.control_vehiculos.mantenimiento.application.port.in.MantenimientoDetalle;
import com.gerson.ramos.control_vehiculos.mantenimiento.application.port.in.RegistrarMantenimientoComando;
import com.gerson.ramos.control_vehiculos.shared.domain.RecursoNoEncontradoException;
import com.gerson.ramos.control_vehiculos.vehiculo.domain.model.CodigoEquipo;
import com.gerson.ramos.control_vehiculos.vehiculo.domain.model.Placa;
import com.gerson.ramos.control_vehiculos.vehiculo.domain.model.Vehiculo;

/** Historial y programación (con reloj fijo en 2026-11-21 y parámetros 10,000 km / 6 meses). */
class ConsultasDeMantenimientoServiceTest {

	private final EscenarioMantenimiento escenario = new EscenarioMantenimiento();

	private Vehiculo otroVehiculoActivo(String placa, String codigoEquipo) {
		return escenario.vehiculosRepo.guardar(Vehiculo.registrar(new Placa(placa), new CodigoEquipo(codigoEquipo),
				escenario.vehiculoActivo.localidadId(), escenario.vehiculoActivo.tipoUsoId()));
	}

	private void registrarPara(Vehiculo vehiculo, LocalDate fecha, int kilometraje) {
		escenario.registrar.registrar(new RegistrarMantenimientoComando(vehiculo.id().valor(), fecha, kilometraje,
				escenario.rutina10k.id().valor(), null));
	}

	@Test
	void el_historial_va_del_mas_reciente_al_mas_antiguo_con_el_codigo_de_rutina() {
		escenario.registrarMantenimiento(LocalDate.of(2026, 1, 10), 90_000, escenario.rutina10k);
		escenario.registrarMantenimiento(LocalDate.of(2026, 7, 10), 100_000, escenario.rutina20k);

		List<MantenimientoDetalle> historial = escenario.historial.historialDe(escenario.idVehiculo());

		assertThat(historial).extracting(MantenimientoDetalle::kilometraje).containsExactly(100_000, 90_000);
		assertThat(historial).extracting(MantenimientoDetalle::rutina).containsExactly("20K", "10K");
	}

	@Test
	void el_historial_falla_si_el_vehiculo_no_existe() {
		assertThatThrownBy(() -> escenario.historial.historialDe(999)).isInstanceOf(RecursoNoEncontradoException.class);
		assertThatThrownBy(() -> escenario.programacion.programacionDe(999)).isInstanceOf(RecursoNoEncontradoException.class);
	}

	@Test
	void sin_mantenimientos_no_hay_programacion() {
		FilaProgramacion fila = escenario.programacion.programacionDe(escenario.idVehiculo());

		assertThat(fila.sinRegistro()).isTrue();
		assertThat(fila.proximoKm()).isNull();
		assertThat(fila.diasRestantes()).isNull();
		assertThat(fila.vencidaPorFecha()).isFalse();
		assertThat(fila.placa()).isEqualTo("ABC-123");
		assertThat(fila.localidad()).isEqualTo("San Pedro Sula");
	}

	@Test
	void calcula_el_proximo_km_y_la_fecha_de_referencia_con_los_parametros() {
		escenario.registrarMantenimiento(LocalDate.of(2026, 10, 21), 100_000, escenario.rutina10k);

		FilaProgramacion fila = escenario.programacion.programacionDe(escenario.idVehiculo());

		assertThat(fila.ultimaFecha()).isEqualTo(LocalDate.of(2026, 10, 21));
		assertThat(fila.ultimoKilometraje()).isEqualTo(100_000);
		assertThat(fila.ultimaRutina()).isEqualTo("10K");
		assertThat(fila.proximoKm()).isEqualTo(110_000L);
		assertThat(fila.fechaReferencia()).isEqualTo(LocalDate.of(2027, 4, 21));
		assertThat(fila.diasRestantes()).isEqualTo(151L);
		assertThat(fila.vencidaPorFecha()).isFalse();
	}

	@Test
	void la_rutina_realizada_no_cambia_el_intervalo() {
		escenario.registrarMantenimiento(LocalDate.of(2026, 10, 21), 100_000, escenario.rutina20k);

		assertThat(escenario.programacion.programacionDe(escenario.idVehiculo()).proximoKm()).isEqualTo(110_000L);
	}

	@Test
	void un_mantenimiento_antiguo_queda_vencido() {
		escenario.registrarMantenimiento(LocalDate.of(2026, 1, 1), 100_000, escenario.rutina10k);

		FilaProgramacion fila = escenario.programacion.programacionDe(escenario.idVehiculo());

		assertThat(fila.fechaReferencia()).isEqualTo(LocalDate.of(2026, 7, 1));
		assertThat(fila.diasRestantes()).isEqualTo(-143L);
		assertThat(fila.vencidaPorFecha()).isTrue();
	}

	@Test
	void respeta_los_parametros_actualizados() {
		escenario.registrarMantenimiento(LocalDate.of(2026, 10, 21), 100_000, escenario.rutina10k);
		escenario.parametros.actualizar(5_000, 3);

		FilaProgramacion fila = escenario.programacion.programacionDe(escenario.idVehiculo());

		assertThat(fila.proximoKm()).isEqualTo(105_000L);
		assertThat(fila.fechaReferencia()).isEqualTo(LocalDate.of(2027, 1, 21));
	}

	@Test
	void la_lista_ordena_vencidos_primero_deja_sin_registro_al_final_y_omite_inactivos() {
		Vehiculo vencido = escenario.vehiculoActivo;
		Vehiculo alDia = otroVehiculoActivo("BBB-222", "EQ-010");
		Vehiculo sinRegistro = otroVehiculoActivo("AAA-111", "EQ-011");
		registrarPara(vencido, LocalDate.of(2026, 1, 1), 100_000);
		registrarPara(alDia, LocalDate.of(2026, 11, 1), 50_000);

		List<FilaProgramacion> filas = escenario.programacion.programacionDeVehiculosActivos();

		assertThat(filas).extracting(FilaProgramacion::placa).containsExactly("ABC-123", "BBB-222", "AAA-111");
		assertThat(filas.get(0).vencidaPorFecha()).isTrue();
		assertThat(filas.get(1).vencidaPorFecha()).isFalse();
		assertThat(filas.get(2).sinRegistro()).isTrue();
		assertThat(filas).extracting(FilaProgramacion::vehiculoId).doesNotContain(escenario.vehiculoInactivo.id().valor());
		assertThat(sinRegistro.id()).isNotNull();
	}

	@Test
	void la_lista_usa_el_ultimo_mantenimiento_de_cada_vehiculo() {
		escenario.registrarMantenimiento(LocalDate.of(2026, 1, 10), 90_000, escenario.rutina10k);
		escenario.registrarMantenimiento(LocalDate.of(2026, 9, 10), 100_000, escenario.rutina20k);

		List<FilaProgramacion> filas = escenario.programacion.programacionDeVehiculosActivos();

		assertThat(filas).hasSize(1);
		assertThat(filas.get(0).ultimoKilometraje()).isEqualTo(100_000);
		assertThat(filas.get(0).ultimaRutina()).isEqualTo("20K");
	}
}
