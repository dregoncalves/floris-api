package br.com.floris.model;

import lombok.*;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Gasto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User usuario;

    @Column(nullable = false)
    private String descricao;

    @Column(nullable = false)
    private BigDecimal valor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoGasto tipo;

    @Column(nullable = false)
    private LocalDate dataVencimento;

    private Integer numeroParcelaAtual;
    private Integer totalParcelas;

    private Boolean gastoCartao;
    private Boolean pago;

    public enum TipoGasto {
        FIXO, VARIAVEL, PARCELADO
    }

    // Calcula o valor mensal do gasto
    @Transient
    public BigDecimal getValorMensal() {
        if (getTipo() == TipoGasto.PARCELADO && totalParcelas != null && totalParcelas > 0) {
            return valor.divide(BigDecimal.valueOf(totalParcelas), 2, RoundingMode.HALF_UP);
        }
        return this.valor;
    }
}