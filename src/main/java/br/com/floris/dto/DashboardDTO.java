package br.com.floris.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class DashboardDTO {
    private String mesReferencia;
    private BigDecimal saldoAtual;
    private BigDecimal totalEntradas;
    private BigDecimal totalGastosFixos;
    private BigDecimal totalGastosVariaveis;
    private BigDecimal totalLivre;
    private BigDecimal percentualFixos;
    private BigDecimal percentualVariaveis;
    private BigDecimal percentualLivre;
    private ReservaEmergenciaDTO reservaEmergencia;
    private List<MetaFinanceiraResumoDTO> metasFinanceiras;
    private List<SaldoProjetadoDTO> fluxoProjetadoProximosMeses;
    private List<DivisaoGastosDTO> divisaoGastos;
    private List<String> diagnostico;
    private List<String> alertas;
}