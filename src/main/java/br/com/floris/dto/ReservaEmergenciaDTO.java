package br.com.floris.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Data;

@Data
public class ReservaEmergenciaDTO {
    private Long id;
    private Long usuarioId;
    private BigDecimal valorObjetivo;
    private BigDecimal valorAtual;
    private BigDecimal percentualConcluido;
    private Boolean ativa;
    private LocalDate dataCriacao;
    private LocalDate ultimaAtualizacao;
}