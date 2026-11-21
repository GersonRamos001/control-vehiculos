package com.gerson.ramos.control_vehiculos.kilometraje.infrastructure.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.gerson.ramos.control_vehiculos.configuracion.domain.model.LocalidadId;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.TipoUsoId;
import com.gerson.ramos.control_vehiculos.kilometraje.application.port.in.ConsultarLecturasUseCase;
import com.gerson.ramos.control_vehiculos.kilometraje.application.port.in.ConsultarSeguimientoUseCase;
import com.gerson.ramos.control_vehiculos.kilometraje.application.port.in.EliminarUltimaLecturaUseCase;
import com.gerson.ramos.control_vehiculos.kilometraje.application.port.in.FilaSeguimiento;
import com.gerson.ramos.control_vehiculos.kilometraje.application.port.in.LecturaDetalle;
import com.gerson.ramos.control_vehiculos.kilometraje.application.port.in.LecturaPorVehiculo;
import com.gerson.ramos.control_vehiculos.kilometraje.application.port.in.RegistrarLecturaComando;
import com.gerson.ramos.control_vehiculos.kilometraje.application.port.in.RegistrarLecturaUseCase;
import com.gerson.ramos.control_vehiculos.kilometraje.application.port.in.RegistrarLecturasSemanalesUseCase;
import com.gerson.ramos.control_vehiculos.kilometraje.application.port.in.ResultadoLote;
import com.gerson.ramos.control_vehiculos.kilometraje.application.port.in.ResultadoRegistro;
import com.gerson.ramos.control_vehiculos.kilometraje.domain.model.LecturaId;
import com.gerson.ramos.control_vehiculos.kilometraje.domain.model.LecturaKilometraje;
import com.gerson.ramos.control_vehiculos.shared.domain.ConflictoDeDatosException;
import com.gerson.ramos.control_vehiculos.shared.domain.RecursoNoEncontradoException;
import com.gerson.ramos.control_vehiculos.vehiculo.application.port.in.ConsultarVehiculosUseCase;
import com.gerson.ramos.control_vehiculos.vehiculo.domain.model.CodigoEquipo;
import com.gerson.ramos.control_vehiculos.vehiculo.domain.model.Placa;
import com.gerson.ramos.control_vehiculos.vehiculo.domain.model.Vehiculo;
import com.gerson.ramos.control_vehiculos.vehiculo.domain.model.VehiculoId;

