package com.gerson.ramos.control_vehiculos.usuarios.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import java.time.Instant;

import org.junit.jupiter.api.Test;

import com.gerson.ramos.control_vehiculos.shared.domain.ConflictoDeConcurrenciaException;
import com.gerson.ramos.control_vehiculos.shared.domain.ConflictoDeDatosException;
import com.gerson.ramos.control_vehiculos.shared.domain.RecursoNoEncontradoException;
import com.gerson.ramos.control_vehiculos.shared.domain.ReglaDeNegocioException;
import com.gerson.ramos.control_vehiculos.testsupport.CodificadorDeClaveFalso;
import com.gerson.ramos.control_vehiculos.testsupport.InMemoryUsuarioRepository;
import com.gerson.ramos.control_vehiculos.testsupport.SesionesEspia;
import com.gerson.ramos.control_vehiculos.usuarios.application.port.in.ActualizarUsuarioComando;
import com.gerson.ramos.control_vehiculos.usuarios.application.port.in.CrearUsuarioComando;
import com.gerson.ramos.control_vehiculos.usuarios.domain.model.Rol;
import com.gerson.ramos.control_vehiculos.usuarios.domain.model.Usuario;
import com.gerson.ramos.control_vehiculos.usuarios.domain.model.UsuarioId;

class GestionarUsuariosServiceTest {

	private static final String CLAVE = "clave-segura-1";

	private final InMemoryUsuarioRepository repositorio = new InMemoryUsuarioRepository();
	private final CodificadorDeClaveFalso codificador = new CodificadorDeClaveFalso();
	private final SesionesEspia sesiones = new SesionesEspia();
	private final GestionarUsuariosService servicio = new GestionarUsuariosService(repositorio, codificador, sesiones);

	private Usuario crear(String username, Rol rol) {
		return servicio.crear(new CrearUsuarioComando(username, "Nombre de " + username, rol, CLAVE));
	}

	private ActualizarUsuarioComando cambio(Usuario usuario, String nombre, Rol rol, String actor) {
		return new ActualizarUsuarioComando(usuario.id().valor(), usuario.version(), nombre, rol, actor);
	}

	@Test
	void crea_el_usuario_con_el_nombre_normalizado_y_la_clave_codificada() {
		Usuario creado = servicio.crear(new CrearUsuarioComando("  Ana.Lopez ", "Ana López", Rol.OPERADOR, CLAVE));

		assertThat(creado.id()).isNotNull();
		assertThat(creado.username()).isEqualTo("ana.lopez");
		assertThat(creado.rol()).isEqualTo(Rol.OPERADOR);
		assertThat(creado.activo()).isTrue();
		assertThat(creado.hashClave()).isEqualTo(codificador.codificar(CLAVE)).isNotEqualTo(CLAVE);
	}

	@Test
	void rechaza_un_nombre_de_usuario_repetido_aunque_cambien_las_mayusculas() {
		crear("ana", Rol.OPERADOR);

		assertThatThrownBy(() -> crear("ANA", Rol.CONSULTA))
			.isInstanceOfSatisfying(ConflictoDeDatosException.class, e -> assertThat(e.campo()).isEqualTo("username"));
	}

	@Test
	void rechaza_una_clave_que_no_cumple_la_politica_y_no_guarda_nada() {
		assertThatThrownBy(() -> servicio.crear(new CrearUsuarioComando("ana", "Ana", Rol.OPERADOR, "corta")))
			.isInstanceOf(ReglaDeNegocioException.class);

		assertThat(repositorio.contar()).isZero();
	}

	@Test
	void actualiza_nombre_y_rol_y_cierra_las_sesiones_solo_si_cambia_el_rol() {
		crear("jefe", Rol.ADMINISTRADOR);
		Usuario ana = crear("ana", Rol.OPERADOR);

		Usuario soloNombre = servicio.actualizar(cambio(ana, "Ana María", Rol.OPERADOR, "jefe"));
		assertThat(soloNombre.nombreCompleto()).isEqualTo("Ana María");
		assertThat(sesiones.cerradas).isEmpty();

		Usuario conRol = servicio.actualizar(cambio(soloNombre, "Ana María", Rol.CONSULTA, "jefe"));
		assertThat(conRol.rol()).isEqualTo(Rol.CONSULTA);
		assertThat(sesiones.cerradas).containsExactly("ana");
	}

