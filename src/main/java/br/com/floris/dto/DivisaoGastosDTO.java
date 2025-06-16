package br.com.floris.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DivisaoGastosDTO {
    private String tipo;
    private BigDecimal valor;
    private BigDecimal percentual;
}