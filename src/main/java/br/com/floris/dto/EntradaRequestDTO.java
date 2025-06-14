package br.com.floris.dto;

import br.com.floris.model.Entrada.TipoEntrada;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record EntradaRequestDTO(
        @NotBlank String descricao,
        @NotNull @DecimalMin("0.00") BigDecimal valor,
        @NotNull LocalDate dataRecebimento,
        @NotNull TipoEntrada tipo,
        @NotNull Boolean recorrente
) {
}