	@Test
	void rechaza_actualizar_con_una_version_vieja() {
		crear("jefe", Rol.ADMINISTRADOR);
		Usuario ana = crear("ana", Rol.OPERADOR);
		servicio.actualizar(cambio(ana, "Otro nombre", Rol.OPERADOR, "jefe"));

		assertThatThrownBy(() -> servicio.actualizar(cambio(ana, "Nombre viejo", Rol.OPERADOR, "jefe")))
			.isInstanceOf(ConflictoDeConcurrenciaException.class);
	}

	@Test
	void un_administrador_no_puede_quitarse_a_si_mismo_el_rol() {
		Usuario jefe = crear("jefe", Rol.ADMINISTRADOR);
		crear("otro", Rol.ADMINISTRADOR);

		assertThatThrownBy(() -> servicio.actualizar(cambio(jefe, "Jefe", Rol.OPERADOR, "JEFE")))
			.isInstanceOf(ReglaDeNegocioException.class)
			.hasMessageContaining("a ti mismo");
	}

	@Test
	void no_se_puede_degradar_al_ultimo_administrador_activo() {
		Usuario jefe = crear("jefe", Rol.ADMINISTRADOR);
		crear("ana", Rol.OPERADOR);

		assertThatThrownBy(() -> servicio.actualizar(cambio(jefe, "Jefe", Rol.CONSULTA, "ana")))
			.isInstanceOf(ReglaDeNegocioException.class)
			.hasMessageContaining("al menos un administrador");
		assertThat(repositorio.contarAdministradoresActivos()).isEqualTo(1);
	}

	@Test
	void se_puede_degradar_a_un_administrador_si_queda_otro_activo() {
		crear("jefe", Rol.ADMINISTRADOR);
		Usuario otro = crear("otro", Rol.ADMINISTRADOR);

		Usuario degradado = servicio.actualizar(cambio(otro, "Otro", Rol.OPERADOR, "jefe"));

		assertThat(degradado.rol()).isEqualTo(Rol.OPERADOR);
		assertThat(repositorio.contarAdministradoresActivos()).isEqualTo(1);
	}

	@Test
	void desactivar_cierra_las_sesiones_y_activar_no() {
		crear("jefe", Rol.ADMINISTRADOR);
		Usuario ana = crear("ana", Rol.OPERADOR);

		Usuario inactiva = servicio.cambiarEstado(ana.id().valor(), false, "jefe");
		assertThat(inactiva.activo()).isFalse();
		assertThat(sesiones.cerradas).containsExactly("ana");

		Usuario activa = servicio.cambiarEstado(ana.id().valor(), true, "jefe");
		assertThat(activa.activo()).isTrue();
		assertThat(sesiones.cerradas).hasSize(1);
	}

	@Test
	void nadie_puede_desactivarse_a_si_mismo() {
		Usuario jefe = crear("jefe", Rol.ADMINISTRADOR);
		crear("otro", Rol.ADMINISTRADOR);

		assertThatThrownBy(() -> servicio.cambiarEstado(jefe.id().valor(), false, "jefe"))
			.isInstanceOf(ReglaDeNegocioException.class)
			.hasMessageContaining("tu propia cuenta");
	}

	@Test
	void no_se_puede_desactivar_al_ultimo_administrador_activo() {
		Usuario jefe = crear("jefe", Rol.ADMINISTRADOR);
		crear("ana", Rol.OPERADOR);

		assertThatThrownBy(() -> servicio.cambiarEstado(jefe.id().valor(), false, "ana"))
			.isInstanceOf(ReglaDeNegocioException.class)
			.hasMessageContaining("al menos un administrador");
	}

	@Test
	void cambiar_el_estado_al_que_ya_tiene_no_hace_nada() {
		crear("jefe", Rol.ADMINISTRADOR);
		Usuario ana = crear("ana", Rol.OPERADOR);

		Usuario igual = servicio.cambiarEstado(ana.id().valor(), true, "jefe");

		assertThat(igual.version()).isEqualTo(ana.version());
		assertThat(sesiones.cerradas).isEmpty();
	}

