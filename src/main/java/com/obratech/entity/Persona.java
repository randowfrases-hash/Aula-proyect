package com.obratech.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DBRef;
import java.util.HashSet;
import java.util.Set;

@Document(collection = "personas")
public class Persona {

    @Id
    private String id;

    private String nombre;

    private String email;

    @DBRef
    private Set<Proyecto> proyectos = new HashSet<>();

    public Persona() {}

    public Persona(String nombre, String email) {
        this.nombre = nombre;
        this.email = email;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Set<Proyecto> getProyectos() {
        return proyectos;
    }

    public void setProyectos(Set<Proyecto> proyectos) {
        this.proyectos = proyectos;
    }
}
