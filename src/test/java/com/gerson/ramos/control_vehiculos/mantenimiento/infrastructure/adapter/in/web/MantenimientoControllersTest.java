package com.gerson.ramos.control_vehiculos.mantenimiento.infrastructure.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
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

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.gerson.ramos.control_vehiculos.configuracion.application.port.in.ConsultarCatalogosUseCase;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.CodigoCatalogo;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.LocalidadId;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.RutinaMantenimiento;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.RutinaMantenimientoId;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.TipoUsoId;
import com.gerson.ramos.control_vehiculos.mantenimiento.application.port.in.ConsultarHistorialUseCase;
import com.gerson.ramos.control_vehiculos.mantenimiento.application.port.in.ConsultarProgramacionUseCase;
import com.gerson.ramos.control_vehiculos.mantenimiento.application.port.in.FilaProgramacion;
import com.gerson.ramos.control_vehiculos.mantenimiento.application.port.in.MantenimientoDetalle;
import com.gerson.ramos.control_vehiculos.mantenimiento.application.port.in.RegistrarMantenimientoComando;
import com.gerson.ramos.control_vehiculos.mantenimiento.application.port.in.RegistrarMantenimientoUseCase;
import com.gerson.ramos.control_vehiculos.mantenimiento.domain.model.Mantenimiento;
import com.gerson.ramos.control_vehiculos.mantenimiento.domain.model.MantenimientoId;
import com.gerson.ramos.control_vehiculos.shared.domain.ConflictoDeDatosException;
import com.gerson.ramos.control_vehiculos.shared.domain.RecursoNoEncontradoException;
import com.gerson.ramos.control_vehiculos.vehiculo.application.port.in.ConsultarVehiculosUseCase;
import com.gerson.ramos.control_vehiculos.vehiculo.domain.model.CodigoEquipo;
import com.gerson.ramos.control_vehiculos.vehiculo.domain.model.Placa;
import com.gerson.ramos.control_vehiculos.vehiculo.domain.model.Vehiculo;
import com.gerson.ramos.control_vehiculos.vehiculo.domain.model.VehiculoId;

