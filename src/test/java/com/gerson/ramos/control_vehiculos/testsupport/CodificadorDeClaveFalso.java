package com.gerson.ramos.control_vehiculos.testsupport;

import com.gerson.ramos.control_vehiculos.usuarios.application.port.out.CodificadorDeClavePort;

/** "Hash" reversible para las pruebas: el prefijo {@code hash:} seguido de la clave. */
public class CodificadorDeClaveFalso implements CodificadorDeClavePort {

	public static final String PREFIJO = "hash:";

	@Override
	public String codificar(String clave) {
		return PREFIJO + clave;
	}

	@Override
	public boolean coincide(String clave, String hash) {
		return hash.equals(PREFIJO + clave);
	}
}
