package br.com.floris.models;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Entity;
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
public class Gasto {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	private String descricao;
	private BigDecimal valor;
	private LocalDate data;
	private Boolean fixo;
	private Boolean pago;
	private int parcelas;
}
