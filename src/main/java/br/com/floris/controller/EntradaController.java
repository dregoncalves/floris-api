package br.com.floris.controller;

import br.com.floris.dto.EntradaRequestDTO;
import br.com.floris.dto.EntradaResponseDTO;
import br.com.floris.service.EntradaService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/entradas")
public class EntradaController {

    private final EntradaService service;

    public EntradaController(EntradaService service) {
        this.service = service;
    }

    @GetMapping
    public List<EntradaResponseDTO> listar(Authentication authentication) {
        return service.listarEntradasDoUsuario(authentication);
    }

    @GetMapping("/{id}")
    public EntradaResponseDTO buscarPorId(@PathVariable Long id, Authentication authentication) {
        return service.buscarPorId(id, authentication);
    }

    @PostMapping
    public EntradaResponseDTO criar(@RequestBody @Valid EntradaRequestDTO dto, Authentication authentication) {
        return service.criar(dto, authentication);
    }

    @PutMapping("/{id}")
    public EntradaResponseDTO atualizar(@PathVariable Long id, @RequestBody @Valid EntradaRequestDTO dto, Authentication authentication) {
        return service.atualizar(id, dto, authentication);
    }

    @DeleteMapping("/{id}")
    public void deletar(@PathVariable Long id, Authentication authentication) {
        service.deletar(id, authentication);
    }
}