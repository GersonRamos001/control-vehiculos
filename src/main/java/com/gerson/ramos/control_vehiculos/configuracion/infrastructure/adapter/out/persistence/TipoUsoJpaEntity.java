package com.gerson.ramos.control_vehiculos.configuracion.infrastructure.adapter.out.persistence;

import com.gerson.ramos.control_vehiculos.shared.infrastructure.persistence.AuditableJpaEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

@Entity
@Table(name = "tipo_uso")
@Getter
public class TipoUsoJpaEntity extends AuditableJpaEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 30, updatable = false)
	private String codigo;

	@Column(nullable = false, length = 100)
	private String nombre;

	@Column(nullable = false)
	private boolean activo;

	protected TipoUsoJpaEntity() {
		// requerido por JPA
	}

	TipoUsoJpaEntity(String codigo, String nombre, boolean activo) {
		this.codigo = codigo;
		this.nombre = nombre;
		this.activo = activo;
	}

	void actualizar(String nombre, boolean activo) {
		this.nombre = nombre;
		this.activo = activo;
	}

	@Override
	public boolean equals(Object otro) {
		return this == otro || (otro instanceof TipoUsoJpaEntity que && id != null && id.equals(que.id));
	}

	@Override
	public int hashCode() {
		return TipoUsoJpaEntity.class.hashCode();
	}
}
