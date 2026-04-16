package com.obratech.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DBRef;

@Document(collection = "proyectos")
public class Proyecto {

    @Id
    private String id;

    private String titulo;

    private String descripcion;

    private String tipoProyecto;
    private String ubicacion;
    private Double presupuesto;
    private Integer plazoEstimado;
    private LocalDate fechaInicio;
    private LocalDate fechaEntrega;
    private LocalDate fechaLimitePostulacion;
    private LocalDateTime fechaLimite;

    private String estadoAsignacion;
    private String estadoEjecucion;

    private Double areaTotal;
    private Integer numeroPisos;

    private String tipoContratacion;

    private String documentoLegalUrl;
    private String documentoLegalNombre;

    @DBRef
    private Usuario cliente;

    private LocalDateTime fechaCreacion = LocalDateTime.now();

    private String estadoValidacion = "PENDIENTE";

    @DBRef
    private Set<Persona> personas = new HashSet<>();

    @DBRef
    private Set<Trabajador> trabajadoresAsignados = new HashSet<>();

    @DBRef
    private com.obratech.entity.Contratista contratistaAsignado;

    public Proyecto() {}

    public Proyecto(String titulo, String descripcion) {
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.estadoAsignacion = "Sin asignar";
        this.estadoEjecucion = "Pendiente";
        this.fechaCreacion = LocalDateTime.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getTipoProyecto() {
        return tipoProyecto;
    }

    public void setTipoProyecto(String tipoProyecto) {
        this.tipoProyecto = tipoProyecto;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

    public Double getPresupuesto() {
        return presupuesto;
    }

    public void setPresupuesto(Double presupuesto) {
        this.presupuesto = presupuesto;
    }

    public Integer getPlazoEstimado() {
        return plazoEstimado;
    }

    public void setPlazoEstimado(Integer plazoEstimado) {
        this.plazoEstimado = plazoEstimado;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDate getFechaEntrega() {
        return fechaEntrega;
    }

    public void setFechaEntrega(LocalDate fechaEntrega) {
        this.fechaEntrega = fechaEntrega;
    }

    public LocalDate getFechaLimitePostulacion() {
        return fechaLimitePostulacion;
    }

    public void setFechaLimitePostulacion(LocalDate fechaLimitePostulacion) {
        this.fechaLimitePostulacion = fechaLimitePostulacion;
    }

    public LocalDateTime getFechaLimite() {
        return fechaLimite;
    }

    public void setFechaLimite(LocalDateTime fechaLimite) {
        this.fechaLimite = fechaLimite;
    }

    public String getEstadoAsignacion() {
        return estadoAsignacion;
    }

    public void setEstadoAsignacion(String estadoAsignacion) {
        this.estadoAsignacion = estadoAsignacion;
    }

    public String getEstadoEjecucion() {
        return estadoEjecucion;
    }

    public void setEstadoEjecucion(String estadoEjecucion) {
        this.estadoEjecucion = estadoEjecucion;
    }

    public Double getAreaTotal() {
        return areaTotal;
    }

    public void setAreaTotal(Double areaTotal) {
        this.areaTotal = areaTotal;
    }

    public Integer getNumeroPisos() {
        return numeroPisos;
    }

    public void setNumeroPisos(Integer numeroPisos) {
        this.numeroPisos = numeroPisos;
    }

    public String getTipoContratacion() {
        return tipoContratacion;
    }

    public void setTipoContratacion(String tipoContratacion) {
        this.tipoContratacion = tipoContratacion;
    }

    public Usuario getCliente() {
        return cliente;
    }

    public void setCliente(Usuario cliente) {
        this.cliente = cliente;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public Set<Persona> getPersonas() {
        return personas;
    }

    public void setPersonas(Set<Persona> personas) {
        this.personas = personas;
    }

    public com.obratech.entity.Contratista getContratistaAsignado() {
        return contratistaAsignado;
    }

    public void setContratistaAsignado(com.obratech.entity.Contratista contratistaAsignado) {
        this.contratistaAsignado = contratistaAsignado;
    }

    public String getEstadoValidacion() {
        return estadoValidacion;
    }

    public void setEstadoValidacion(String estadoValidacion) {
        this.estadoValidacion = estadoValidacion;
    }

    public Set<Trabajador> getTrabajadoresAsignados() {
        return trabajadoresAsignados;
    }

    public void setTrabajadoresAsignados(Set<Trabajador> trabajadoresAsignados) {
        this.trabajadoresAsignados = trabajadoresAsignados;
    }

    public String getDocumentoLegalUrl() { return documentoLegalUrl; }
    public void setDocumentoLegalUrl(String documentoLegalUrl) { this.documentoLegalUrl = documentoLegalUrl; }

    public String getDocumentoLegalNombre() { return documentoLegalNombre; }
    public void setDocumentoLegalNombre(String documentoLegalNombre) { this.documentoLegalNombre = documentoLegalNombre; }
}
