package com.obratech.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.obratech.entity.Usuario;
import com.obratech.repository.UsuarioRepository;

@Service
public class UsuarioService {

    private final UsuarioRepository repo;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository repo, PasswordEncoder passwordEncoder) {
        this.repo = repo;
        this.passwordEncoder = passwordEncoder;
    }

    public List<Usuario> findAll() { return repo.findAll(); }

    public Optional<Usuario> findById(String id) { return repo.findById(id); }

    public Optional<Usuario> findByUsername(String username) { return repo.findByUsername(username); }

    /**
     * Registra un nuevo usuario validando campos, rol y encriptando la contraseña.
     */
    public Usuario register(Usuario u) {
        if (u.getUsername() == null || u.getUsername().isBlank())
            throw new IllegalArgumentException("El correo electrónico no puede estar vacío.");
        if (!u.getUsername().matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$"))
            throw new IllegalArgumentException("El correo electrónico no tiene un formato válido.");
        if (u.getPassword() == null || u.getPassword().isBlank())
            throw new IllegalArgumentException("La contraseña no puede estar vacía.");
        if (u.getPassword().length() < 8)
            throw new IllegalArgumentException("La contraseña debe tener al menos 8 caracteres.");
        if (repo.findByUsername(u.getUsername()).isPresent())
            throw new IllegalArgumentException("El correo electrónico ya está registrado.");

        // Normalizar rol
        String roleSolicitado = u.getRole();
        u.setRole(mapearRol(roleSolicitado));

        // Encriptar contraseña
        u.setPassword(passwordEncoder.encode(u.getPassword()));
        u.setCreado(LocalDateTime.now());
        u.setActivo(true);

        return repo.save(u);
    }

    /**
     * Activa o desactiva una cuenta.
     */
    public void toggleActivo(String id) {
        repo.findById(id).ifPresent(u -> {
            u.setActivo(!u.isActivo());
            repo.save(u);
        });
    }

    public void deleteById(String id) { repo.deleteById(id); }

    // ── Mapea el valor del select del registro ──────
    private String mapearRol(String roleSolicitado) {
        if (roleSolicitado == null) return "ROLE_USER";
        return switch (roleSolicitado.toLowerCase()) {
            case "contratista" -> "ROLE_CONTRACTOR";
            case "cliente"     -> "ROLE_CLIENT";
            case "trabajador"  -> "ROLE_WORKER";
            case "admin"       -> "ROLE_ADMIN";
            default            -> "ROLE_USER";
        };
    }
}
