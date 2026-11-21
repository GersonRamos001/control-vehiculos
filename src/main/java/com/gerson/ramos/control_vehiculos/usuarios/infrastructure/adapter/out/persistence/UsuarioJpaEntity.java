package com.gerson.ramos.control_vehiculos.usuarios.infrastructure.adapter.out.persistence;

import java.time.Instant;

import com.gerson.ramos.control_vehiculos.shared.infrastructure.persistence.AuditableJpaEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;

/** El rol se guarda como texto (columna VARCHAR) y el mapper lo convierte, para no depender del mapeo de enums de Hibernate. */
@Entity
@Table(name = "usuario")
@Getter
public class UsuarioJpaEntity extends AuditableJpaEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 30, updatable = false)
	private String username;

	@Column(name = "nombre_completo", nullable = false, length = 100)
	private String nombreCompleto;

	@Column(nullable = false, length = 20)
	private String rol;

	@Column(name = "clave_hash", nullable = false, length = 100)
	private String claveHash;

	@Column(nullable = false)
	private boolean activo;

	@Column(name = "intentos_fallidos", nullable = false)
	private int intentosFallidos;

	@Column(name = "bloqueado_hasta")
	private Instant bloqueadoHasta;

	@Column(name = "ultimo_acceso")
	private Instant ultimoAcceso;

	@Version
	private Long version;

	protected UsuarioJpaEntity() {
		// requerido por JPA
	}

	UsuarioJpaEntity(String username) {
		this.username = username;
	}

	void actualizar(String nombreCompleto, String rol, String claveHash, boolean activo, int intentosFallidos,
			Instant bloqueadoHasta, Instant ultimoAcceso) {
		this.nombreCompleto = nombreCompleto;
		this.rol = rol;
		this.claveHash = claveHash;
		this.activo = activo;
		this.intentosFallidos = intentosFallidos;
		this.bloqueadoHasta = bloqueadoHasta;
		this.ultimoAcceso = ultimoAcceso;
	}

	@Override
	public boolean equals(Object otro) {
		return this == otro || (otro instanceof UsuarioJpaEntity que && id != null && id.equals(que.id));
	}

	@Override
	public int hashCode() {
		return UsuarioJpaEntity.class.hashCode();
	}
}
