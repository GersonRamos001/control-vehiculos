package com.gerson.ramos.control_vehiculos.usuarios.domain.model;

import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.regex.Pattern;

import org.jspecify.annotations.Nullable;

import com.gerson.ramos.control_vehiculos.shared.domain.ReglaDeNegocioException;
import com.gerson.ramos.control_vehiculos.shared.domain.Validaciones;

/**
 * Persona con acceso a la aplicación. El dominio solo conoce el hash de la contraseña (opaco: quien lo calcula es un
 * puerto de salida), así que nunca maneja contraseñas en claro.
 */
public final class Usuario {

	static final int LONGITUD_MAXIMA_NOMBRE = 100;
	private static final Pattern PATRON_USERNAME = Pattern.compile("[a-z0-9._-]{3,30}");

	private final @Nullable UsuarioId id;
	private final String username;
	private String nombreCompleto;
	private Rol rol;
	private boolean activo;
	private String hashClave;
	private int intentosFallidos;
	private @Nullable Instant bloqueadoHasta;
	private @Nullable Instant ultimoAcceso;
	private final @Nullable Long version;

	private Usuario(@Nullable UsuarioId id, String username, String nombreCompleto, Rol rol, boolean activo, String hashClave,
			int intentosFallidos, @Nullable Instant bloqueadoHasta, @Nullable Instant ultimoAcceso, @Nullable Long version) {
		this.id = id;
		this.username = username;
		this.nombreCompleto = Validaciones.requerirTexto(nombreCompleto, "El nombre", LONGITUD_MAXIMA_NOMBRE);
		this.rol = Validaciones.requerir(rol, "El rol");
		this.activo = activo;
		this.hashClave = Validaciones.requerir(hashClave, "La contraseña");
		this.intentosFallidos = intentosFallidos;
		this.bloqueadoHasta = bloqueadoHasta;
		this.ultimoAcceso = ultimoAcceso;
		this.version = version;
	}

	/** Usuario nuevo, aún sin id (lo asigna la persistencia). Nace activo y sin bloqueo. */
	public static Usuario crear(String username, String nombreCompleto, Rol rol, String hashClave) {
		return new Usuario(null, normalizarUsername(username), nombreCompleto, rol, true, hashClave, 0, null, null, null);
	}

	/** Reconstruye un usuario ya persistido. */
	public static Usuario reconstruir(UsuarioId id, String username, String nombreCompleto, Rol rol, boolean activo,
			String hashClave, int intentosFallidos, @Nullable Instant bloqueadoHasta, @Nullable Instant ultimoAcceso,
			@Nullable Long version) {
		return new Usuario(Validaciones.requerir(id, "El id"), normalizarUsername(username), nombreCompleto, rol, activo,
				hashClave, intentosFallidos, bloqueadoHasta, ultimoAcceso, version);
	}

	/** Pasa el nombre de usuario a minúsculas y comprueba su formato (3 a 30 caracteres: letras, dígitos, punto, guion y guion bajo). */
	public static String normalizarUsername(@Nullable String valor) {
		String texto = Validaciones.requerirTexto(valor, "El nombre de usuario", 100).toLowerCase(Locale.ROOT);
		if (!PATRON_USERNAME.matcher(texto).matches()) {
			throw new ReglaDeNegocioException(
					"El nombre de usuario debe tener de 3 a 30 caracteres: letras minúsculas, dígitos, punto, guion o guion bajo");
		}
		return texto;
	}

	public void corregirDatos(String nuevoNombre, Rol nuevoRol) {
		this.nombreCompleto = Validaciones.requerirTexto(nuevoNombre, "El nombre", LONGITUD_MAXIMA_NOMBRE);
		this.rol = Validaciones.requerir(nuevoRol, "El rol");
	}

	public void activar() {
		this.activo = true;
	}

	public void desactivar() {
		this.activo = false;
	}

	public void cambiarHashClave(String nuevoHash) {
		this.hashClave = Validaciones.requerir(nuevoHash, "La contraseña");
	}

	public boolean estaBloqueado(Instant ahora) {
		return bloqueadoHasta != null && ahora.isBefore(bloqueadoHasta);
	}

	/**
	 * Cuenta una contraseña incorrecta. Al llegar al máximo la cuenta queda bloqueada durante la duración indicada. Si un
	 * bloqueo anterior ya venció, el conteo empieza de nuevo; mientras la cuenta siga bloqueada no se acumulan intentos.
	 */
	public void registrarIntentoFallido(Instant ahora, int maximoIntentos, Duration duracionBloqueo) {
		if (estaBloqueado(ahora)) {
			return;
		}
		if (bloqueadoHasta != null) {
			intentosFallidos = 0;
			bloqueadoHasta = null;
		}
		intentosFallidos++;
		if (intentosFallidos >= maximoIntentos) {
			bloqueadoHasta = ahora.plus(duracionBloqueo);
		}
	}

	/** Acceso correcto: reinicia el conteo, levanta cualquier bloqueo y anota el último acceso. */
	public void registrarAcceso(Instant ahora) {
		desbloquear();
		this.ultimoAcceso = ahora;
	}

	public void desbloquear() {
		this.intentosFallidos = 0;
		this.bloqueadoHasta = null;
	}

	public @Nullable UsuarioId id() {
		return id;
	}

	public String username() {
		return username;
	}

	public String nombreCompleto() {
		return nombreCompleto;
	}

	public Rol rol() {
		return rol;
	}

	public boolean activo() {
		return activo;
	}

	public String hashClave() {
		return hashClave;
	}

	public int intentosFallidos() {
		return intentosFallidos;
	}

	public @Nullable Instant bloqueadoHasta() {
		return bloqueadoHasta;
	}

	public @Nullable Instant ultimoAcceso() {
		return ultimoAcceso;
	}

	public @Nullable Long version() {
		return version;
	}
}