	@Test
	void desbloquear_levanta_el_bloqueo() {
		crear("jefe", Rol.ADMINISTRADOR);
		Usuario ana = crear("ana", Rol.OPERADOR);
		for (int i = 0; i < 5; i++) {
			ana.registrarIntentoFallido(Instant.now(), 5, Duration.ofMinutes(15));
		}
		repositorio.guardar(ana);

		Usuario desbloqueada = servicio.desbloquear(ana.id().valor());

		assertThat(desbloqueada.intentosFallidos()).isZero();
		assertThat(desbloqueada.bloqueadoHasta()).isNull();
	}

	@Test
	void restablecer_la_clave_la_cambia_levanta_el_bloqueo_y_cierra_las_sesiones() {
		crear("jefe", Rol.ADMINISTRADOR);
		Usuario ana = crear("ana", Rol.OPERADOR);
		for (int i = 0; i < 5; i++) {
			ana.registrarIntentoFallido(Instant.now(), 5, Duration.ofMinutes(15));
		}
		repositorio.guardar(ana);

		Usuario restablecida = servicio.restablecerClave(ana.id().valor(), "otra-clave-nueva", "jefe");

		assertThat(restablecida.hashClave()).isEqualTo(codificador.codificar("otra-clave-nueva"));
		assertThat(restablecida.intentosFallidos()).isZero();
		assertThat(restablecida.bloqueadoHasta()).isNull();
		assertThat(sesiones.cerradas).containsExactly("ana");
	}

	@Test
	void restablecer_rechaza_la_propia_cuenta_y_las_claves_debiles() {
		Usuario jefe = crear("jefe", Rol.ADMINISTRADOR);
		Usuario ana = crear("ana", Rol.OPERADOR);

		assertThatThrownBy(() -> servicio.restablecerClave(jefe.id().valor(), "otra-clave-nueva", "jefe"))
			.hasMessageContaining("Cambiar mi contraseña");
		assertThatThrownBy(() -> servicio.restablecerClave(ana.id().valor(), "corta", "jefe"))
			.isInstanceOf(ReglaDeNegocioException.class);
		assertThat(sesiones.cerradas).isEmpty();
	}

	@Test
	void buscar_un_usuario_que_no_existe_falla() {
		assertThatThrownBy(() -> servicio.buscar(99)).isInstanceOf(RecursoNoEncontradoException.class);
		assertThat(new UsuarioId(1).valor()).isEqualTo(1);
	}

	@Test
	void lista_ordenada_por_nombre_de_usuario() {
		crear("zoe", Rol.CONSULTA);
		crear("ana", Rol.OPERADOR);

		assertThat(servicio.listar()).extracting(Usuario::username).containsExactly("ana", "zoe");
	}

	@Test
	void crea_el_primer_administrador_solo_con_la_tabla_vacia() {
		assertThat(servicio.crearSiNoHayUsuarios("Admin", "admin123")).isTrue();

		Usuario admin = servicio.listar().get(0);
		assertThat(admin.username()).isEqualTo("admin");
		assertThat(admin.rol()).isEqualTo(Rol.ADMINISTRADOR);
		assertThat(admin.hashClave()).isEqualTo(codificador.codificar("admin123"));

		assertThat(servicio.crearSiNoHayUsuarios("otro", "otra-clave-1")).isFalse();
		assertThat(repositorio.contar()).isEqualTo(1);
	}

	@Test
	void con_la_tabla_vacia_exige_credenciales_validas_y_con_usuarios_no_las_mira() {
		assertThatThrownBy(() -> servicio.crearSiNoHayUsuarios("", "admin123")).isInstanceOf(ReglaDeNegocioException.class);
		assertThatThrownBy(() -> servicio.crearSiNoHayUsuarios("admin", "")).isInstanceOf(ReglaDeNegocioException.class);
		assertThat(repositorio.contar()).isZero();

		crear("jefe", Rol.ADMINISTRADOR);
		assertThat(servicio.crearSiNoHayUsuarios("", "")).isFalse();
	}
}
