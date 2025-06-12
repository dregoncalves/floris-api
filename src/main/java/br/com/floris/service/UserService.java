package br.com.floris.service;

import br.com.floris.model.User;
import br.com.floris.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository repository;

    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    public List<User> findAll() {
        return repository.findAll();
    }

    public User findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado com id: " + id));
    }

    public User findByUsername(String username) {
        return repository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
    }

    public User create(User user) {
        if (repository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("Username já cadastrado: " + user.getUsername());
        }
        return repository.save(user);
    }

    public User update(Long id, User updatedUser) {
        User user = findById(id);
        user.setName(updatedUser.getName());
        user.setUsername(updatedUser.getUsername());
        user.setAge(updatedUser.getAge());
        user.setPassword(updatedUser.getPassword());
        user.setRole(updatedUser.getRole());
        return repository.save(user);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }
}
