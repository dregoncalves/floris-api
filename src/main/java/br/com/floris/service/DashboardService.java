package br.com.floris.service;

import br.com.floris.dto.*;
import br.com.floris.model.Entrada;
import br.com.floris.model.Gasto;
import br.com.floris.model.MetaFinanceira;
import br.com.floris.model.ReservaEmergencia;
import br.com.floris.model.User;
import br.com.floris.repository.EntradaRepository;
import br.com.floris.repository.GastoRepository;
import br.com.floris.repository.MetaFinanceiraRepository;
import br.com.floris.repository.ReservaEmergenciaRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final EntradaRepository entradaRepository;
    private final GastoRepository gastoRepository;
    private final MetaFinanceiraRepository metaFinanceiraRepository;
    private final ReservaEmergenciaRepository reservaEmergenciaRepository;
    private final UserService userService;

    public DashboardService(
            EntradaRepository entradaRepository,
            GastoRepository gastoRepository,
            MetaFinanceiraRepository metaFinanceiraRepository,
            ReservaEmergenciaRepository reservaEmergenciaRepository,
            UserService userService
    ) {
        this.entradaRepository = entradaRepository;
        this.gastoRepository = gastoRepository;
        this.metaFinanceiraRepository = metaFinanceiraRepository;
        this.reservaEmergenciaRepository = reservaEmergenciaRepository;
        this.userService = userService;
    }

    // Gera todos os dados do dashboard pro usuário
    public DashboardDTO gerarDashboard(Authentication authentication) {
        User usuario = userService.findByUsername(authentication.getName());
        LocalDate hoje = LocalDate.now();
        String mesReferenciaFormatado = hoje.format(DateTimeFormatter.ofPattern("yyyy-MM"));

        List<Entrada> todasAsEntradas = entradaRepository.findAllByUsuarioId(usuario.getId());
        List<Gasto> todosOsGastos = gastoRepository.findAllByUsuarioId(usuario.getId());

        List<Entrada> entradasDoMes = getEntradasParaMes(hoje, todasAsEntradas);
        List<Gasto> gastosDoMes = getGastosParaMes(hoje, todosOsGastos);

        BigDecimal totalEntradas = entradasDoMes.stream()
                .map(Entrada::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalGastosFixos = gastosDoMes.stream()
                .filter(g -> g.getTipo() == Gasto.TipoGasto.FIXO)
                .map(Gasto::getValorMensal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalGastosVariaveis = gastosDoMes.stream()
                .filter(g -> g.getTipo() == Gasto.TipoGasto.VARIAVEL)
                .map(Gasto::getValorMensal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalGastosParcelados = gastosDoMes.stream()
                .filter(g -> g.getTipo() == Gasto.TipoGasto.PARCELADO)
                .map(gasto -> {
                    BigDecimal valorParcelaBase = gasto.getValor()
                            .divide(BigDecimal.valueOf(gasto.getTotalParcelas()), 2, RoundingMode.DOWN);

                    LocalDate dataPrimeiraParcela = gasto.getDataVencimento();
                    LocalDate dataUltimaParcela = dataPrimeiraParcela.plusMonths(gasto.getTotalParcelas() - 1);

                    boolean isUltimaParcela = hoje.getYear() == dataUltimaParcela.getYear() &&
                            hoje.getMonth() == dataUltimaParcela.getMonth();

                    if (isUltimaParcela) {
                        BigDecimal totalJaPago = valorParcelaBase.multiply(BigDecimal.valueOf(gasto.getTotalParcelas() - 1));
                        return gasto.getValor().subtract(totalJaPago);
                    } else {
                        return valorParcelaBase;
                    }
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalGastosDoMes = totalGastosFixos.add(totalGastosVariaveis).add(totalGastosParcelados);
        BigDecimal saldoDoMes = totalEntradas.subtract(totalGastosDoMes);

        DashboardDTO dto = new DashboardDTO();
        dto.setMesReferencia(mesReferenciaFormatado);
        dto.setSaldoAtual(saldoDoMes.setScale(2, RoundingMode.HALF_UP));
        dto.setTotalEntradas(totalEntradas.setScale(2, RoundingMode.HALF_UP));
        dto.setTotalGastosFixos(totalGastosFixos.setScale(2, RoundingMode.HALF_UP));
        dto.setTotalGastosVariaveis(totalGastosVariaveis.add(totalGastosParcelados).setScale(2, RoundingMode.HALF_UP));
        dto.setTotalLivre(saldoDoMes.setScale(2, RoundingMode.HALF_UP));

        BigDecimal percentualFixos = percentual(totalGastosFixos, totalEntradas);
        BigDecimal percentualVariaveis = percentual(totalGastosVariaveis.add(totalGastosParcelados), totalEntradas);
        BigDecimal percentualLivre = percentual(saldoDoMes, totalEntradas);

        dto.setPercentualFixos(percentualFixos.setScale(1, RoundingMode.HALF_UP));
        dto.setPercentualVariaveis(percentualVariaveis.setScale(1, RoundingMode.HALF_UP));
        dto.setPercentualLivre(percentualLivre.setScale(1, RoundingMode.HALF_UP));

        dto.setReservaEmergencia(buscarReservaEmergencia(usuario));
        dto.setMetasFinanceiras(transformarMetasEmDTO(metaFinanceiraRepository.findByUsuarioId(usuario.getId())));
        dto.setFluxoProjetadoProximosMeses(calcularFluxoProjetado(hoje, saldoDoMes, todasAsEntradas, todosOsGastos));

        List<DivisaoGastosDTO> divisaoGastos = Arrays.asList(
                criarDivisao("Fixos", totalGastosFixos, percentualFixos),
                criarDivisao("Variáveis e Parcelados", totalGastosVariaveis.add(totalGastosParcelados), percentualVariaveis),
                criarDivisao("Livre", saldoDoMes, percentualLivre)
        );
        dto.setDivisaoGastos(divisaoGastos);

        dto.setDiagnostico(gerarDiagnosticos(percentualFixos, percentualLivre, dto.getReservaEmergencia()));
        dto.setAlertas(gerarAlertas(saldoDoMes, dto.getReservaEmergencia()));

        return dto;
    }

    // Filtra entradas para o mês de referência (considera recorrentes)
    private List<Entrada> getEntradasParaMes(LocalDate mesDeReferencia, List<Entrada> todasAsEntradas) {
        LocalDate inicioDoMes = mesDeReferencia.withDayOfMonth(1);
        LocalDate fimDoMes = mesDeReferencia.withDayOfMonth(mesDeReferencia.lengthOfMonth());

        return todasAsEntradas.stream()
                .filter(entrada -> {
                    if (entrada.getRecorrente()) {
                        // Se for recorrente, conta a partir do mês de criação
                        return !mesDeReferencia.isBefore(entrada.getDataRecebimento().withDayOfMonth(1));
                    }
                    // Senão, só conta se estiver dentro do mês
                    return !entrada.getDataRecebimento().isBefore(inicioDoMes) && !entrada.getDataRecebimento().isAfter(fimDoMes);
                })
                .collect(Collectors.toList());
    }

    // Filtra gastos para o mês de referência (considera tipos diferentes)
    private List<Gasto> getGastosParaMes(LocalDate mesDeReferencia, List<Gasto> todosOsGastos) {
        LocalDate inicioDoMes = mesDeReferencia.withDayOfMonth(1);
        LocalDate fimDoMes = mesDeReferencia.withDayOfMonth(mesDeReferencia.lengthOfMonth());

        return todosOsGastos.stream()
                .filter(gasto -> {
                    switch (gasto.getTipo()) {
                        case FIXO:
                            // Gastos fixos valem a partir do mês de vencimento
                            return !mesDeReferencia.isBefore(gasto.getDataVencimento().withDayOfMonth(1));
                        case PARCELADO:
                            // Gastos parcelados valem dentro do período das parcelas
                            LocalDate dataPrimeiraParcela = gasto.getDataVencimento();
                            LocalDate dataUltimaParcela = dataPrimeiraParcela.plusMonths(gasto.getTotalParcelas() - 1);
                            return !mesDeReferencia.isBefore(dataPrimeiraParcela.withDayOfMonth(1)) && !mesDeReferencia.isAfter(dataUltimaParcela.withDayOfMonth(1));
                        case VARIAVEL:
                            // Gastos variáveis valem só no mês exato
                            return !gasto.getDataVencimento().isBefore(inicioDoMes) && !gasto.getDataVencimento().isAfter(fimDoMes);
                        default:
                            return false;
                    }
                })
                .collect(Collectors.toList());
    }

    // Calcula o fluxo de caixa projetado para os próximos meses
    private List<SaldoProjetadoDTO> calcularFluxoProjetado(LocalDate hoje, BigDecimal saldoInicialMesAtual, List<Entrada> todasAsEntradas, List<Gasto> todosOsGastos) {
        List<SaldoProjetadoDTO> fluxoProjetado = new ArrayList<>();
        BigDecimal saldoAcumulado = saldoInicialMesAtual;

        for (int i = 1; i <= 6; i++) { // Projeta pros próximos 6 meses
            LocalDate mesDaProjecao = hoje.plusMonths(i);

            List<Entrada> entradasProjetadas = getEntradasParaMes(mesDaProjecao, todasAsEntradas);
            List<Gasto> gastosProjetados = getGastosParaMes(mesDaProjecao, todosOsGastos);

            BigDecimal totalEntradasMes = entradasProjetadas.stream().map(Entrada::getValor).reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal totalGastosMes = gastosProjetados.stream()
                    .map(gasto -> {
                        if (gasto.getTipo() == Gasto.TipoGasto.PARCELADO) {
                            // Calcula o valor da parcela de gastos parcelados
                            BigDecimal valorParcelaBase = gasto.getValor()
                                    .divide(BigDecimal.valueOf(gasto.getTotalParcelas()), 2, RoundingMode.DOWN);

                            LocalDate dataPrimeiraParcela = gasto.getDataVencimento();
                            LocalDate dataUltimaParcela = dataPrimeiraParcela.plusMonths(gasto.getTotalParcelas() - 1);

                            boolean isUltimaParcela = mesDaProjecao.getYear() == dataUltimaParcela.getYear() &&
                                    mesDaProjecao.getMonth() == dataUltimaParcela.getMonth();

                            if (isUltimaParcela) {
                                // Ajusta pra última parcela se o valor for diferente
                                BigDecimal totalJaPago = valorParcelaBase.multiply(BigDecimal.valueOf(gasto.getTotalParcelas() - 1));
                                return gasto.getValor().subtract(totalJaPago);
                            }
                            return valorParcelaBase;
                        }
                        return gasto.getValorMensal(); // Pra outros tipos, pega o valor mensal
                    })
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal saldoDoMesProjetado = totalEntradasMes.subtract(totalGastosMes);
            saldoAcumulado = saldoAcumulado.add(saldoDoMesProjetado);

            String mesFormatado = mesDaProjecao.format(DateTimeFormatter.ofPattern("yyyy-MM"));
            SaldoProjetadoDTO saldoDto = new SaldoProjetadoDTO();
            saldoDto.setMes(mesFormatado);
            saldoDto.setSaldo(saldoAcumulado.setScale(2, RoundingMode.HALF_UP));
            fluxoProjetado.add(saldoDto);
        }
        return fluxoProjetado;
    }

    // Converte Metas Financeiras pra DTOs de resumo
    private List<MetaFinanceiraResumoDTO> transformarMetasEmDTO(List<MetaFinanceira> metas) {
        return metas.stream().map(meta -> {
            MetaFinanceiraResumoDTO dto = new MetaFinanceiraResumoDTO();
            dto.setId(meta.getId());
            dto.setUsuarioId(meta.getUsuario().getId());
            dto.setDescricao(meta.getDescricao());
            dto.setValorAtual(meta.getValorAtual());
            dto.setValorObjetivo(meta.getValorObjetivo());
            dto.setPrazoFinal(meta.getPrazoFinal());
            dto.setConcluida(meta.getConcluida());
            BigDecimal perc = meta.getValorObjetivo() != null && meta.getValorObjetivo().compareTo(BigDecimal.ZERO) > 0
                    ? meta.getValorAtual().divide(meta.getValorObjetivo(), 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                    : BigDecimal.ZERO;
            dto.setPercentualConcluido(perc.setScale(2, RoundingMode.HALF_UP));
            return dto;
        }).collect(Collectors.toList());
    }

    // Calcula o percentual de um valor em relação a um total
    private BigDecimal percentual(BigDecimal valor, BigDecimal total) {
        if (total == null || total.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
        return valor.divide(total, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
    }

    // Busca a reserva de emergência do usuário
    private ReservaEmergenciaDTO buscarReservaEmergencia(User usuario) {
        Optional<ReservaEmergencia> reservaOpt = reservaEmergenciaRepository.findByUsuario(usuario);
        if (reservaOpt.isPresent()) {
            ReservaEmergencia reserva = reservaOpt.get();
            ReservaEmergenciaDTO dto = new ReservaEmergenciaDTO();
            dto.setId(reserva.getId());
            dto.setUsuarioId(reserva.getUsuario().getId());
            dto.setValorAtual(reserva.getValorAtual());
            dto.setValorObjetivo(reserva.getValorObjetivo());
            dto.setPercentualConcluido(reserva.getPercentualConcluido());
            dto.setAtiva(reserva.getAtiva());
            dto.setDataCriacao(reserva.getDataCriacao());
            dto.setUltimaAtualizacao(reserva.getUltimaAtualizacao());
            return dto;
        }
        return null; // Retorna nulo se não houver reserva
    }

    // Cria um DTO para a divisão de gastos
    private DivisaoGastosDTO criarDivisao(String tipo, BigDecimal valor, BigDecimal percentual) {
        DivisaoGastosDTO dto = new DivisaoGastosDTO();
        dto.setTipo(tipo);
        dto.setValor(valor.setScale(2, RoundingMode.HALF_UP));
        dto.setPercentual(percentual.setScale(1, RoundingMode.HALF_UP));
        return dto;
    }

    // Gera diagnósticos financeiros pro usuário
    private List<String> gerarDiagnosticos(BigDecimal percentualFixos, BigDecimal percentualLivre, ReservaEmergenciaDTO reserva) {
        List<String> diags = new ArrayList<>();
        if (percentualFixos.compareTo(BigDecimal.valueOf(50)) <= 0) {
            diags.add("Seus gastos fixos representam " + percentualFixos.setScale(1, RoundingMode.HALF_UP) + "% da sua renda, um nível saudável!");
        } else {
            diags.add("Atenção: seus gastos fixos (" + percentualFixos.setScale(1, RoundingMode.HALF_UP) + "%) parecem elevados.");
        }
        if (reserva != null && reserva.getPercentualConcluido() != null) {
            if (reserva.getPercentualConcluido().compareTo(BigDecimal.valueOf(100)) >= 0) {
                diags.add("Parabéns! Sua reserva de emergência está 100% concluída.");
            } else {
                diags.add("Sua reserva de emergência está " + reserva.getPercentualConcluido().setScale(1, RoundingMode.HALF_UP) + "% concluída.");
            }
        }
        if (percentualLivre.compareTo(BigDecimal.ZERO) > 0) {
            diags.add("Você tem " + percentualLivre.setScale(1, RoundingMode.HALF_UP) + "% da sua renda livre para investir ou usar como quiser. Ótimo!");
        }
        return diags;
    }

    // Gera alertas financeiros pro usuário
    private List<String> gerarAlertas(BigDecimal saldoDoMes, ReservaEmergenciaDTO reserva) {
        List<String> alertas = new ArrayList<>();
        if (saldoDoMes.compareTo(BigDecimal.ZERO) < 0) {
            alertas.add("Alerta: seu saldo neste mês ficou negativo em " + saldoDoMes.abs().setScale(2, RoundingMode.HALF_UP) + ".");
        }
        if (reserva == null || reserva.getValorAtual() == null || reserva.getValorAtual().compareTo(BigDecimal.ZERO) <= 0) {
            alertas.add("Você ainda não começou a construir sua reserva de emergência. É um passo importante!");
        }
        return alertas;
    }

    // Pega o histórico mensal de entradas e gastos
    public List<HistoricoMensalDTO> getHistoricoMensal(Authentication authentication) {
        User usuario = userService.findByUsername(authentication.getName());
        Long usuarioId = usuario.getId();

        // Pega todas as entradas e gastos do usuário
        List<Entrada> todasAsEntradas = entradaRepository.findAllByUsuarioId(usuarioId);
        List<Gasto> todosOsGastos = gastoRepository.findAllByUsuarioId(usuarioId);

        // Define o período dos últimos 6 meses
        LocalDate dataFim = LocalDate.now();
        LocalDate dataInicio = dataFim.minusMonths(5).withDayOfMonth(1); // 6 meses, contando o mês atual
        YearMonth mesCorrente = YearMonth.from(dataInicio);
        YearMonth ultimoMes = YearMonth.from(dataFim);

        List<HistoricoMensalDTO> historico = new ArrayList<>();
        DateTimeFormatter formatadorMes = DateTimeFormatter.ofPattern("yyyy-MM");

        // Itera por cada mês no período
        while (!mesCorrente.isAfter(ultimoMes)) {
            final YearMonth mesSendoCalculado = mesCorrente;

            // Calcula o total de entradas para o mês atual
            BigDecimal totalEntradasMes = todasAsEntradas.stream()
                    .filter(entrada -> {
                        YearMonth mesDaEntrada = YearMonth.from(entrada.getDataRecebimento());
                        if (entrada.getRecorrente()) {
                            // Recorrente: conta se o mês atual for igual ou depois do início da entrada
                            return !mesSendoCalculado.isBefore(mesDaEntrada);
                        } else {
                            // Não recorrente: conta só no mês exato do registro
                            return mesSendoCalculado.equals(mesDaEntrada);
                        }
                    })
                    .map(Entrada::getValor)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Calcula o total de gastos para o mês atual
            BigDecimal totalGastosMes = todosOsGastos.stream()
                    .filter(gasto -> {
                        YearMonth mesInicioGasto = YearMonth.from(gasto.getDataVencimento());
                        switch (gasto.getTipo()) {
                            case FIXO:
                                // Fixo: como entrada recorrente
                                return !mesSendoCalculado.isBefore(mesInicioGasto);
                            case VARIAVEL:
                                // Variável: só no mês exato
                                return mesSendoCalculado.equals(mesInicioGasto);
                            case PARCELADO:
                                // Parcelado: dentro do período das parcelas
                                if (gasto.getTotalParcelas() == null || gasto.getTotalParcelas() <= 0) return false;
                                YearMonth mesFimGasto = mesInicioGasto.plusMonths(gasto.getTotalParcelas() - 1);
                                return !mesSendoCalculado.isBefore(mesInicioGasto) && !mesSendoCalculado.isAfter(mesFimGasto);
                            default:
                                return false;
                        }
                    })
                    // Pega o valor mensal do gasto (já considera parcelas)
                    .map(Gasto::getValorMensal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Adiciona o resultado à lista
            historico.add(new HistoricoMensalDTO(mesSendoCalculado.format(formatadorMes), totalEntradasMes, totalGastosMes));

            // Avança para o próximo mês
            mesCorrente = mesCorrente.plusMonths(1);
        }

        return historico;
    }
}