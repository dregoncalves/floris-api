package br.com.floris.models;

import java.math.BigDecimal;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

public class Entrada {
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;
	
	String descricao;
	BigDecimal valor;
	String dataRecebimento;
	Boolean frequente;
	

}
