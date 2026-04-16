package com.obratech.controllers;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.obratech.entity.Persona;
import com.obratech.entity.Proyecto;
import com.obratech.service.CalificacionService;
import com.obratech.service.PersonaService;
import com.obratech.service.ProyectoService;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final PersonaService personaService;
    private final ProyectoService proyectoService;
    private final CalificacionService calificacionService;

    public ApiController(PersonaService personaService, ProyectoService proyectoService, CalificacionService calificacionService) {
        this.personaService = personaService;
        this.proyectoService = proyectoService;
        this.calificacionService = calificacionService;
    }

    @GetMapping("/personas")
    public List<Persona> listPersonas() {
        return personaService.findAll();
    }

    @GetMapping("/personas/{id}")
    public ResponseEntity<Persona> getPersona(@PathVariable String id) {
        Optional<Persona> p = personaService.findById(id);
        return p.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/personas")
    public ResponseEntity<Persona> createPersona(@RequestBody Persona p) {
        Persona saved = personaService.save(p);
        return ResponseEntity.created(URI.create("/api/personas/" + saved.getId())).body(saved);
    }

    @PutMapping("/personas/{id}")
    public ResponseEntity<Persona> updatePersona(@PathVariable String id, @RequestBody Persona p) {
        Persona updated = personaService.update(id, p);
        if (updated == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/personas/{id}")
    public ResponseEntity<Void> deletePersona(@PathVariable String id) {
        Optional<Persona> existing = personaService.findById(id);
        if (existing.isEmpty()) return ResponseEntity.notFound().build();
        personaService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/proyectos")
    public List<Proyecto> listProyectos() {
        return proyectoService.findAll();
    }

    @GetMapping("/proyectos/{id}")
    public ResponseEntity<Proyecto> getProyecto(@PathVariable String id) {
        Optional<Proyecto> p = proyectoService.findById(id);
        return p.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/proyectos")
    public ResponseEntity<Proyecto> createProyecto(@RequestBody Proyecto p) {
        Proyecto saved = proyectoService.save(p);
        return ResponseEntity.created(URI.create("/api/proyectos/" + saved.getId())).body(saved);
    }

    @PutMapping("/proyectos/{id}")
    public ResponseEntity<Proyecto> updateProyecto(@PathVariable String id, @RequestBody Proyecto p) {
        Proyecto updated = proyectoService.update(id, p);
        if (updated == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/proyectos/{id}")
    public ResponseEntity<Void> deleteProyecto(@PathVariable String id) {
        Optional<Proyecto> existing = proyectoService.findById(id);
        if (existing.isEmpty()) return ResponseEntity.notFound().build();
        proyectoService.deleteById(id);
        return ResponseEntity.noContent().build();
    }


    // Calificacion endpoints
    @GetMapping("/calificaciones")
    public List<com.obratech.entity.Calificacion> listCalificaciones() { return calificacionService.findAll(); }

    @PostMapping("/calificaciones")
    public ResponseEntity<com.obratech.entity.Calificacion> createCalificacion(@RequestBody com.obratech.entity.Calificacion c) {
        com.obratech.entity.Calificacion saved = calificacionService.save(c);
        return ResponseEntity.created(URI.create("/api/calificaciones/" + saved.getId())).body(saved);
    }

    // Optional helper: link a persona to a proyecto
    @PostMapping("/proyectos/{proyectoId}/personas/{personaId}")
    public ResponseEntity<Proyecto> addPersonaToProyecto(@PathVariable String proyectoId, @PathVariable String personaId) {
        Optional<Proyecto> projOpt = proyectoService.findById(proyectoId);
        Optional<Persona> personOpt = personaService.findById(personaId);
        if (projOpt.isEmpty() || personOpt.isEmpty()) return ResponseEntity.notFound().build();
        Proyecto proyecto = projOpt.get();
        proyecto.getPersonas().add(personOpt.get());
        Proyecto saved = proyectoService.save(proyecto);
        return ResponseEntity.ok(saved);
    }
}