/** Pantallas de mantenimiento con las plantillas reales y los casos de uso simulados. Reloj fijo en 2026-11-21. */
@WebMvcTest({ MantenimientoController.class, ProgramacionController.class })
@WithMockUser(roles = "ADMINISTRADOR")
classMantenimientoControllersTest {

	@TestConfiguration
	static class RelojFijo {

		@Bean
		Clock reloj() {
			return Clock.fixed(Instant.parse("2026-11-21T12:00:00Z"), ZoneOffset.UTC);
		}
	}

	@Autowired
	MockMvc mvc;

	@MockitoBean
	ConsultarVehiculosUseCase vehiculos;

	@MockitoBean
	ConsultarHistorialUseCase historial;

	@MockitoBean
	ConsultarProgramacionUseCase programacion;

	@MockitoBean
	RegistrarMantenimientoUseCase registrar;

	@MockitoBean
	ConsultarCatalogosUseCase catalogos;

	private static Vehiculo vehiculo(boolean activo) {
		Vehiculo v = Vehiculo.reconstruir(new VehiculoId(5), new Placa("ABC-123"), new CodigoEquipo("EQ-001"), new LocalidadId(1),
				new TipoUsoId(1), null, true, 0);
		if (!activo) {
			v.desactivar();
		}
		return v;
	}

	private static FilaProgramacion filaConRegistro(boolean vencida) {
		return new FilaProgramacion(5, "ABC-123", "EQ-001", "San Pedro Sula", LocalDate.of(2026, 5, 1), 100_000, "10K",
				110_000L, LocalDate.of(2026, 11, 1), vencida ? -20L : 40L);
	}

	private static FilaProgramacion filaSinRegistro() {
		return new FilaProgramacion(5, "ABC-123", "EQ-001", "San Pedro Sula", null, null, null, null, null, null);
	}

	private void rutinasDeEjemplo() {
		when(catalogos.listarRutinas(true)).thenReturn(List.of(
				RutinaMantenimiento.reconstruir(new RutinaMantenimientoId(1), new CodigoCatalogo("10K"), "Preventivo 10K", true)));
	}

	@Test
	void el_historial_muestra_el_vehiculo_la_programacion_y_los_mantenimientos() throws Exception {
		when(vehiculos.buscarPorId(5)).thenReturn(vehiculo(true));
		when(programacion.programacionDe(5)).thenReturn(filaConRegistro(false));
		when(historial.historialDe(5)).thenReturn(List.of(
				new MantenimientoDetalle(1, LocalDate.of(2026, 5, 1), 100_000, 1, "10K", "cambio de aceite")));

		String html = mvc.perform(get("/vehiculos/5/mantenimientos"))
			.andExpect(status().isOk())
			.andExpect(view().name("mantenimientos/historial"))
			.andReturn().getResponse().getContentAsString();

		assertThat(html).contains("ABC-123", "110,000", "2026-11-01", "40 días", "100,000", "10K", "cambio de aceite");
	}

	@Test
	void el_historial_de_un_vehiculo_sin_mantenimientos_explica_que_no_hay_programacion() throws Exception {
		when(vehiculos.buscarPorId(5)).thenReturn(vehiculo(true));
		when(programacion.programacionDe(5)).thenReturn(filaSinRegistro());
		when(historial.historialDe(5)).thenReturn(List.of());

		String html = mvc.perform(get("/vehiculos/5/mantenimientos")).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

		assertThat(html).contains("aún no tiene mantenimientos registrados");
	}

	@Test
	void el_historial_de_un_vehiculo_inexistente_devuelve_404() throws Exception {
		when(vehiculos.buscarPorId(99)).thenThrow(new RecursoNoEncontradoException("Vehículo", 99L));

		mvc.perform(get("/vehiculos/99/mantenimientos")).andExpect(status().isNotFound());
	}

	@Test
	void el_formulario_propone_la_fecha_de_hoy_y_las_rutinas_activas() throws Exception {
		when(vehiculos.buscarPorId(5)).thenReturn(vehiculo(true));
		rutinasDeEjemplo();

		String html = mvc.perform(get("/vehiculos/5/mantenimientos/nuevo"))
			.andExpect(status().isOk())
			.andExpect(view().name("mantenimientos/formulario"))
			.andReturn().getResponse().getContentAsString();

		assertThat(html).contains("2026-11-21", "10K");
	}

	@Test
	void un_vehiculo_inactivo_no_permite_registrar_y_vuelve_al_historial() throws Exception {
		when(vehiculos.buscarPorId(5)).thenReturn(vehiculo(false));

		mvc.perform(get("/vehiculos/5/mantenimientos/nuevo"))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/vehiculos/5/mantenimientos"))
			.andExpect(flash().attributeExists("error"));
	}

	@Test
	void registra_el_mantenimiento_con_los_datos_del_formulario() throws Exception {
		when(vehiculos.buscarPorId(5)).thenReturn(vehiculo(true));
		when(registrar.registrar(any())).thenReturn(Mantenimiento.reconstruir(new MantenimientoId(1), new VehiculoId(5),
				new RutinaMantenimientoId(1), LocalDate.of(2026, 11, 1), 100_000, null));

		mvc.perform(post("/vehiculos/5/mantenimientos").with(csrf())
				.param("fecha", "2026-11-01")
				.param("kilometraje", "100000")
				.param("rutinaId", "1")
				.param("observaciones", "cambio de aceite"))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/vehiculos/5/mantenimientos"))
			.andExpect(flash().attribute("exito", "Mantenimiento registrado para ABC-123"));

		ArgumentCaptor<RegistrarMantenimientoComando> captor = ArgumentCaptor.forClass(RegistrarMantenimientoComando.class);
		verify(registrar).registrar(captor.capture());
		assertThat(captor.getValue()).isEqualTo(new RegistrarMantenimientoComando(5, LocalDate.of(2026, 11, 1), 100_000, 1, "cambio de aceite"));
	}

	@Test
	void rechaza_el_registro_con_campos_obligatorios_vacios() throws Exception {
		when(vehiculos.buscarPorId(5)).thenReturn(vehiculo(true));
		rutinasDeEjemplo();

		mvc.perform(post("/vehiculos/5/mantenimientos").with(csrf()).param("fecha", "2026-11-01"))
			.andExpect(status().isOk())
			.andExpect(view().name("mantenimientos/formulario"))
			.andExpect(model().attributeHasFieldErrors("form", "kilometraje", "rutinaId"));

		verify(registrar, never()).registrar(any());
	}

	@Test
	void rechaza_una_fecha_con_formato_invalido() throws Exception {
		when(vehiculos.buscarPorId(5)).thenReturn(vehiculo(true));
		rutinasDeEjemplo();

		mvc.perform(post("/vehiculos/5/mantenimientos").with(csrf())
				.param("fecha", "no-es-fecha").param("kilometraje", "1").param("rutinaId", "1"))
			.andExpect(status().isOk())
			.andExpect(model().attributeHasFieldErrors("form", "fecha"));

		verify(registrar, never()).registrar(any());
	}

	@Test
	void muestra_el_error_del_dominio_junto_al_campo() throws Exception {
		when(vehiculos.buscarPorId(5)).thenReturn(vehiculo(true));
		rutinasDeEjemplo();
		when(registrar.registrar(any())).thenThrow(new ConflictoDeDatosException("kilometraje", "El kilometraje es menor que el registrado"));

		mvc.perform(post("/vehiculos/5/mantenimientos").with(csrf())
				.param("fecha", "2026-11-01").param("kilometraje", "1").param("rutinaId", "1"))
			.andExpect(status().isOk())
			.andExpect(view().name("mantenimientos/formulario"))
			.andExpect(model().attributeHasFieldErrors("form", "kilometraje"));
	}

	@Test
	void el_registro_sin_token_csrf_es_rechazado() throws Exception {
		mvc.perform(post("/vehiculos/5/mantenimientos").param("fecha", "2026-11-01")).andExpect(status().isForbidden());

		verify(registrar, never()).registrar(any());
	}

	@Test
	void la_programacion_lista_vencidos_y_vehiculos_sin_registro() throws Exception {
		when(programacion.programacionDeVehiculosActivos()).thenReturn(List.of(filaConRegistro(true),
				new FilaProgramacion(6, "AAA-111", "EQ-006", "Tegucigalpa", null, null, null, null, null, null)));

		String html = mvc.perform(get("/mantenimientos/programacion"))
			.andExpect(status().isOk())
			.andExpect(view().name("mantenimientos/programacion"))
			.andReturn().getResponse().getContentAsString();

		assertThat(html).contains("ABC-123", "110,000", "Vencido hace 20 días", "AAA-111", "Sin mantenimientos registrados");
	}
}
