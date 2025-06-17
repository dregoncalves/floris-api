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

    Page<Entrada> findByUsuarioId(Long usuarioId, Pageable pageable);

    List<Entrada> findAllByUsuarioId(Long usuarioId);

    Optional<Entrada> findByIdAndUsuarioId(Long id, Long usuarioId);

    List<Entrada> findAllByUsuarioIdAndDataRecebimentoBetween(Long usuarioId, LocalDate dataInicio, LocalDate dataFim);
}