package br.com.floris.repository;

import br.com.floris.model.Entrada;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EntradaRepository extends JpaRepository<Entrada, Long> {
    List<Entrada> findByUsuarioId(Long usuarioId);
}
