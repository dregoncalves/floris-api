package br.com.floris.controller;

import br.com.floris.dto.gastos.GastoRequestDTO;
import br.com.floris.dto.gastos.GastoResponseDTO;
import br.com.floris.service.GastoService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;

@RestController
@RequestMapping("/gastos")
public class GastoController {

    private final GastoService service;

    public GastoController(GastoService service) {
        this.service = service;
    }

    @GetMapping
    public Page<GastoResponseDTO> listar(Authentication authentication, @PageableDefault(sort = "dataVencimento", direction = Sort.Direction.DESC) Pageable pageable) {
        return service.listarGastosDoUsuario(authentication, pageable);
    }

    @GetMapping("/{id}")
    public GastoResponseDTO buscarPorId(@PathVariable Long id, Authentication authentication) {
        return service.buscarPorId(id, authentication);
    }

    @PostMapping
    public GastoResponseDTO criar(@RequestBody @Valid GastoRequestDTO dto, Authentication authentication) {
        return service.criar(dto, authentication);
    }

    @PutMapping("/{id}")
    public GastoResponseDTO atualizar(@PathVariable Long id, @RequestBody @Valid GastoRequestDTO dto, Authentication authentication) {
        return service.atualizar(id, dto, authentication);
    }

    @DeleteMapping("/{id}")
    public void deletar(@PathVariable Long id, Authentication authentication) {
        service.deletar(id, authentication);
    }
}