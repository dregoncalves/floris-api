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
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
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

    public DashboardDTO gerarDashboard(Authentication authentication) {
        User usuario = userService.findByUsername(authentication.getName());
        LocalDate hoje = LocalDate.now();
        String mesReferencia = hoje.format(DateTimeFormatter.ofPattern("yyyy-MM"));

        // --- Otimização: Busca apenas as transações do mês atual ---
        // NOTA: Você precisa criar estes métodos nos seus JpaRepository
        // Ex: List<Gasto> findByUsuarioIdAndDataVencimentoBetween(Long id, LocalDate start, LocalDate end);
        LocalDate inicioDoMes = hoje.withDayOfMonth(1);
        LocalDate fimDoMes = hoje.withDayOfMonth(hoje.lengthOfMonth());

        List<Entrada> entradasMes = entradaRepository.findAllByUsuarioIdAndDataRecebimentoBetween(usuario.getId(), inicioDoMes, fimDoMes);

        // Para os gastos, precisamos de uma lógica um pouco diferente para pegar parcelas de meses anteriores
        // A forma mais simples é buscar todos e filtrar na aplicação, já que a projeção fará isso de qualquer forma.
        List<Gasto> todosOsGastos = gastoRepository.findAllByUsuarioId(usuario.getId());

        List<Gasto> gastosDoMes = todosOsGastos.stream().filter(gasto -> {
            if (gasto.getTipo() != Gasto.TipoGasto.PARCELADO) {
                return !gasto.getDataVencimento().isBefore(inicioDoMes) && !gasto.getDataVencimento().isAfter(fimDoMes);
            } else {
                LocalDate dataPrimeiraParcela = gasto.getDataVencimento();
                LocalDate dataUltimaParcela = dataPrimeiraParcela.plusMonths(gasto.getTotalParcelas() - 1);
                return !hoje.isBefore(dataPrimeiraParcela.withDayOfMonth(1)) && !hoje.isAfter(dataUltimaParcela.withDayOfMonth(1));
            }
        }).collect(Collectors.toList());


        // --- Cálculos Focados no Mês de Referência ---
        BigDecimal totalEntradas = entradasMes.stream()
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
                .map(Gasto::getValorMensal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalGastosDoMes = totalGastosFixos.add(totalGastosVariaveis).add(totalGastosParcelados);
        BigDecimal saldoDoMes = totalEntradas.subtract(totalGastosDoMes);

        // --- Montagem do DTO com dados consistentes ---
        DashboardDTO dto = new DashboardDTO();
        dto.setMesReferencia(mesReferencia);
        dto.setSaldoAtual(saldoDoMes.setScale(2, RoundingMode.HALF_UP));
        dto.setTotalEntradas(totalEntradas.setScale(2, RoundingMode.HALF_UP));
        dto.setTotalGastosFixos(totalGastosFixos.setScale(2, RoundingMode.HALF_UP));
        dto.setTotalGastosVariaveis(totalGastosVariaveis.setScale(2, RoundingMode.HALF_UP));
        dto.setTotalLivre(saldoDoMes.setScale(2, RoundingMode.HALF_UP));

        BigDecimal percentualFixos = percentual(totalGastosFixos, totalEntradas);
        BigDecimal percentualVariaveis = percentual(totalGastosVariaveis.add(totalGastosParcelados), totalEntradas); // Somando parcelado aqui por simplicidade
        BigDecimal percentualLivre = percentual(saldoDoMes, totalEntradas);

        dto.setPercentualFixos(percentualFixos.setScale(1, RoundingMode.HALF_UP));
        dto.setPercentualVariaveis(percentualVariaveis.setScale(1, RoundingMode.HALF_UP));
        dto.setPercentualLivre(percentualLivre.setScale(1, RoundingMode.HALF_UP));

        // --- Lógica Auxiliar e Projeções ---
        dto.setReservaEmergencia(buscarReservaEmergencia(usuario));
        List<MetaFinanceira> metas = metaFinanceiraRepository.findByUsuarioId(usuario.getId());
        dto.setMetasFinanceiras(transformarMetasEmDTO(metas));

        // A projeção começa a partir do saldo deste mês
        List<SaldoProjetadoDTO> fluxoProjetado = calcularFluxoProjetadoProximosMeses(usuario, hoje, saldoDoMes);
        dto.setFluxoProjetadoProximosMeses(fluxoProjetado);

        // Divisão de gastos agora reflete o parcelado
        List<DivisaoGastosDTO> divisaoGastos = Arrays.asList(
                criarDivisao("Fixos", totalGastosFixos, percentualFixos),
                criarDivisao("Variáveis e Parcelados", totalGastosVariaveis.add(totalGastosParcelados), percentualVariaveis),
                criarDivisao("Livre", saldoDoMes, percentualLivre)
        );
        dto.setDivisaoGastos(divisaoGastos);

        dto.setDiagnostico(gerarDiagnosticos(percentualFixos, percentualVariaveis, percentualLivre, dto.getReservaEmergencia()));
        dto.setAlertas(gerarAlertas(percentualFixos, percentualVariaveis, saldoDoMes, dto.getReservaEmergencia()));

        return dto;
    }

    // ==== Métodos Auxiliares ====

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

    private BigDecimal percentual(BigDecimal valor, BigDecimal total) {
        if (total == null || total.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
        return valor.divide(total, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
    }

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
        } else {
            return null;
        }
    }

    private List<SaldoProjetadoDTO> calcularFluxoProjetadoProximosMeses(User usuario, LocalDate hoje, BigDecimal saldoInicial) {
        List<SaldoProjetadoDTO> fluxoProjetado = new ArrayList<>();
        BigDecimal saldoAcumulado = saldoInicial;

        List<Entrada> todasAsEntradas = entradaRepository.findAllByUsuarioId(usuario.getId());
        List<Gasto> todosOsGastos = gastoRepository.findAllByUsuarioId(usuario.getId());

        for (int i = 1; i <= 6; i++) {
            LocalDate mesDaProjecao = hoje.plusMonths(i);
            LocalDate inicioDoMesProjecao = mesDaProjecao.withDayOfMonth(1);
            LocalDate fimDoMesProjecao = mesDaProjecao.withDayOfMonth(mesDaProjecao.lengthOfMonth());

            BigDecimal totalEntradasMes = todasAsEntradas.stream()
                    .filter(entrada -> {
                        if (entrada.getRecorrente()) {
                            return !mesDaProjecao.isBefore(entrada.getDataRecebimento().withDayOfMonth(1));
                        }
                        return !entrada.getDataRecebimento().isBefore(inicioDoMesProjecao) && !entrada.getDataRecebimento().isAfter(fimDoMesProjecao);
                    })
                    .map(Entrada::getValor)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal totalGastosMes = todosOsGastos.stream()
                    .filter(gasto -> {
                        switch (gasto.getTipo()) {
                            case PARCELADO:
                                LocalDate dataPrimeiraParcela = gasto.getDataVencimento();
                                LocalDate dataUltimaParcela = dataPrimeiraParcela.plusMonths(gasto.getTotalParcelas() - 1);
                                return !mesDaProjecao.isBefore(dataPrimeiraParcela.withDayOfMonth(1)) && !mesDaProjecao.isAfter(dataUltimaParcela.withDayOfMonth(1));
                            case FIXO:
                                return !mesDaProjecao.isBefore(gasto.getDataVencimento().withDayOfMonth(1));
                            case VARIAVEL:
                                return !gasto.getDataVencimento().isBefore(inicioDoMesProjecao) && !gasto.getDataVencimento().isAfter(fimDoMesProjecao);
                            default:
                                return false;
                        }
                    })
                    .map(Gasto::getValorMensal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            saldoAcumulado = saldoAcumulado.add(totalEntradasMes).subtract(totalGastosMes);

            String mesFormatado = inicioDoMesProjecao.format(DateTimeFormatter.ofPattern("yyyy-MM"));
            SaldoProjetadoDTO saldoDto = new SaldoProjetadoDTO();
            saldoDto.setMes(mesFormatado);
            saldoDto.setSaldo(saldoAcumulado.setScale(2, RoundingMode.HALF_UP));
            fluxoProjetado.add(saldoDto);
        }
        return fluxoProjetado;
    }

    private DivisaoGastosDTO criarDivisao(String tipo, BigDecimal valor, BigDecimal percentual) {
        DivisaoGastosDTO dto = new DivisaoGastosDTO();
        dto.setTipo(tipo);
        dto.setValor(valor.setScale(2, RoundingMode.HALF_UP));
        dto.setPercentual(percentual.setScale(1, RoundingMode.HALF_UP));
        return dto;
    }

    private List<String> gerarDiagnosticos(BigDecimal percentualFixos, BigDecimal percentualVariaveis, BigDecimal percentualLivre, ReservaEmergenciaDTO reserva) {
        List<String> diags = new ArrayList<>();
        if (percentualFixos.compareTo(BigDecimal.valueOf(50)) <= 0) {
            diags.add("Seus gastos fixos representam " + percentualFixos.setScale(1, RoundingMode.HALF_UP) + "% da sua renda, um nível saudável!");
        } else {
            diags.add("Atenção: seus gastos fixos (" + percentualFixos.setScale(1, RoundingMode.HALF_UP) + "%) parecem elevados.");
        }
        if (reserva != null && reserva.getPercentualConcluido() != null) {
            diags.add("Sua reserva de emergência está " + reserva.getPercentualConcluido().setScale(1, RoundingMode.HALF_UP) + "% concluída.");
        }
        return diags;
    }

    private List<String> gerarAlertas(BigDecimal percentualFixos, BigDecimal percentualVariaveis, BigDecimal saldoDoMes, ReservaEmergenciaDTO reserva) {
        List<String> alertas = new ArrayList<>();
        if (saldoDoMes.compareTo(BigDecimal.ZERO) < 0) {
            alertas.add("Alerta: seu saldo neste mês ficou negativo.");
        }
        if (percentualVariaveis.compareTo(BigDecimal.valueOf(35)) > 0) {
            alertas.add("Fique de olho nos seus gastos variáveis e parcelados, eles estão consumindo " + percentualVariaveis.setScale(1, RoundingMode.HALF_UP) + "% da sua renda.");
        }
        if (reserva != null && reserva.getValorAtual().compareTo(BigDecimal.ZERO) <= 0) {
            alertas.add("Você ainda não começou a construir sua reserva de emergência. É um passo importante!");
        }
        return alertas;
    }
}