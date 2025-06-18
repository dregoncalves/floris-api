package br.com.floris.repository;

import br.com.floris.model.Gasto;
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
public interface GastoRepository extends JpaRepository<Gasto, Long> {

    // Lista os gastos do usuário com paginação
    Page<Gasto> findByUsuarioId(Long usuarioId, Pageable pageable);

    // Busca todos os gastos de um usuário
    List<Gasto> findAllByUsuarioId(Long usuarioId);

    // Busca um gasto específico pelo ID e ID do usuário
    Optional<Gasto> findByIdAndUsuarioId(Long id, Long usuarioId);

    // Busca gastos de um usuário em um período
    List<Gasto> findAllByUsuarioIdAndDataVencimentoBetween(Long usuarioId, LocalDate dataInicio, LocalDate dataFim);
}