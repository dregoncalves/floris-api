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

    // Lista os gastos de um usuário, com paginação
    public Page<GastoResponseDTO> listarGastosDoUsuario(Authentication authentication, Pageable pageable) {
        User usuario = getUsuarioAutenticado(authentication);
        Page<Gasto> gastosPage = repository.findByUsuarioId(usuario.getId(), pageable);

        return gastosPage.map(GastoResponseDTO::fromModel);
    }

    // Busca um gasto pelo ID, garantindo que pertence ao usuário logado
    public GastoResponseDTO buscarPorId(Long id, Authentication authentication) {
        User usuario = getUsuarioAutenticado(authentication);
        Gasto gasto = repository.findByIdAndUsuarioId(id, usuario.getId())
                .orElseThrow(() -> new RuntimeException("Gasto não encontrado ou não pertence ao usuário"));

        return GastoResponseDTO.fromModel(gasto);
    }

    // Cria um novo gasto pro usuário logado
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

    // Atualiza um gasto existente, verificando se pertence ao usuário
    public GastoResponseDTO atualizar(Long id, GastoRequestDTO dto, Authentication authentication) {
        User usuario = getUsuarioAutenticado(authentication);
        // Busca o gasto e verifica se é do usuário
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

    // Deleta um gasto, verificando se pertence ao usuário
    public void deletar(Long id, Authentication authentication) {
        User usuario = getUsuarioAutenticado(authentication);
        // Busca o gasto e verifica se é do usuário
        Gasto gasto = repository.findByIdAndUsuarioId(id, usuario.getId())
                .orElseThrow(() -> new RuntimeException("Gasto não encontrado ou não pertence ao usuário"));

        repository.delete(gasto);
    }

    // Pega o usuário logado a partir da autenticação
    private User getUsuarioAutenticado(Authentication authentication) {
        String username = authentication.getName();
        return userService.findByUsername(username);
    }
}