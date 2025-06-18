package br.com.floris.repository;

import br.com.floris.model.Entrada;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface EntradaRepository extends JpaRepository<Entrada, Long> {

    // Busca entradas por ID do usuário, com paginação
    Page<Entrada> findByUsuarioId(Long usuarioId, Pageable pageable);

    List<Entrada> findAllByUsuarioId(Long usuarioId);

    Optional<Entrada> findByIdAndUsuarioId(Long id, Long usuarioId);

    // Busca entradas de um usuário em um período
    List<Entrada> findAllByUsuarioIdAndDataRecebimentoBetween(Long usuarioId, LocalDate dataInicio, LocalDate dataFim);
}