/** Pantallas de kilometraje con las plantillas reales y los casos de uso simulados. Reloj fijo en 2026-11-21. */
@WebMvcTest({ KilometrajeController.class, SeguimientoSemanalController.class })
@WithMockUser(roles = "ADMINISTRADOR")
classKilometrajeControllersTest {

	@TestConfiguration
	static class RelojFijo {

		@Bean
		Clock reloj() {
			return Clock.fixed(Instant.parse("2026-11-21T12:00:00Z"), ZoneOffset.UTC);
		}
	}

	private static final LocalDate HOY = LocalDate.of(2026, 11, 21);

	@Autowired
	MockMvc mvc;

	@MockitoBean
	ConsultarVehiculosUseCase vehiculos;

	@MockitoBean
	ConsultarLecturasUseCase historial;

	@MockitoBean
	ConsultarSeguimientoUseCase seguimiento;

	@MockitoBean
	RegistrarLecturaUseCase registrar;

	@MockitoBean
	EliminarUltimaLecturaUseCase eliminarUltima;

	@MockitoBean
	RegistrarLecturasSemanalesUseCase lote;

	private static Vehiculo vehiculo() {
		return Vehiculo.reconstruir(new VehiculoId(5), new Placa("ABC-123"), new CodigoEquipo("EQ-001"), new LocalidadId(1),
				new TipoUsoId(1), null, true, 0);
	}

	/** Mantenimiento a 100,000 km, próximo a 110,000, lectura 107,500 hace 2 días: restan 2,500 km. */
	private static FilaSeguimiento filaAlDia() {
		return new FilaSeguimiento(5, "ABC-123", "EQ-001", "San Pedro Sula", LocalDate.of(2026, 5, 1), 100_000, 110_000L, 161L,
				LocalDate.of(2026, 11, 19), 107_500, 2L, 107_500L, 2_500L, false);
	}

	private static FilaSeguimiento filaPendienteSinMantenimiento() {
		return new FilaSeguimiento(6, "DEF-456", "EQ-002", "Tegucigalpa", null, null, null, null, null, null, null, null, null, true);
	}

	private static LecturaKilometraje lectura(LocalDate fecha, int km) {
		return LecturaKilometraje.reconstruir(new LecturaId(1), new VehiculoId(5), fecha, km);
	}

	@Test
	void la_pantalla_semanal_muestra_una_fila_con_casilla_por_vehiculo_y_datos_para_el_calculo_en_vivo() throws Exception {
		when(seguimiento.seguimientoSemanal()).thenReturn(List.of(filaPendienteSinMantenimiento(), filaAlDia()));

		String html = mvc.perform(get("/kilometraje/semanal"))
			.andExpect(status().isOk())
			.andExpect(view().name("kilometraje/semanal"))
			.andReturn().getResponse().getContentAsString();

		assertThat(html).contains("name=\"kilometrajes[5]\"", "name=\"kilometrajes[6]\"", "2026-11-21", "1 pendientes de lectura",
				"data-proximo-km=\"110000\"", "data-ultima-lectura=\"107500\"", "data-km-base=\"107500\"", "2,500 km",
				"Sin mantenimientos", "/js/seguimiento-semanal.js");
	}

	@Test
	void la_pantalla_semanal_acepta_otra_fecha_inicial() throws Exception {
		when(seguimiento.seguimientoSemanal()).thenReturn(List.of());

		String html = mvc.perform(get("/kilometraje/semanal").param("fecha", "2026-11-14")).andExpect(status().isOk())
			.andReturn().getResponse().getContentAsString();

		assertThat(html).contains("2026-11-14");
	}

	@Test
	void guarda_las_lecturas_escritas_e_ignora_las_casillas_vacias() throws Exception {
		when(lote.registrar(any(), any())).thenReturn(new ResultadoLote(2, 0, 0, Map.of()));

		mvc.perform(post("/kilometraje/semanal").with(csrf())
				.param("fecha", "2026-11-21")
				.param("kilometrajes[5]", "107500")
				.param("kilometrajes[6]", "50000")
				.param("kilometrajes[7]", ""))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/kilometraje/semanal"))
			.andExpect(flash().attribute("exito", "2 lecturas registradas"));

		@SuppressWarnings("unchecked")
		ArgumentCaptor<List<LecturaPorVehiculo>> captor = ArgumentCaptor.forClass(List.class);
		verify(lote).registrar(eq(HOY), captor.capture());
		assertThat(captor.getValue()).containsExactlyInAnyOrder(new LecturaPorVehiculo(5, 107_500), new LecturaPorVehiculo(6, 50_000));
	}

	@Test
	void el_mensaje_de_exito_distingue_corregidas_y_sin_cambios() throws Exception {
		when(lote.registrar(any(), any())).thenReturn(new ResultadoLote(1, 2, 3, Map.of()));

		mvc.perform(post("/kilometraje/semanal").with(csrf()).param("fecha", "2026-11-21").param("kilometrajes[5]", "1"))
			.andExpect(status().is3xxRedirection())
			.andExpect(flash().attribute("exito", "1 lectura registrada, 2 corregidas, 3 sin cambios"));
	}

	@Test
	void sin_ninguna_lectura_escrita_no_guarda_y_avisa() throws Exception {
		when(seguimiento.seguimientoSemanal()).thenReturn(List.of(filaAlDia()));

		mvc.perform(post("/kilometraje/semanal").with(csrf()).param("fecha", "2026-11-21").param("kilometrajes[5]", ""))
			.andExpect(status().isOk())
			.andExpect(view().name("kilometraje/semanal"))
			.andExpect(model().attributeHasErrors("form"));

		verify(lote, never()).registrar(any(), any());
	}

	@Test
	void las_filas_con_error_conservan_lo_digitado_y_muestran_el_motivo() throws Exception {
		when(seguimiento.seguimientoSemanal()).thenReturn(List.of(filaAlDia()));
		when(lote.registrar(any(), any())).thenReturn(new ResultadoLote(1, 0, 0,
				Map.of(5L, "El kilometraje es menor que el de la lectura del 2026-11-14 (107500 km)")));

		String html = mvc.perform(post("/kilometraje/semanal").with(csrf()).param("fecha", "2026-11-21").param("kilometrajes[5]", "100"))
			.andExpect(status().isOk())
			.andExpect(view().name("kilometraje/semanal"))
			.andReturn().getResponse().getContentAsString();

		assertThat(html).contains("Se guardó lo válido (1 lectura registrada)", "El kilometraje es menor que el de la lectura del 2026-11-14", "value=\"100\"");
	}

	@Test
	void una_fecha_invalida_o_un_km_no_numerico_se_rechazan_sin_llamar_al_caso_de_uso() throws Exception {
		when(seguimiento.seguimientoSemanal()).thenReturn(List.of(filaAlDia()));

		mvc.perform(post("/kilometraje/semanal").with(csrf()).param("fecha", "ayer").param("kilometrajes[5]", "abc"))
			.andExpect(status().isOk())
			.andExpect(model().attributeHasFieldErrors("form", "fecha"));

		verify(lote, never()).registrar(any(), any());
	}

	@Test
	void la_captura_semanal_sin_token_csrf_es_rechazada() throws Exception {
		mvc.perform(post("/kilometraje/semanal").param("fecha", "2026-11-21")).andExpect(status().isForbidden());

		verify(lote, never()).registrar(any(), any());
	}

	@Test
	void el_historial_muestra_el_seguimiento_los_km_restantes_y_las_diferencias() throws Exception {
		when(vehiculos.buscarPorId(5)).thenReturn(vehiculo());
		when(seguimiento.seguimientoDe(5)).thenReturn(filaAlDia());
		when(historial.historialDe(5)).thenReturn(List.of(
				new LecturaDetalle(2, LocalDate.of(2026, 11, 19), 107_500, 600, 7L),
				new LecturaDetalle(1, LocalDate.of(2026, 11, 12), 106_900, null, null)));

		String html = mvc.perform(get("/vehiculos/5/kilometraje"))
			.andExpect(status().isOk())
			.andExpect(view().name("kilometraje/historial"))
			.andReturn().getResponse().getContentAsString();

		assertThat(html).contains("ABC-123", "2,500 km", "110,000", "107,500", "600 km", "7 días", "Eliminar última", "2026-11-21");
	}

	@Test
	void el_historial_de_un_vehiculo_inexistente_devuelve_404() throws Exception {
		when(vehiculos.buscarPorId(99)).thenThrow(new RecursoNoEncontradoException("Vehículo", 99L));

		mvc.perform(get("/vehiculos/99/kilometraje")).andExpect(status().isNotFound());
	}

	@Test
	void registra_una_lectura_individual_y_redirige() throws Exception {
		when(registrar.registrar(any())).thenReturn(new ResultadoRegistro(lectura(LocalDate.of(2026, 11, 19), 107_500), ResultadoRegistro.Tipo.NUEVA));

		mvc.perform(post("/vehiculos/5/kilometraje").with(csrf()).param("fecha", "2026-11-19").param("kilometraje", "107500"))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/vehiculos/5/kilometraje"))
			.andExpect(flash().attribute("exito", "Lectura del 2026-11-19 registrada"));

		verify(registrar).registrar(new RegistrarLecturaComando(5, LocalDate.of(2026, 11, 19), 107_500));
	}

	@Test
	void corregir_una_lectura_de_la_misma_fecha_lo_informa() throws Exception {
		when(registrar.registrar(any())).thenReturn(new ResultadoRegistro(lectura(LocalDate.of(2026, 11, 19), 107_500), ResultadoRegistro.Tipo.REEMPLAZADA));

		mvc.perform(post("/vehiculos/5/kilometraje").with(csrf()).param("fecha", "2026-11-19").param("kilometraje", "107500"))
			.andExpect(status().is3xxRedirection())
			.andExpect(flash().attribute("exito", "Lectura del 2026-11-19 corregida a 107500 km"));
	}

	@Test
	void rechaza_la_lectura_individual_con_campos_vacios() throws Exception {
		when(vehiculos.buscarPorId(5)).thenReturn(vehiculo());
		when(seguimiento.seguimientoDe(5)).thenReturn(filaAlDia());

		mvc.perform(post("/vehiculos/5/kilometraje").with(csrf()).param("fecha", "2026-11-19"))
			.andExpect(status().isOk())
			.andExpect(view().name("kilometraje/historial"))
			.andExpect(model().attributeHasFieldErrors("form", "kilometraje"));

		verify(registrar, never()).registrar(any());
	}

	@Test
	void muestra_el_error_del_dominio_junto_al_campo() throws Exception {
		when(vehiculos.buscarPorId(5)).thenReturn(vehiculo());
		when(seguimiento.seguimientoDe(5)).thenReturn(filaAlDia());
		when(registrar.registrar(any())).thenThrow(new ConflictoDeDatosException("kilometraje", "El kilometraje es menor que el de la lectura anterior"));

		mvc.perform(post("/vehiculos/5/kilometraje").with(csrf()).param("fecha", "2026-11-19").param("kilometraje", "1"))
			.andExpect(status().isOk())
			.andExpect(view().name("kilometraje/historial"))
			.andExpect(model().attributeHasFieldErrors("form", "kilometraje"));
	}

	@Test
	void elimina_la_ultima_lectura_y_redirige() throws Exception {
		when(eliminarUltima.eliminarUltima(5)).thenReturn(lectura(LocalDate.of(2026, 11, 19), 107_500));

		mvc.perform(post("/vehiculos/5/kilometraje/ultima/eliminar").with(csrf()))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/vehiculos/5/kilometraje"))
			.andExpect(flash().attributeExists("exito"));

		verify(eliminarUltima).eliminarUltima(5);
	}

	@Test
	void eliminar_sin_token_csrf_es_rechazado() throws Exception {
		mvc.perform(post("/vehiculos/5/kilometraje/ultima/eliminar")).andExpect(status().isForbidden());

		verify(eliminarUltima, never()).eliminarUltima(5);
	}
}
