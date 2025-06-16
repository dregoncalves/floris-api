package br.com.floris.dto.gastos;

import br.com.floris.model.Gasto;
import br.com.floris.model.Gasto.TipoGasto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record GastoResponseDTO(
        Long id,
        String descricao,
        BigDecimal valor,
        TipoGasto tipo,
        LocalDate dataVencimento,
        Integer numeroParcelaAtual,
        Integer totalParcelas,
        Boolean gastoCartao,
        Boolean pago
) {
    public static GastoResponseDTO fromModel(Gasto gasto) {
        return new GastoResponseDTO(
                gasto.getId(),
                gasto.getDescricao(),
                gasto.getValor(),
                gasto.getTipo(),
                gasto.getDataVencimento(),
                gasto.getNumeroParcelaAtual(),
                gasto.getTotalParcelas(),
                gasto.getGastoCartao(),
                gasto.getPago()
        );
    }
}