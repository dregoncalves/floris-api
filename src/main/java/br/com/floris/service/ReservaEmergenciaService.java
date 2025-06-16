package br.com.floris.service;

import br.com.floris.dto.ReservaEmergenciaDTO;
import br.com.floris.model.ReservaEmergencia;
import br.com.floris.model.User;
import br.com.floris.repository.ReservaEmergenciaRepository;
import br.com.floris.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class ReservaEmergenciaService {

    @Autowired
    private ReservaEmergenciaRepository reservaRepository;

    @Autowired
    private UserRepository userRepository;

    public ReservaEmergenciaDTO criarReserva(Long userId, ReservaEmergenciaDTO dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));

        ReservaEmergencia reserva = new ReservaEmergencia();
        reserva.setUsuario(user); // <-- aqui
        reserva.setValorObjetivo(dto.getValorObjetivo());
        reserva.setValorAtual(dto.getValorAtual());
        reserva.setAtiva(true);
        reserva.setDataCriacao(LocalDate.now());
        reserva.setUltimaAtualizacao(LocalDate.now());

        reservaRepository.save(reserva);
        return toDTO(reserva);
    }

    public ReservaEmergenciaDTO buscarReservaPorUsuario(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));

        ReservaEmergencia reserva = reservaRepository.findByUsuario(user)
                .orElseThrow(() -> new EntityNotFoundException("Reserva não encontrada"));

        return toDTO(reserva);
    }

    public ReservaEmergenciaDTO atualizarReserva(Long userId, ReservaEmergenciaDTO dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));

        ReservaEmergencia reserva = reservaRepository.findByUsuario(user)
                .orElseThrow(() -> new EntityNotFoundException("Reserva não encontrada"));

        reserva.setValorObjetivo(dto.getValorObjetivo());
        reserva.setValorAtual(dto.getValorAtual());
        reserva.setAtiva(dto.getAtiva());
        reserva.setUltimaAtualizacao(LocalDate.now());

        reservaRepository.save(reserva);
        return toDTO(reserva);
    }

    public void deletarReserva(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));

        ReservaEmergencia reserva = reservaRepository.findByUsuario(user)
                .orElseThrow(() -> new EntityNotFoundException("Reserva não encontrada"));

        reservaRepository.delete(reserva);
    }

    private ReservaEmergenciaDTO toDTO(ReservaEmergencia reserva) {
        ReservaEmergenciaDTO dto = new ReservaEmergenciaDTO();
        dto.setId(reserva.getId());
        dto.setUsuarioId(reserva.getUsuario().getId()); // <-- aqui
        dto.setValorObjetivo(reserva.getValorObjetivo());
        dto.setValorAtual(reserva.getValorAtual());
        dto.setPercentualConcluido(reserva.getPercentualConcluido());
        dto.setAtiva(reserva.getAtiva());
        dto.setDataCriacao(reserva.getDataCriacao());
        dto.setUltimaAtualizacao(reserva.getUltimaAtualizacao());
        return dto;
    }
}
