package br.com.floris.dto;

import br.com.floris.model.Gasto.TipoGasto;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record GastoRequestDTO(
        @NotBlank String descricao,
        @NotNull @DecimalMin("0.00") BigDecimal valor,
        @NotNull TipoGasto tipo,
        @NotNull LocalDate dataVencimento,
        Integer numeroParcelaAtual,
        Integer totalParcelas,
        Boolean gastoCartao,
        Boolean pago
) {
}