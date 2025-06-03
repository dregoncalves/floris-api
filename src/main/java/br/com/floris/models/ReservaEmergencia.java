package br.com.floris.models;

import java.math.BigDecimal;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

public class ReservaEmergencia {
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;
	
	BigDecimal valorObjetivo;
	BigDecimal valorAtual;
	Boolean ativa;
	

}
