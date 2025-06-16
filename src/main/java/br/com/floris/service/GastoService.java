package br.com.floris.service;

import br.com.floris.dto.gastos.GastoRequestDTO;
import br.com.floris.dto.gastos.GastoResponseDTO;
import br.com.floris.model.Gasto;
import br.com.floris.model.User;
import br.com.floris.repository.GastoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class GastoService {

    private final GastoRepository repository;
    private final UserService userService;

    public GastoService(GastoRepository repository, UserService userService) {
        this.repository = repository;
        this.userService = userService;
    }

    public Page<GastoResponseDTO> listarGastosDoUsuario(Authentication authentication, Pageable pageable) {
        User usuario = getUsuarioAutenticado(authentication);
        Page<Gasto> gastosPage = repository.findByUsuarioId(usuario.getId(), pageable);
        
        return gastosPage.map(GastoResponseDTO::fromModel);
    }

    public GastoResponseDTO buscarPorId(Long id, Authentication authentication) {
        User usuario = getUsuarioAutenticado(authentication);
        Gasto gasto = repository.findByIdAndUsuarioId(id, usuario.getId())
                .orElseThrow(() -> new RuntimeException("Gasto não encontrado ou não pertence ao usuário"));

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
        // Use o método inteligente aqui também!
        Gasto gasto = repository.findByIdAndUsuarioId(id, usuario.getId())
                .orElseThrow(() -> new RuntimeException("Gasto não encontrado ou não pertence ao usuário"));

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
        // E aqui também!
        Gasto gasto = repository.findByIdAndUsuarioId(id, usuario.getId())
                .orElseThrow(() -> new RuntimeException("Gasto não encontrado ou não pertence ao usuário"));

        repository.delete(gasto);
    }

    private User getUsuarioAutenticado(Authentication authentication) {
        String username = authentication.getName();
        return userService.findByUsername(username);
    }
}