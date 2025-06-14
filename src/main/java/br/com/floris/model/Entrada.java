package br.com.floris.model;

import lombok.*;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Entrada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private User usuario;

    @Column(nullable = false)
    private String descricao;

    @Column(nullable = false)
    private BigDecimal valor;

    @Column(nullable = false)
    private LocalDate dataRecebimento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoEntrada tipo;

    @Column(nullable = false)
    private Boolean recorrente;

    public enum TipoEntrada {
        SALARIO, FREELA, OUTROS
    }
}