package br.com.floris.models;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

public class Simulacao {
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

}
