package com.gerson.ramos.control_vehiculos.usuarios.infrastructure.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.transaction.annotation.Transactional;

import com.gerson.ramos.control_vehiculos.TestcontainersConfiguration;
import com.gerson.ramos.control_vehiculos.usuarios.application.port.out.UsuarioRepositoryPort;
import com.gerson.ramos.control_vehiculos.usuarios.domain.model.Rol;
import com.gerson.ramos.control_vehiculos.usuarios.domain.model.Usuario;

/**
 * Integración contra MySQL real (Testcontainers): migración V7 + mapeo JPA + adaptador. Cada test corre en una transacción
 * que se revierte. Al arrancar el contexto el administrador inicial ya existe (lo crea PrimerAdministradorRunner), por eso
 * las pruebas usan otros nombres y comparan conteos relativos.
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Transactional
class UsuarioPersistenceAdapterTest {

	@Autowired
	UsuarioRepositoryPort usuarios;

	private static Usuario nuevo(String username, Rol rol) {
		return Usuario.crear(username, "Nombre de " + username, rol, "{bcrypt}hash-de-prueba");
	}

	@Test
	void el_arranque_crea_el_administrador_inicial_con_la_clave_codificada() {
		Usuario admin = usuarios.buscarPorUsername("admin").orElseThrow();

		assertThat(admin.rol()).isEqualTo(Rol.ADMINISTRADOR);
		assertThat(admin.activo()).isTrue();
		assertThat(admin.hashClave()).startsWith("{bcrypt}").doesNotContain("admin123");
	}

	@Test
	void guarda_y_recupera_un_usuario() {
		Usuario guardado = usuarios.guardar(nuevo("ana.lopez", Rol.OPERADOR));

		assertThat(guardado.id()).isNotNull();
		assertThat(guardado.version()).isZero();
		assertThat(guardado.username()).isEqualTo("ana.lopez");
		assertThat(guardado.rol()).isEqualTo(Rol.OPERADOR);
		assertThat(guardado.activo()).isTrue();
		assertThat(guardado.intentosFallidos()).isZero();
		assertThat(guardado.bloqueadoHasta()).isNull();
		assertThat(guardado.ultimoAcceso()).isNull();

		assertThat(usuarios.buscarPorId(guardado.id())).get().extracting(Usuario::username).isEqualTo("ana.lopez");
		assertThat(usuarios.buscarPorUsername("ana.lopez")).isPresent();
		assertThat(usuarios.existePorUsername("ana.lopez")).isTrue();
		assertThat(usuarios.existePorUsername("nadie")).isFalse();
	}

	@Test
	void el_nombre_de_usuario_es_unico() {
		usuarios.guardar(nuevo("repetido", Rol.CONSULTA));

		assertThatThrownBy(() -> usuarios.guardar(nuevo("repetido", Rol.OPERADOR))).isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	void persiste_el_bloqueo_y_el_ultimo_acceso() {
		Usuario usuario = usuarios.guardar(nuevo("bloqueable", Rol.OPERADOR));
		Instant ahora = Instant.now().truncatedTo(ChronoUnit.MICROS);
		usuario.registrarAcceso(ahora);
		for (int i = 0; i < 5; i++) {
			usuario.registrarIntentoFallido(ahora, 5, Duration.ofMinutes(15));
		}

		Usuario guardado = usuarios.guardar(usuario);
		Usuario leido = usuarios.buscarPorId(guardado.id()).orElseThrow();

		assertThat(leido.intentosFallidos()).isEqualTo(5);
		assertThat(leido.bloqueadoHasta()).isEqualTo(ahora.plus(Duration.ofMinutes(15)));
		assertThat(leido.ultimoAcceso()).isEqualTo(ahora);
		assertThat(leido.version()).isEqualTo(1L);
	}

	@Test
	void actualizar_con_una_version_vieja_falla_por_bloqueo_optimista() {
		Usuario guardado = usuarios.guardar(nuevo("concurrente", Rol.OPERADOR));
		Usuario copiaVieja = usuarios.buscarPorId(guardado.id()).orElseThrow();

		guardado.corregirDatos("Nombre nuevo", Rol.CONSULTA);
		usuarios.guardar(guardado);
		copiaVieja.corregirDatos("Otro nombre", Rol.OPERADOR);

		assertThatThrownBy(() -> usuarios.guardar(copiaVieja)).isInstanceOf(ObjectOptimisticLockingFailureException.class);
	}

	@Test
	void cuenta_solo_los_administradores_activos() {
		long antes = usuarios.contarAdministradoresActivos();
		usuarios.guardar(nuevo("admin.dos", Rol.ADMINISTRADOR));
		Usuario inactivo = nuevo("admin.tres", Rol.ADMINISTRADOR);
		inactivo.desactivar();
		usuarios.guardar(inactivo);
		usuarios.guardar(nuevo("operadora", Rol.OPERADOR));

		assertThat(usuarios.contarAdministradoresActivos()).isEqualTo(antes + 1);
	}

	@Test
	void lista_ordenada_por_nombre_de_usuario() {
		usuarios.guardar(nuevo("zzz.ultimo", Rol.CONSULTA));
		usuarios.guardar(nuevo("aaa.primero", Rol.CONSULTA));

		assertThat(usuarios.listarTodos()).extracting(Usuario::username).isSorted().contains("aaa.primero", "zzz.ultimo", "admin");
		assertThat(usuarios.contar()).isGreaterThanOrEqualTo(3);
	}
}
