package br.com.floris.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SaldoProjetadoDTO {
    private String mes;
    private BigDecimal saldo;
}