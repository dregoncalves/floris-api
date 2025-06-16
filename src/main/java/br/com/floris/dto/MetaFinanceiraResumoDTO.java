package br.com.floris.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class MetaFinanceiraResumoDTO {
    private Long id;
    private Long usuarioId;
    private String descricao;
    private BigDecimal valorObjetivo;
    private BigDecimal valorAtual;
    private LocalDate prazoFinal;
    private Boolean concluida;
    private BigDecimal percentualConcluido;
}