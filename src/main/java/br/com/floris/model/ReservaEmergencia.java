package br.com.floris.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservaEmergencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User usuario;


    private BigDecimal valorObjetivo;
    private BigDecimal valorAtual;
    private Boolean ativa;

    private LocalDate dataCriacao;
    private LocalDate ultimaAtualizacao;

    @Transient
    public BigDecimal getPercentualConcluido() {
        if (valorObjetivo == null || valorObjetivo.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return valorAtual.divide(valorObjetivo, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
    }
}