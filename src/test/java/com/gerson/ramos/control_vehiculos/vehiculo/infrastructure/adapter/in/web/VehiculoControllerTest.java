package com.gerson.ramos.control_vehiculos.vehiculo.infrastructure.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.gerson.ramos.control_vehiculos.configuracion.application.port.in.ConsultarCatalogosUseCase;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.CodigoCatalogo;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.Localidad;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.LocalidadId;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.TipoUso;
import com.gerson.ramos.control_vehiculos.configuracion.domain.model.TipoUsoId;
import com.gerson.ramos.control_vehiculos.shared.domain.ConflictoDeConcurrenciaException;
import com.gerson.ramos.control_vehiculos.shared.domain.ConflictoDeDatosException;
import com.gerson.ramos.control_vehiculos.shared.domain.RecursoNoEncontradoException;
import com.gerson.ramos.control_vehiculos.vehiculo.application.port.in.ActualizarVehiculoUseCase;
import com.gerson.ramos.control_vehiculos.vehiculo.application.port.in.CambiarEstadoVehiculoUseCase;
import com.gerson.ramos.control_vehiculos.vehiculo.application.port.in.ConsultarVehiculosUseCase;
import com.gerson.ramos.control_vehiculos.vehiculo.application.port.in.FiltroEstado;
import com.gerson.ramos.control_vehiculos.vehiculo.application.port.in.RegistrarVehiculoUseCase;
import com.gerson.ramos.control_vehiculos.vehiculo.application.port.in.VehiculoDetalle;
import com.gerson.ramos.control_vehiculos.vehiculo.domain.model.CodigoEquipo;
import com.gerson.ramos.control_vehiculos.vehiculo.domain.model.Placa;
import com.gerson.ramos.control_vehiculos.vehiculo.domain.model.Vehiculo;
import com.gerson.ramos.control_vehiculos.vehiculo.domain.model.VehiculoId;

