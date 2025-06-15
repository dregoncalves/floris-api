package br.com.floris.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import br.com.floris.model.ReservaEmergencia;
import br.com.floris.model.User;

public interface ReservaEmergenciaRepository extends JpaRepository<ReservaEmergencia, Long> {
    Optional<ReservaEmergencia> findByUser(User user);
}
