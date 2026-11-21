package com.gerson.ramos.control_vehiculos.configuracion.infrastructure.adapter.out.persistence;

import com.gerson.ramos.control_vehiculos.shared.infrastructure.persistence.AuditableJpaEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

/** Fila única (id = 1) con los parámetros globales de mantenimiento; la crea la migración V3. */
@Entity
@Table(name = "parametros_mantenimiento")
@Getter
public class ParametrosMantenimientoJpaEntity extends AuditableJpaEntity {

	static final long ID_UNICO = 1L;

	@Id
	private Long id;

	@Column(name = "intervalo_km", nullable = false)
	private int intervaloKm;

	@Column(name = "intervalo_meses", nullable = false)
	private int intervaloMeses;

	protected ParametrosMantenimientoJpaEntity() {
		// requerido por JPA
	}

	void actualizar(int intervaloKm, int intervaloMeses) {
		this.intervaloKm = intervaloKm;
		this.intervaloMeses = intervaloMeses;
	}

	@Override
	public boolean equals(Object otro) {
		return this == otro || (otro instanceof ParametrosMantenimientoJpaEntity que && id != null && id.equals(que.id));
	}

	@Override
	public int hashCode() {
		return ParametrosMantenimientoJpaEntity.class.hashCode();
	}
}
