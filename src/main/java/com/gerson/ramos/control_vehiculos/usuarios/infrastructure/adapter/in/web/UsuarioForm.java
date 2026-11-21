package com.gerson.ramos.control_vehiculos.usuarios.infrastructure.adapter.in.web;

import com.gerson.ramos.control_vehiculos.usuarios.domain.model.Rol;
import com.gerson.ramos.control_vehiculos.usuarios.domain.model.Usuario;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Alta y edición de usuarios. El nombre de usuario y la contraseña solo se piden al crear (el formato y la política
 * de contraseñas los valida el dominio; aquí solo se comprueba que vengan y que la confirmación coincida).
 */
@Getter
@Setter
public class UsuarioForm {

	@Size(max = 30, message = "El nombre de usuario no puede exceder 30 caracteres")
	private String username;

	@NotBlank(message = "El nombre es obligatorio")
	@Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
	private String nombreCompleto;

	@NotNull(message = "Elige un rol")
	private Rol rol;

	private String clave;

	private String confirmacion;

	private Long version;

	static UsuarioForm vacio() {
		UsuarioForm form = new UsuarioForm();
		form.rol = Rol.OPERADOR;
		return form;
	}

	static UsuarioForm desde(Usuario usuario) {
		UsuarioForm form = new UsuarioForm();
		form.username = usuario.username();
		form.nombreCompleto = usuario.nombreCompleto();
		form.rol = usuario.rol();
		form.version = usuario.version();
		return form;
	}
}
