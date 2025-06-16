package br.com.floris.repository;

import br.com.floris.model.Gasto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GastoRepository extends JpaRepository<Gasto, Long> {
    List<Gasto> findByUsuarioId(Long usuarioId);

    Optional<Gasto> findByIdAndUsuarioId(Long id, Long usuarioId);
}
