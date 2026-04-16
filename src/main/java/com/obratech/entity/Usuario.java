package com.obratech.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "usuarios")
public class Usuario implements Serializable {

    @Id
    private String id;

    // Correo del usuario 
    private String username;

    // Contraseña encriptada con BCrypt
    private String password;

    // Rol del usuario: ROLE_ADMIN, ROLE_CLIENT, ROLE_CONTRACTOR, ROLE_WORKER
    private String role = "ROLE_USER";

    // Estado: activo/bloqueado 
    private boolean activo = true;

    // Intentos fallidos de login 
    private int intentosFallidos = 0;

    // Fecha en que se bloqueó la cuenta temporalmente
    private LocalDateTime bloqueadoHasta;

    // Fecha de creación
    private LocalDateTime creado;

    // Último acceso
    private LocalDateTime ultimoAcceso;

    public Usuario() {
        if (this.creado == null) {
            this.creado = LocalDateTime.now();
        }
    }

    // ─── Getters y Setters ────────────────────────────────────────────────────

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public int getIntentosFallidos() { return intentosFallidos; }
    public void setIntentosFallidos(int intentosFallidos) { this.intentosFallidos = intentosFallidos; }

    public LocalDateTime getBloqueadoHasta() { return bloqueadoHasta; }
    public void setBloqueadoHasta(LocalDateTime bloqueadoHasta) { this.bloqueadoHasta = bloqueadoHasta; }

    public LocalDateTime getCreado() { return creado; }
    public void setCreado(LocalDateTime creado) { this.creado = creado; }

    public LocalDateTime getUltimoAcceso() { return ultimoAcceso; }
    public void setUltimoAcceso(LocalDateTime ultimoAcceso) { this.ultimoAcceso = ultimoAcceso; }

    @Override
    public String toString() {
        return "Usuario{id=" + id + ", username='" + username + "', role='" + role + "', activo=" + activo + "}";
    }
}
