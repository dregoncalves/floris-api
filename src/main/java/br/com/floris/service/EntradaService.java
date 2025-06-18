package br.com.floris.service;

import br.com.floris.dto.entradas.EntradaRequestDTO;
import br.com.floris.dto.entradas.EntradaResponseDTO;
import br.com.floris.model.Entrada;
import br.com.floris.model.User;
import br.com.floris.repository.EntradaRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
public class EntradaService {

    private final EntradaRepository repository;
    private final UserService userService;

    public EntradaService(EntradaRepository repository, UserService userService) {
        this.repository = repository;
        this.userService = userService;
    }

    // Lista as entradas de um usuário, com paginação
    public Page<EntradaResponseDTO> listarEntradasDoUsuario(Authentication authentication, Pageable pageable) {
        User usuario = getUsuarioAutenticado(authentication);
        Page<Entrada> entradasPage = repository.findByUsuarioId(usuario.getId(), pageable);
        return entradasPage.map(EntradaResponseDTO::fromModel);
    }

    // Busca uma entrada pelo ID, garantindo que pertence ao usuário
    public EntradaResponseDTO buscarPorId(Long id, Authentication authentication) {
        User usuario = getUsuarioAutenticado(authentication);
        Entrada entrada = repository.findByIdAndUsuarioId(id, usuario.getId())
                .orElseThrow(() -> new RuntimeException("Entrada não encontrada ou não pertence ao usuário"));
        return EntradaResponseDTO.fromModel(entrada);
    }

    // Cria uma nova entrada pro usuário logado
    public EntradaResponseDTO criar(EntradaRequestDTO dto, Authentication authentication) {
        User usuario = getUsuarioAutenticado(authentication);
        Entrada entrada = Entrada.builder()
                .descricao(dto.descricao())
                .valor(dto.valor())
                .dataRecebimento(dto.dataRecebimento())
                .tipo(dto.tipo())
                .recorrente(dto.recorrente())
                .usuario(usuario)
                .build();

        Entrada saved = repository.save(entrada);
        return EntradaResponseDTO.fromModel(saved);
    }

    // Atualiza uma entrada existente, verificando se pertence ao usuário
    public EntradaResponseDTO atualizar(Long id, EntradaRequestDTO dto, Authentication authentication) {
        User usuario = getUsuarioAutenticado(authentication);
        Entrada entrada = repository.findByIdAndUsuarioId(id, usuario.getId())
                .orElseThrow(() -> new RuntimeException("Entrada não encontrada ou não pertence ao usuário"));

        entrada.setDescricao(dto.descricao());
        entrada.setValor(dto.valor());
        entrada.setDataRecebimento(dto.dataRecebimento());
        entrada.setTipo(dto.tipo());
        entrada.setRecorrente(dto.recorrente());

        Entrada updated = repository.save(entrada);
        return EntradaResponseDTO.fromModel(updated);
    }

    // Deleta uma entrada, verificando se pertence ao usuário
    public void deletar(Long id, Authentication authentication) {
        User usuario = getUsuarioAutenticado(authentication);
        Entrada entrada = repository.findByIdAndUsuarioId(id, usuario.getId())
                .orElseThrow(() -> new RuntimeException("Entrada não encontrada ou não pertence ao usuário"));

        repository.delete(entrada);
    }

    // Pega o usuário logado a partir da autenticação
    private User getUsuarioAutenticado(Authentication authentication) {
        String username = authentication.getName();
        return userService.findByUsername(username);
    }
}