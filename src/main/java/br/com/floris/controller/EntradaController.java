package br.com.floris.controller;

import br.com.floris.dto.entradas.EntradaRequestDTO;
import br.com.floris.dto.entradas.EntradaResponseDTO;
import br.com.floris.service.EntradaService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;

@RestController
@RequestMapping("/entradas")
public class EntradaController {

    private final EntradaService service;

    public EntradaController(EntradaService service) {
        this.service = service;
    }

    @GetMapping
    public Page<EntradaResponseDTO> listar(Authentication authentication, @PageableDefault(sort = "dataRecebimento", direction = Sort.Direction.DESC) Pageable pageable) {
        return service.listarEntradasDoUsuario(authentication, pageable);
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