package com.obratech.service;

import com.obratech.entity.Persona;
import com.obratech.repository.PersonaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PersonaService {

    private final PersonaRepository repo;

    public PersonaService(PersonaRepository repo) {
        this.repo = repo;
    }

    public List<Persona> findAll() {
        return repo.findAll();
    }

    public Optional<Persona> findById(String id) {
        return repo.findById(id);
    }

    public Persona update(String id, Persona p) {
        Optional<Persona> existing = repo.findById(id);
        if (existing.isPresent()) {
            p.setId(id);
            return repo.save(p);
        }
        return null;
    }

    public Persona save(Persona p) {
        return repo.save(p);
    }

    public void deleteById(String id) {
        repo.deleteById(id);
    }
}
