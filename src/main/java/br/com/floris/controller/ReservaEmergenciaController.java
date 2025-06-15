package br.com.floris.controller;

import br.com.floris.dto.ReservaEmergenciaDTO;
import br.com.floris.service.ReservaEmergenciaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reserva-emergencia")
public class ReservaEmergenciaController {

    @Autowired
    private ReservaEmergenciaService reservaService;

    @PostMapping("/{userId}")
    public ReservaEmergenciaDTO criar(@PathVariable Long userId, @RequestBody ReservaEmergenciaDTO dto) {
        return reservaService.criarReserva(userId, dto);
    }

    @GetMapping("/{userId}")
    public ReservaEmergenciaDTO buscar(@PathVariable Long userId) {
        return reservaService.buscarReservaPorUsuario(userId);
    }

    @PutMapping("/{userId}")
    public ReservaEmergenciaDTO atualizar(@PathVariable Long userId, @RequestBody ReservaEmergenciaDTO dto) {
        return reservaService.atualizarReserva(userId, dto);
    }

    @DeleteMapping("/{userId}")
    public void deletar(@PathVariable Long userId) {
        reservaService.deletarReserva(userId);
    }
}
