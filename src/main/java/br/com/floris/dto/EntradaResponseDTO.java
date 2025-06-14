package br.com.floris.dto;

import br.com.floris.model.Entrada;
import br.com.floris.model.Entrada.TipoEntrada;

import java.math.BigDecimal;
import java.time.LocalDate;

public record EntradaResponseDTO(
        Long id,
        String descricao,
        BigDecimal valor,
        LocalDate dataRecebimento,
        TipoEntrada tipo,
        Boolean recorrente
) {
    public static EntradaResponseDTO fromModel(Entrada entrada) {
        return new EntradaResponseDTO(
                entrada.getId(),
                entrada.getDescricao(),
                entrada.getValor(),
                entrada.getDataRecebimento(),
                entrada.getTipo(),
                entrada.getRecorrente()
        );
    }
}
