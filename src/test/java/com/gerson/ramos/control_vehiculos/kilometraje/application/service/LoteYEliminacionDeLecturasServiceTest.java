package com.gerson.ramos.control_vehiculos.kilometraje.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.gerson.ramos.control_vehiculos.kilometraje.application.port.in.LecturaPorVehiculo;
import com.gerson.ramos.control_vehiculos.kilometraje.application.port.in.ResultadoLote;
import com.gerson.ramos.control_vehiculos.kilometraje.domain.model.LecturaKilometraje;
import com.gerson.ramos.control_vehiculos.shared.domain.RecursoNoEncontradoException;

class LoteYEliminacionDeLecturasServiceTest {

	private static final LocalDate FECHA = LocalDate.of(2026, 11, 14);

	private final EscenarioKilometraje escenario = new EscenarioKilometraje();

	@Test
	void el_lote_registra_las_filas_validas_aunque_otras_fallen() {
		escenario.registrarLectura(escenario.idOtro(), LocalDate.of(2026, 11, 7), 60_000);

		ResultadoLote resultado = escenario.lote.registrar(FECHA, List.of(
				new LecturaPorVehiculo(escenario.idActivo(), 107_500),
				new LecturaPorVehiculo(escenario.idOtro(), 59_000), // menor que la lectura anterior
				new LecturaPorVehiculo(escenario.vehiculoInactivo.id().valor(), 10_000),
				new LecturaPorVehiculo(999, 1_000)));

		assertThat(resultado.nuevas()).isEqualTo(1);
		assertThat(resultado.reemplazadas()).isZero();
		assertThat(resultado.registradas()).isEqualTo(1);
		assertThat(resultado.huboErrores()).isTrue();
		assertThat(resultado.errores()).containsOnlyKeys(escenario.idOtro(), escenario.vehiculoInactivo.id().valor(), 999L);
		assertThat(resultado.errores().get(escenario.idOtro())).contains("menor");
		assertThat(escenario.lecturasRepo.ultimaDe(escenario.vehiculoActivo.id())).isPresent();
		assertThat(escenario.lecturasRepo.ultimaDe(escenario.otroActivo.id()).orElseThrow().kilometraje()).isEqualTo(60_000);
	}

	@Test
	void reenviar_el_mismo_lote_es_seguro() {
		List<LecturaPorVehiculo> filas = List.of(new LecturaPorVehiculo(escenario.idActivo(), 107_500),
				new LecturaPorVehiculo(escenario.idOtro(), 60_000));
		escenario.lote.registrar(FECHA, filas);

		ResultadoLote otraVez = escenario.lote.registrar(FECHA, filas);

		assertThat(otraVez.nuevas()).isZero();
		assertThat(otraVez.sinCambios()).isEqualTo(2);
		assertThat(otraVez.huboErrores()).isFalse();
		assertThat(escenario.lecturasRepo.historialDe(escenario.vehiculoActivo.id())).hasSize(1);
	}

	@Test
	void el_lote_cuenta_las_correcciones() {
		escenario.lote.registrar(FECHA, List.of(new LecturaPorVehiculo(escenario.idActivo(), 175_000)));

		ResultadoLote corregido = escenario.lote.registrar(FECHA, List.of(new LecturaPorVehiculo(escenario.idActivo(), 107_500)));

		assertThat(corregido.reemplazadas()).isEqualTo(1);
		assertThat(corregido.registradas()).isEqualTo(1);
		assertThat(escenario.lecturasRepo.ultimaDe(escenario.vehiculoActivo.id()).orElseThrow().kilometraje()).isEqualTo(107_500);
	}

	@Test
	void un_lote_vacio_no_hace_nada() {
		ResultadoLote resultado = escenario.lote.registrar(FECHA, List.of());

		assertThat(resultado.registradas()).isZero();
		assertThat(resultado.huboErrores()).isFalse();
	}

	@Test
	void eliminar_la_ultima_deja_como_ultima_a_la_anterior() {
		escenario.registrarLectura(LocalDate.of(2026, 11, 7), 107_000);
		escenario.registrarLectura(LocalDate.of(2026, 11, 14), 107_500);

		LecturaKilometraje eliminada = escenario.eliminarUltima.eliminarUltima(escenario.idActivo());

		assertThat(eliminada.fecha()).isEqualTo(LocalDate.of(2026, 11, 14));
		assertThat(escenario.lecturasRepo.ultimaDe(escenario.vehiculoActivo.id()).orElseThrow().fecha())
			.isEqualTo(LocalDate.of(2026, 11, 7));
		assertThat(escenario.lecturasRepo.historialDe(escenario.vehiculoActivo.id())).hasSize(1);
	}

	@Test
	void despues_de_eliminar_se_puede_volver_a_capturar_esa_fecha() {
		escenario.registrarLectura(LocalDate.of(2026, 11, 14), 175_000);
		escenario.eliminarUltima.eliminarUltima(escenario.idActivo());

		assertThat(escenario.registrarLectura(LocalDate.of(2026, 11, 14), 107_500).tipo().name()).isEqualTo("NUEVA");
	}

	@Test
	void eliminar_falla_si_no_hay_lecturas_o_el_vehiculo_no_existe() {
		assertThatThrownBy(() -> escenario.eliminarUltima.eliminarUltima(escenario.idActivo()))
			.isInstanceOf(RecursoNoEncontradoException.class);
		assertThatThrownBy(() -> escenario.eliminarUltima.eliminarUltima(999)).isInstanceOf(RecursoNoEncontradoException.class);
	}
}
