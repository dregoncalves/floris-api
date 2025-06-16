package br.com.floris.repository;

import br.com.floris.model.ReservaEmergencia;
import br.com.floris.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReservaEmergenciaRepository extends JpaRepository<ReservaEmergencia, Long> {
    Optional<ReservaEmergencia> findByUsuario(User usuario);
}
