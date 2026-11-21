package com.gerson.ramos.control_vehiculos.configuracion.infrastructure.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
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

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.gerson.ramos.control_vehiculos.configuracion.application.port.in.ActualizarParametrosAlertasUseCase;
import com.gerson.ramos.control_vehiculos.configuracion.application.port.in.ActualizarParametrosMantenimientoUseCase;
import com.gerson.ramos.control_vehiculos.configuracion.application.port.in.ConsultarParametrosAlertasUseCase;
import com.gerson.ramos.control_vehiculos.configuracion.application.port.in.ConsultarParametrosMantenimientoUseCase;
import com.gerson.ramos.control_vehiculos.configuracion.application.port.in.GestionarLocalidadesUseCase;
import com.gerson.ramos.control_vehiculos.configuracion.application.port.in.GestionarRutinasUseCase;
import com.gerson.ramos.control_vehiculos.configuracion.application.port.in.GestionarTiposUsoUseCase;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.CodigoCatalogo;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.Localidad;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.LocalidadId;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.ParametrosAlertas;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.ParametrosMantenimiento;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.RutinaMantenimiento;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.RutinaMantenimientoId;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.TipoUso;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.TipoUsoId;
import com.gerson.ramos.control_vehiculos.shared.domain.ConflictoDeDatosException;

/** Prueba de las pantallas de catálogos con las plantillas reales y los casos de uso simulados. */
@WebMvcTest({ LocalidadController.class, TipoUsoController.class, RutinaController.class,
		ParametrosMantenimientoController.class, ParametrosAlertasController.class })
