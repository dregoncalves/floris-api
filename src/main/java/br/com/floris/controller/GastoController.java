package br.com.floris.controller;

import br.com.floris.dto.GastoRequestDTO;
import br.com.floris.dto.GastoResponseDTO;
import br.com.floris.service.GastoService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/gastos")
public class GastoController {

    private final GastoService service;

    public GastoController(GastoService service) {
        this.service = service;
    }

    @GetMapping
    public List<GastoResponseDTO> listar(Authentication authentication) {
        return service.listarGastosDoUsuario(authentication);
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