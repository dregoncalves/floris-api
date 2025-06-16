package br.com.floris.service;

import br.com.floris.dto.*;
import br.com.floris.model.Entrada;
import br.com.floris.model.Gasto;
import br.com.floris.model.MetaFinanceira;
import br.com.floris.model.ReservaEmergencia; // supondo que exista esse Model
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

    public DashboardDTO gerarDashboard(Authentication authentication) {
        User usuario = userService.findByUsername(authentication.getName());
        LocalDate hoje = LocalDate.now();
        String mesReferencia = hoje.format(DateTimeFormatter.ofPattern("yyyy-MM"));

        List<Entrada> entradas = entradaRepository.findByUsuarioId(usuario.getId());
        List<Gasto> gastos = gastoRepository.findByUsuarioId(usuario.getId());
        List<MetaFinanceira> metas = metaFinanceiraRepository.findByUsuarioId(usuario.getId());

        // Entradas/Gastos do mês atual
        List<Entrada> entradasMes = entradas.stream()
                .filter(e -> isSameMonth(e.getDataRecebimento(), hoje))
                .collect(Collectors.toList());

        List<Gasto> gastosMes = gastos.stream()
                .filter(g -> isSameMonth(g.getDataVencimento(), hoje))
                .collect(Collectors.toList());

        BigDecimal totalEntradas = entradasMes.stream()
                .map(Entrada::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalGastosFixos = gastosMes.stream()
                .filter(g -> g.getTipo() == Gasto.TipoGasto.FIXO)
                .map(Gasto::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalGastosVariaveis = gastosMes.stream()
                .filter(g -> g.getTipo() == Gasto.TipoGasto.VARIAVEL)
                .map(Gasto::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalLivre = totalEntradas.subtract(totalGastosFixos).subtract(totalGastosVariaveis);

        BigDecimal percentualFixos = percentual(totalGastosFixos, totalEntradas);
        BigDecimal percentualVariaveis = percentual(totalGastosVariaveis, totalEntradas);
        BigDecimal percentualLivre = percentual(totalLivre, totalEntradas);

        BigDecimal saldoAtual = calcularSaldoAteHoje(entradas, gastos, hoje);

        ReservaEmergenciaDTO reservaEmergencia = buscarReservaEmergencia(usuario);

        List<MetaFinanceiraResumoDTO> metasResumo = metas.stream().map(meta -> {
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

        List<SaldoProjetadoDTO> fluxoProjetado = calcularFluxoProjetadoProximosMeses(entradas, gastos, hoje, saldoAtual);

        List<DivisaoGastosDTO> divisaoGastos = Arrays.asList(
                criarDivisao("Fixos", totalGastosFixos, percentualFixos),
                criarDivisao("Variaveis", totalGastosVariaveis, percentualVariaveis),
                criarDivisao("Livre", totalLivre, percentualLivre)
        );

        List<String> diagnostico = gerarDiagnosticos(percentualFixos, percentualVariaveis, percentualLivre, reservaEmergencia);
        List<String> alertas = gerarAlertas(percentualFixos, percentualVariaveis, saldoAtual, reservaEmergencia);

        DashboardDTO dto = new DashboardDTO();
        dto.setMesReferencia(mesReferencia);
        dto.setSaldoAtual(saldoAtual.setScale(2, RoundingMode.HALF_UP));
        dto.setTotalEntradas(totalEntradas.setScale(2, RoundingMode.HALF_UP));
        dto.setTotalGastosFixos(totalGastosFixos.setScale(2, RoundingMode.HALF_UP));
        dto.setTotalGastosVariaveis(totalGastosVariaveis.setScale(2, RoundingMode.HALF_UP));
        dto.setTotalLivre(totalLivre.setScale(2, RoundingMode.HALF_UP));
        dto.setPercentualFixos(percentualFixos.setScale(1, RoundingMode.HALF_UP));
        dto.setPercentualVariaveis(percentualVariaveis.setScale(1, RoundingMode.HALF_UP));
        dto.setPercentualLivre(percentualLivre.setScale(1, RoundingMode.HALF_UP));
        dto.setReservaEmergencia(reservaEmergencia);
        dto.setMetasFinanceiras(metasResumo);
        dto.setFluxoProjetadoProximosMeses(fluxoProjetado);
        dto.setDivisaoGastos(divisaoGastos);
        dto.setDiagnostico(diagnostico);
        dto.setAlertas(alertas);

        return dto;
    }

    // ==== Métodos Auxiliares ====

    private boolean isSameMonth(LocalDate data, LocalDate referencia) {
        return data != null && data.getYear() == referencia.getYear() && data.getMonth() == referencia.getMonth();
    }

    private BigDecimal percentual(BigDecimal valor, BigDecimal total) {
        if (total == null || total.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
        return valor.divide(total, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
    }

    private BigDecimal calcularSaldoAteHoje(List<Entrada> entradas, List<Gasto> gastos, LocalDate hoje) {
        BigDecimal totalEntradas = entradas.stream()
                .filter(e -> !e.getDataRecebimento().isAfter(hoje))
                .map(Entrada::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalGastos = gastos.stream()
                .filter(g -> !g.getDataVencimento().isAfter(hoje))
                .map(Gasto::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return totalEntradas.subtract(totalGastos);
    }

    private ReservaEmergenciaDTO buscarReservaEmergencia(User usuario) {
        // Busca real, adaptando ao seu Model/Repository
        Optional<ReservaEmergencia> reservaOpt = reservaEmergenciaRepository.findByUsuario(usuario);
        if (reservaOpt.isPresent()) {
            ReservaEmergencia reserva = reservaOpt.get();
            ReservaEmergenciaDTO dto = new ReservaEmergenciaDTO();
            dto.setId(reserva.getId());
            dto.setUsuarioId(reserva.getUsuario().getId());
            dto.setValorAtual(reserva.getValorAtual());
            dto.setValorObjetivo(reserva.getValorObjetivo());
            BigDecimal percentual = reserva.getValorObjetivo() != null && reserva.getValorObjetivo().compareTo(BigDecimal.ZERO) > 0
                    ? reserva.getValorAtual().divide(reserva.getValorObjetivo(), 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                    : BigDecimal.ZERO;
            dto.setPercentualConcluido(percentual.setScale(2, RoundingMode.HALF_UP));
            dto.setAtiva(reserva.getAtiva());
            dto.setDataCriacao(reserva.getDataCriacao());
            dto.setUltimaAtualizacao(reserva.getUltimaAtualizacao());
            return dto;
        } else {
            return null;
        }
    }

    private List<SaldoProjetadoDTO> calcularFluxoProjetadoProximosMeses(List<Entrada> entradas, List<Gasto> gastos, LocalDate hoje, BigDecimal saldoAtual) {
        List<SaldoProjetadoDTO> fluxo = new ArrayList<>();
        BigDecimal saldo = saldoAtual;
        for (int i = 1; i <= 3; i++) {
            LocalDate inicioMes = hoje.plusMonths(i).withDayOfMonth(1);
            LocalDate fimMes = inicioMes.withDayOfMonth(inicioMes.lengthOfMonth());

            BigDecimal entradasMes = entradas.stream()
                    .filter(e -> !e.getDataRecebimento().isBefore(inicioMes) && !e.getDataRecebimento().isAfter(fimMes))
                    .map(Entrada::getValor)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal gastosMes = gastos.stream()
                    .filter(g -> !g.getDataVencimento().isBefore(inicioMes) && !g.getDataVencimento().isAfter(fimMes))
                    .map(Gasto::getValor)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            saldo = saldo.add(entradasMes).subtract(gastosMes);
            String mes = inicioMes.format(DateTimeFormatter.ofPattern("yyyy-MM"));
            SaldoProjetadoDTO saldoDto = new SaldoProjetadoDTO();
            saldoDto.setMes(mes);
            saldoDto.setSaldo(saldo.setScale(2, RoundingMode.HALF_UP));
            fluxo.add(saldoDto);
        }
        return fluxo;
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
            diags.add("Seus gastos fixos estão em " + percentualFixos + "% da renda — saudável!");
        } else {
            diags.add("Gastos fixos elevados! (" + percentualFixos + "% da renda)");
        }
        if (reserva != null && reserva.getPercentualConcluido() != null) {
            diags.add("Reserva de emergência em " + reserva.getPercentualConcluido().setScale(1, RoundingMode.HALF_UP) + "% da meta (R$" +
                    reserva.getValorAtual().setScale(2, RoundingMode.HALF_UP) + " de R$" +
                    reserva.getValorObjetivo().setScale(2, RoundingMode.HALF_UP) + ").");
        }
        diags.add("Saldo projetado positivo nos próximos meses.");
        return diags;
    }

    private List<String> gerarAlertas(BigDecimal percentualFixos, BigDecimal percentualVariaveis, BigDecimal saldoAtual, ReservaEmergenciaDTO reserva) {
        List<String> alertas = new ArrayList<>();
        if (saldoAtual.compareTo(BigDecimal.ZERO) < 0) {
            alertas.add("Saldo negativo");
        }
        if (percentualFixos.compareTo(BigDecimal.valueOf(60)) > 0) {
            alertas.add("Atenção: gastos fixos altos.");
        }
        if (percentualVariaveis.compareTo(BigDecimal.valueOf(20)) > 0) {
            alertas.add("Fique atento aos gastos variáveis — subiram em relação ao mês anterior.");
        }
        if (reserva != null && reserva.getPercentualConcluido() != null &&
                reserva.getPercentualConcluido().compareTo(BigDecimal.valueOf(20)) < 0) {
            alertas.add("Sua reserva de emergência está abaixo de 20% da meta.");
        }
        return alertas;
    }
}