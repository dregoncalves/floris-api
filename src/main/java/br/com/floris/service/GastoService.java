package br.com.floris.service;

import br.com.floris.dto.GastoRequestDTO;
import br.com.floris.dto.GastoResponseDTO;
import br.com.floris.model.Gasto;
import br.com.floris.model.User;
import br.com.floris.repository.GastoRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class GastoService {

    private final GastoRepository repository;
    private final UserService userService;

    public GastoService(GastoRepository repository, UserService userService) {
        this.repository = repository;
        this.userService = userService;
    }

    public List<GastoResponseDTO> listarGastosDoUsuario(Authentication authentication) {
        User usuario = getUsuarioAutenticado(authentication);
        return repository.findByUsuarioId(usuario.getId()).stream()
                .map(GastoResponseDTO::fromModel)
                .collect(Collectors.toList());
    }

    public GastoResponseDTO buscarPorId(Long id, Authentication authentication) {
        User usuario = getUsuarioAutenticado(authentication);
        Gasto gasto = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Gasto não encontrado"));
        if (!gasto.getUsuario().getId().equals(usuario.getId())) {
            throw new RuntimeException("Acesso negado");
        }
        return GastoResponseDTO.fromModel(gasto);
    }

    public GastoResponseDTO criar(GastoRequestDTO dto, Authentication authentication) {
        User usuario = getUsuarioAutenticado(authentication);
        Gasto gasto = Gasto.builder()
                .descricao(dto.descricao())
                .valor(dto.valor())
                .tipo(dto.tipo())
                .dataVencimento(dto.dataVencimento())
                .numeroParcelaAtual(dto.numeroParcelaAtual())
                .totalParcelas(dto.totalParcelas())
                .gastoCartao(dto.gastoCartao())
                .pago(dto.pago())
                .usuario(usuario)
                .build();

        Gasto saved = repository.save(gasto);
        return GastoResponseDTO.fromModel(saved);
    }

    public GastoResponseDTO atualizar(Long id, GastoRequestDTO dto, Authentication authentication) {
        User usuario = getUsuarioAutenticado(authentication);
        Gasto gasto = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Gasto não encontrado"));

        if (!gasto.getUsuario().getId().equals(usuario.getId())) {
            throw new RuntimeException("Acesso negado");
        }

        gasto.setDescricao(dto.descricao());
        gasto.setValor(dto.valor());
        gasto.setTipo(dto.tipo());
        gasto.setDataVencimento(dto.dataVencimento());
        gasto.setNumeroParcelaAtual(dto.numeroParcelaAtual());
        gasto.setTotalParcelas(dto.totalParcelas());
        gasto.setGastoCartao(dto.gastoCartao());
        gasto.setPago(dto.pago());

        Gasto updated = repository.save(gasto);
        return GastoResponseDTO.fromModel(updated);
    }

    public void deletar(Long id, Authentication authentication) {
        User usuario = getUsuarioAutenticado(authentication);
        Gasto gasto = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Gasto não encontrado"));

        if (!gasto.getUsuario().getId().equals(usuario.getId())) {
            throw new RuntimeException("Acesso negado");
        }
        repository.delete(gasto);
    }

    private User getUsuarioAutenticado(Authentication authentication) {
        String username = authentication.getName();
        return userService.findByUsername(username);
    }
}