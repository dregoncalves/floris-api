package br.com.floris.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HistoricoMensalDTO {
    private String mes;
    private BigDecimal totalEntradas;
    private BigDecimal totalGastos;
}