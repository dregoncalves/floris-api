package br.com.floris.controller;

import br.com.floris.dto.DashboardDTO;
import br.com.floris.dto.HistoricoMensalDTO;
import br.com.floris.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    private final DashboardService service;

    public DashboardController(DashboardService service) {
        this.service = service;
    }

    @GetMapping
    public DashboardDTO getDashboard(Authentication authentication) {
        return service.gerarDashboard(authentication);
    }

    @GetMapping("/historico")
    public ResponseEntity<List<HistoricoMensalDTO>> getHistorico(Authentication authentication) {
        List<HistoricoMensalDTO> historico = service.getHistoricoMensal(authentication);
        return ResponseEntity.ok(historico);
    }
}