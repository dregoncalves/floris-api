package br.com.floris.repository;

import br.com.floris.model.Gasto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface GastoRepository extends JpaRepository<Gasto, Long> {

    // Paginação
    Page<Gasto> findByUsuarioId(Long usuarioId, Pageable pageable);

    List<Gasto> findAllByUsuarioId(Long usuarioId);

    Optional<Gasto> findByIdAndUsuarioId(Long id, Long usuarioId);

    List<Gasto> findAllByUsuarioIdAndDataVencimentoBetween(Long usuarioId, LocalDate dataInicio, LocalDate dataFim);
}