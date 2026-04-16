
package com.obratech.service;

import com.obratech.entity.Calificacion;
import com.obratech.repository.CalificacionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CalificacionService {

    private final CalificacionRepository repo;

    public CalificacionService(CalificacionRepository repo) {
        this.repo = repo;
    }

    public List<Calificacion> findAll() { return repo.findAll(); }

    public Optional<Calificacion> findById(String id) { return repo.findById(id); }

    public Calificacion save(Calificacion c) { return repo.save(c); }

    public void deleteById(String id) { repo.deleteById(id); }
}
