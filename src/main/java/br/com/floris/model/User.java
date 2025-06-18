package br.com.floris.model;

import br.com.floris.enums.Role;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Table(name = "user")
@Entity(name = "user")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    private int age;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Retorna as permissões do usuário, com base na role
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public boolean isAccountNonExpired() {
        // Indica se a conta do usuário não está expirada
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        // Indica se a conta do usuário não está bloqueada
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        // Indica se as credenciais (senha) do usuário não estão expiradas
        return true;
    }

    @Override
    public boolean isEnabled() {
        // Indica se o usuário está ativo/habilitado
        return true;
    }
}