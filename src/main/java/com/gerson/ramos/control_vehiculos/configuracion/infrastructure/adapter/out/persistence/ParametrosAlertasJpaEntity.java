package com.gerson.ramos.control_vehiculos.configuracion.infrastructure.adapter.out.persistence;

import com.gerson.ramos.control_vehiculos.shared.infrastructure.persistence.AuditableJpaEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

/** Fila única (id = 1) con los umbrales de alerta; la crea la migración V6. */
@Entity
@Table(name = "parametros_alertas")
@Getter
public class ParametrosAlertasJpaEntity extends AuditableJpaEntity {

	static final long ID_UNICO = 1L;

	@Id
	private Long id;

	@Column(name = "umbral_km", nullable = false)
	private int umbralKm;

	@Column(name = "umbral_dias", nullable = false)
	private int umbralDias;

	@Column(name = "dias_lectura_pendiente", nullable = false)
	private int diasLecturaPendiente;

	protected ParametrosAlertasJpaEntity() {
		// requerido por JPA
	}

	void actualizar(int umbralKm, int umbralDias, int diasLecturaPendiente) {
		this.umbralKm = umbralKm;
		this.umbralDias = umbralDias;
		this.diasLecturaPendiente = diasLecturaPendiente;
	}

	@Override
	public boolean equals(Object otro) {
		return this == otro || (otro instanceof ParametrosAlertasJpaEntity que && id != null && id.equals(que.id));
	}

	@Override
	public int hashCode() {
		return ParametrosAlertasJpaEntity.class.hashCode();
	}
}
