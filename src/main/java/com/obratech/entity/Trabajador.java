package com.obratech.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DBRef;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Document(collection = "trabajador")
public class Trabajador {

    @Id
    private String id;

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    private String apellido;

    @Email(message = "El email no es válido")
    @NotBlank(message = "El email es obligatorio")
    private String email;

    @NotBlank(message = "El teléfono es obligatorio")
    private String telefono;

    @NotBlank(message = "El oficio es obligatorio")
    private String oficio;

    @NotNull(message = "La experiencia es obligatoria")
    @Min(value = 0, message = "La experiencia no puede ser negativa")
    @Max(value = 70, message = "La experiencia no puede ser mayor a 70 años")
    private Integer experiencia;

    @NotNull(message = "La disponibilidad no puede ser nula")
    private Boolean disponibilidad = true;

    private LocalDateTime creado = LocalDateTime.now();

    private String username;

    private String role = "ROLE_WORKER";

    private String cvUrl;

    // Aprobado por el admin — solo trabajadores con activo=true aparecen en el mercado
    private Boolean activo = false;

    public Trabajador() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getOficio() { return oficio; }
    public void setOficio(String oficio) { this.oficio = oficio; }

    public Integer getExperiencia() { return experiencia; }
    public void setExperiencia(Integer experiencia) { this.experiencia = experiencia; }

    public Boolean getDisponibilidad() { return disponibilidad; }
    public void setDisponibilidad(Boolean disponibilidad) { this.disponibilidad = disponibilidad; }

    public LocalDateTime getCreado() { return creado; }
    public void setCreado(LocalDateTime creado) { this.creado = creado; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getCvUrl() { return cvUrl; }
    public void setCvUrl(String cvUrl) { this.cvUrl = cvUrl; }

    public Boolean getActivo() { return activo != null && activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }

    @Override
    public String toString() {
        String usuarioId = null;
        return "Trabajador{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", apellido='" + apellido + '\'' +
                ", email='" + email + '\'' +
                ", telefono='" + telefono + '\'' +
                ", oficio='" + oficio + '\'' +
                ", experiencia=" + experiencia +
                ", disponibilidad=" + disponibilidad +
                ", creado=" + creado +
                ", usuarioId=" + usuarioId +
                '}';
    }

    public void setUsuarioId(String id2) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setUsuarioId'");
    }
}
