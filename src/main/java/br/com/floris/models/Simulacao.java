package br.com.floris.models;

import java.math.BigDecimal;
import java.time.LocalDate;

import br.com.floris.models.enums.TipoSimulacao;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Simulacao {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	private String descricao;

	private BigDecimal valor;

	private LocalDate data;

	@Enumerated(EnumType.STRING)
	private TipoSimulacao tipo; // ENTRADA, GASTO, INVESTIMENTO

	private Boolean recorrente; // útil em caso de gasto/entrada recorrente

	private int parcelas; // se for uma simulação parcelada

	private Boolean aprovada; // o usuário aprovou a simulação?
}
