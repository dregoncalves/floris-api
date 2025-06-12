package br.com.floris.repository;

import br.com.floris.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    Optional<User> findByUsernameOrEmail(String username, String email);

    boolean existsByUsernameOrEmail(String username, String email);
}