/** Prueba de la capa web con las plantillas reales y los casos de uso simulados (sin base de datos). */
@WebMvcTest(VehiculoController.class)
@WithMockUser(roles = "ADMINISTRADOR")
classVehiculoControllerTest {

	@Autowired
	MockMvc mvc;

	@MockitoBean
	ConsultarVehiculosUseCase consultar;

	@MockitoBean
	RegistrarVehiculoUseCase registrar;

	@MockitoBean
	ActualizarVehiculoUseCase actualizar;

	@MockitoBean
	CambiarEstadoVehiculoUseCase cambiarEstado;

	@MockitoBean
	ConsultarCatalogosUseCase catalogos;

	private static Vehiculo vehiculo() {
		return Vehiculo.reconstruir(new VehiculoId(5), new Placa("ABC-123"), new CodigoEquipo("EQ-001"), new LocalidadId(1),
				new TipoUsoId(1), null, true, 0);
	}

	private void catalogosDeEjemplo() {
		when(catalogos.listarLocalidades(false))
			.thenReturn(List.of(Localidad.reconstruir(new LocalidadId(1), new CodigoCatalogo("SPS"), "San Pedro Sula", true)));
		when(catalogos.listarTiposUso(false))
			.thenReturn(List.of(TipoUso.reconstruir(new TipoUsoId(1), new CodigoCatalogo("OPERATIVO"), "Operativo", true)));
	}

	@Test
	void sin_sesion_redirige_al_login() throws Exception {
		mvc.perform(get("/vehiculos").with(anonymous()))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrlPattern("**/login"));
	}

	@Test
	void lista_los_vehiculos_activos_por_defecto() throws Exception {
		when(consultar.listar(FiltroEstado.ACTIVOS)).thenReturn(List.of(new VehiculoDetalle(5, "ABC-123", "EQ-001", 1,
				"San Pedro Sula", 1, "Operativo", null, null, true, 0)));

		String html = mvc.perform(get("/vehiculos"))
			.andExpect(status().isOk())
			.andExpect(view().name("vehiculos/lista"))
			.andReturn().getResponse().getContentAsString();

		assertThat(html).contains("ABC-123", "EQ-001", "San Pedro Sula", "Operativo");
	}

	@Test
	void el_filtro_todos_consulta_todos_los_vehiculos() throws Exception {
		mvc.perform(get("/vehiculos").param("filtro", "TODOS")).andExpect(status().isOk());

		verify(consultar).listar(FiltroEstado.TODOS);
	}

	@Test
	void muestra_el_formulario_de_alta_con_los_catalogos() throws Exception {
		catalogosDeEjemplo();

		String html = mvc.perform(get("/vehiculos/nuevo"))
			.andExpect(status().isOk())
			.andExpect(view().name("vehiculos/formulario"))
			.andReturn().getResponse().getContentAsString();

		assertThat(html).contains("San Pedro Sula", "Operativo");
	}

	@Test
	void registra_un_vehiculo_y_redirige_con_mensaje() throws Exception {
		when(registrar.registrar(any())).thenReturn(vehiculo());

		mvc.perform(post("/vehiculos").with(csrf())
				.param("placa", "abc-123")
				.param("codigoEquipo", "eq-001")
				.param("localidadId", "1")
				.param("tipoUsoId", "1"))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/vehiculos"))
			.andExpect(flash().attribute("exito", "Vehículo ABC-123 registrado"));
	}

	@Test
	void rechaza_el_alta_con_campos_obligatorios_vacios() throws Exception {
		catalogosDeEjemplo();

		mvc.perform(post("/vehiculos").with(csrf()).param("placa", "").param("codigoEquipo", ""))
			.andExpect(status().isOk())
			.andExpect(view().name("vehiculos/formulario"))
			.andExpect(model().attributeHasFieldErrors("form", "placa", "codigoEquipo", "localidadId", "tipoUsoId"));

		verify(registrar, never()).registrar(any());
	}

	@Test
	void muestra_el_conflicto_de_dominio_junto_al_campo() throws Exception {
		catalogosDeEjemplo();
		when(registrar.registrar(any())).thenThrow(new ConflictoDeDatosException("placa", "Ya existe un vehículo con la placa ABC-123"));

		mvc.perform(post("/vehiculos").with(csrf())
				.param("placa", "ABC-123")
				.param("codigoEquipo", "EQ-001")
				.param("localidadId", "1")
				.param("tipoUsoId", "1"))
			.andExpect(status().isOk())
			.andExpect(view().name("vehiculos/formulario"))
			.andExpect(model().attributeHasFieldErrors("form", "placa"));
	}

	@Test
	void el_alta_sin_token_csrf_es_rechazada() throws Exception {
		mvc.perform(post("/vehiculos").param("placa", "ABC-123")).andExpect(status().isForbidden());

		verify(registrar, never()).registrar(any());
	}

	@Test
	void muestra_el_formulario_de_edicion_con_los_datos_actuales() throws Exception {
		catalogosDeEjemplo();
		when(consultar.buscarPorId(5)).thenReturn(vehiculo());

		String html = mvc.perform(get("/vehiculos/5/editar"))
			.andExpect(status().isOk())
			.andExpect(view().name("vehiculos/formulario"))
			.andReturn().getResponse().getContentAsString();

		assertThat(html).contains("ABC-123", "EQ-001");
	}

	@Test
	void un_vehiculo_inactivo_no_se_edita() throws Exception {
		Vehiculo inactivo = vehiculo();
		inactivo.desactivar();
		when(consultar.buscarPorId(5)).thenReturn(inactivo);

		mvc.perform(get("/vehiculos/5/editar"))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/vehiculos?filtro=TODOS"))
			.andExpect(flash().attributeExists("error"));
	}

	@Test
	void editar_un_vehiculo_inexistente_devuelve_404() throws Exception {
		when(consultar.buscarPorId(99)).thenThrow(new RecursoNoEncontradoException("Vehículo", 99L));

		mvc.perform(get("/vehiculos/99/editar"))
			.andExpect(status().isNotFound())
			.andExpect(view().name("error/error"));
	}

	@Test
	void actualiza_un_vehiculo_y_redirige() throws Exception {
		when(actualizar.actualizar(any())).thenReturn(vehiculo());

		mvc.perform(post("/vehiculos/5").with(csrf())
				.param("version", "0")
				.param("placa", "ABC-123")
				.param("codigoEquipo", "EQ-001")
				.param("localidadId", "1")
				.param("tipoUsoId", "1"))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/vehiculos"))
			.andExpect(flash().attribute("exito", "Vehículo ABC-123 actualizado"));
	}

	@Test
	void la_modificacion_concurrente_se_muestra_como_error_del_formulario() throws Exception {
		catalogosDeEjemplo();
		when(actualizar.actualizar(any())).thenThrow(new ConflictoDeConcurrenciaException("El vehículo ABC-123"));

		mvc.perform(post("/vehiculos/5").with(csrf())
				.param("version", "0")
				.param("placa", "ABC-123")
				.param("codigoEquipo", "EQ-001")
				.param("localidadId", "1")
				.param("tipoUsoId", "1"))
			.andExpect(status().isOk())
			.andExpect(view().name("vehiculos/formulario"))
			.andExpect(model().attributeHasErrors("form"));
	}

	@Test
	void desactiva_y_activa_un_vehiculo() throws Exception {
		when(cambiarEstado.desactivar(5)).thenReturn(vehiculo());
		when(cambiarEstado.activar(5)).thenReturn(vehiculo());

		mvc.perform(post("/vehiculos/5/desactivar").with(csrf()))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/vehiculos"));
		mvc.perform(post("/vehiculos/5/activar").with(csrf()))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/vehiculos?filtro=TODOS"));

		verify(cambiarEstado).desactivar(5);
		verify(cambiarEstado).activar(5);
	}
}
