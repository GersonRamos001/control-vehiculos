package com.gerson.ramos.control_vehiculos.usuarios.application.port.out;

/** Hash de contraseñas (la implementación usa el codificador de Spring Security). */
public interface CodificadorDeClavePort {

	String codificar(String clave);

	boolean coincide(String clave, String hash);
}
