package br.com.floris.controller;

import br.com.floris.dto.ReservaEmergenciaDTO;
import br.com.floris.model.User;
import br.com.floris.service.ReservaEmergenciaService;
import br.com.floris.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reserva-emergencia")
public class ReservaEmergenciaController {

    private final ReservaEmergenciaService reservaService;
    private final UserService userService;

    public ReservaEmergenciaController(ReservaEmergenciaService reservaService, UserService userService) {
        this.reservaService = reservaService;
        this.userService = userService;
    }

    @GetMapping
    public ReservaEmergenciaDTO getReserva(Authentication authentication) {
        String username = authentication.getName();
        User user = userService.findByUsername(username);
        return reservaService.buscarReservaPorUsuario(user.getId());
    }

    @PostMapping
    public ReservaEmergenciaDTO criarReserva(@RequestBody ReservaEmergenciaDTO dto, Authentication authentication) {
        String username = authentication.getName();
        User user = userService.findByUsername(username);
        return reservaService.criarReserva(user.getId(), dto);
    }

    @PutMapping
    public ReservaEmergenciaDTO atualizarReserva(@RequestBody ReservaEmergenciaDTO dto, Authentication authentication) {
        String username = authentication.getName();
        User user = userService.findByUsername(username);
        return reservaService.atualizarReserva(user.getId(), dto);
    }

    @DeleteMapping
    public void deletarReserva(Authentication authentication) {
        String username = authentication.getName();
        User user = userService.findByUsername(username);
        reservaService.deletarReserva(user.getId());
    }
}