@WithMockUser(roles = "ADMINISTRADOR")
classCatalogosControllersTest {

	@Autowired
	MockMvc mvc;

	@MockitoBean
	GestionarLocalidadesUseCase localidades;

	@MockitoBean
	GestionarTiposUsoUseCase tiposUso;

	@MockitoBean
	GestionarRutinasUseCase rutinas;

	@MockitoBean
	ConsultarParametrosMantenimientoUseCase consultarParametros;

	@MockitoBean
	ActualizarParametrosMantenimientoUseCase actualizarParametros;

	@MockitoBean
	ConsultarParametrosAlertasUseCase consultarUmbrales;

	@MockitoBean
	ActualizarParametrosAlertasUseCase actualizarUmbrales;

	private static Localidad localidad() {
		return Localidad.reconstruir(new LocalidadId(1), new CodigoCatalogo("SPS"), "San Pedro Sula", true);
	}

	private static RutinaMantenimiento rutina() {
		return RutinaMantenimiento.reconstruir(new RutinaMantenimientoId(1), new CodigoCatalogo("10K"), "Preventivo 10K", true);
	}

	@Test
	void lista_localidades_con_sus_acciones() throws Exception {
		when(localidades.listar()).thenReturn(List.of(localidad()));

		String html = mvc.perform(get("/configuracion/localidades"))
			.andExpect(status().isOk())
			.andExpect(view().name("configuracion/catalogo-lista"))
			.andReturn().getResponse().getContentAsString();

		assertThat(html).contains("SPS", "San Pedro Sula", "/configuracion/localidades/1/editar",
				"/configuracion/localidades/1/desactivar");
	}

	@Test
	void crea_una_localidad_y_redirige() throws Exception {
		when(localidades.crear("SPS", "San Pedro Sula")).thenReturn(localidad());

		mvc.perform(post("/configuracion/localidades").with(csrf()).param("codigo", "SPS").param("nombre", "San Pedro Sula"))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/configuracion/localidades"))
			.andExpect(flash().attribute("exito", "Localidad SPS creada"));
	}

	@Test
	void muestra_el_codigo_duplicado_junto_al_campo() throws Exception {
		when(localidades.crear(anyString(), anyString())).thenThrow(new ConflictoDeDatosException("codigo", "Ya existe una localidad con el código SPS"));

		mvc.perform(post("/configuracion/localidades").with(csrf()).param("codigo", "SPS").param("nombre", "Otra"))
			.andExpect(status().isOk())
			.andExpect(view().name("configuracion/catalogo-formulario"))
			.andExpect(model().attributeHasFieldErrors("form", "codigo"));
	}

	@Test
	void rechaza_localidad_sin_nombre() throws Exception {
		mvc.perform(post("/configuracion/localidades").with(csrf()).param("codigo", "SPS").param("nombre", ""))
			.andExpect(status().isOk())
			.andExpect(model().attributeHasFieldErrors("form", "nombre"));

		verify(localidades, never()).crear(anyString(), anyString());
	}

	@Test
	void la_edicion_de_localidad_deja_el_codigo_de_solo_lectura() throws Exception {
		when(localidades.buscar(1)).thenReturn(localidad());

		String html = mvc.perform(get("/configuracion/localidades/1/editar"))
			.andExpect(status().isOk())
			.andReturn().getResponse().getContentAsString();

		assertThat(html).contains("readonly");
	}

	@Test
	void activa_y_desactiva_una_localidad() throws Exception {
		when(localidades.cambiarEstado(1, false)).thenReturn(localidad());

		mvc.perform(post("/configuracion/localidades/1/desactivar").with(csrf()))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/configuracion/localidades"));

		verify(localidades).cambiarEstado(1, false);
	}

	@Test
	void lista_y_crea_tipos_de_uso() throws Exception {
		TipoUso operativo = TipoUso.reconstruir(new TipoUsoId(1), new CodigoCatalogo("OPERATIVO"), "Operativo", true);
		when(tiposUso.listar()).thenReturn(List.of(operativo));
		when(tiposUso.crear("OPERATIVO", "Operativo")).thenReturn(operativo);

		mvc.perform(get("/configuracion/tipos-uso")).andExpect(status().isOk()).andExpect(view().name("configuracion/catalogo-lista"));
		mvc.perform(post("/configuracion/tipos-uso").with(csrf()).param("codigo", "OPERATIVO").param("nombre", "Operativo"))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/configuracion/tipos-uso"));
	}

	@Test
	void lista_rutinas_con_sus_acciones() throws Exception {
		when(rutinas.listar()).thenReturn(List.of(rutina()));

		String html = mvc.perform(get("/configuracion/rutinas"))
			.andExpect(status().isOk())
			.andExpect(view().name("configuracion/catalogo-lista"))
			.andReturn().getResponse().getContentAsString();

		assertThat(html).contains("10K", "Preventivo 10K", "/configuracion/rutinas/1/editar");
	}

	@Test
	void rechaza_rutina_sin_nombre() throws Exception {
		mvc.perform(post("/configuracion/rutinas").with(csrf()).param("codigo", "10K").param("nombre", ""))
			.andExpect(status().isOk())
			.andExpect(model().attributeHasFieldErrors("form", "nombre"));

		verify(rutinas, never()).crear(anyString(), anyString());
	}

	@Test
	void muestra_los_umbrales_de_alerta_vigentes() throws Exception {
		when(consultarUmbrales.obtener()).thenReturn(new ParametrosAlertas(1_000, 30, 7));

		String html = mvc.perform(get("/configuracion/alertas"))
			.andExpect(status().isOk())
			.andExpect(view().name("configuracion/alertas"))
			.andReturn().getResponse().getContentAsString();

		assertThat(html).contains("value=\"1000\"", "value=\"30\"", "value=\"7\"");
	}

	@Test
	void actualiza_los_umbrales_de_alerta_y_redirige() throws Exception {
		when(actualizarUmbrales.actualizar(1_500, 45, 10)).thenReturn(new ParametrosAlertas(1_500, 45, 10));

		mvc.perform(post("/configuracion/alertas").with(csrf())
				.param("umbralKm", "1500").param("umbralDias", "45").param("diasLecturaPendiente", "10"))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/configuracion/alertas"))
			.andExpect(flash().attribute("exito", "Umbrales de alerta actualizados"));

		verify(actualizarUmbrales).actualizar(1_500, 45, 10);
	}

	@Test
	void rechaza_umbrales_no_positivos_o_vacios() throws Exception {
		mvc.perform(post("/configuracion/alertas").with(csrf()).param("umbralKm", "0").param("umbralDias", "-1"))
			.andExpect(status().isOk())
			.andExpect(view().name("configuracion/alertas"))
			.andExpect(model().attributeHasFieldErrors("form", "umbralKm", "umbralDias", "diasLecturaPendiente"));

		verify(actualizarUmbrales, never()).actualizar(org.mockito.ArgumentMatchers.anyInt(), org.mockito.ArgumentMatchers.anyInt(),
				org.mockito.ArgumentMatchers.anyInt());
	}

	@Test
	void muestra_los_parametros_de_mantenimiento_vigentes() throws Exception {
		when(consultarParametros.obtener()).thenReturn(new ParametrosMantenimiento(10_000, 6));

		String html = mvc.perform(get("/configuracion/parametros"))
			.andExpect(status().isOk())
			.andExpect(view().name("configuracion/parametros"))
			.andReturn().getResponse().getContentAsString();

		assertThat(html).contains("10000", "value=\"6\"");
	}

	@Test
	void actualiza_los_parametros_de_mantenimiento_y_redirige() throws Exception {
		when(actualizarParametros.actualizar(12_000, 4)).thenReturn(new ParametrosMantenimiento(12_000, 4));

		mvc.perform(post("/configuracion/parametros").with(csrf()).param("intervaloKm", "12000").param("intervaloMeses", "4"))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/configuracion/parametros"))
			.andExpect(flash().attribute("exito", "Parámetros de mantenimiento actualizados"));

		verify(actualizarParametros).actualizar(12_000, 4);
	}

	@Test
	void rechaza_parametros_no_positivos() throws Exception {
		mvc.perform(post("/configuracion/parametros").with(csrf()).param("intervaloKm", "0").param("intervaloMeses", "-1"))
			.andExpect(status().isOk())
			.andExpect(view().name("configuracion/parametros"))
			.andExpect(model().attributeHasFieldErrors("form", "intervaloKm", "intervaloMeses"));

		verify(actualizarParametros, never()).actualizar(org.mockito.ArgumentMatchers.anyInt(), org.mockito.ArgumentMatchers.anyInt());
	}

	@Test
	void crea_una_rutina_y_redirige() throws Exception {
		when(rutinas.crear("10K", "Preventivo 10K")).thenReturn(rutina());

		mvc.perform(post("/configuracion/rutinas").with(csrf()).param("codigo", "10K").param("nombre", "Preventivo 10K"))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/configuracion/rutinas"))
			.andExpect(flash().attribute("exito", "Rutina 10K creada"));
	}
}
