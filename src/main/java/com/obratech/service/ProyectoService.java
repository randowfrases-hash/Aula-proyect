package com.obratech.service;

import com.obratech.entity.HistorialProyecto;
import com.obratech.entity.Proyecto;
import com.obratech.entity.Usuario;
import com.obratech.repository.HistorialProyectoRepository;
import com.obratech.repository.ProyectoRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ProyectoService {

    private final ProyectoRepository repo;
    private final HistorialProyectoRepository historialRepo;

    public ProyectoService(ProyectoRepository repo, HistorialProyectoRepository historialRepo) {
        this.repo = repo;
        this.historialRepo = historialRepo;
    }


    public List<Proyecto> findAll() {
        return repo.findAll();
    }

    // Buscar proyecto por ID
    public Optional<Proyecto> findById(String id) {
        return repo.findById(id);
    }

    public Proyecto publicarProyecto(Proyecto proyecto, Usuario cliente) {
        proyecto.setCliente(cliente);
        proyecto.setEstadoAsignacion("Sin asignar");
        proyecto.setEstadoEjecucion("Pendiente");
        proyecto.setFechaCreacion(LocalDateTime.now());

        Proyecto guardado = repo.save(proyecto);

        // Registrar historial inicial
        HistorialProyecto historial = new HistorialProyecto(
            null,
            "Pendiente",
            guardado
        );
        historialRepo.save(historial);

        return guardado;
    }

    public void cambiarEstado(Proyecto proyecto, String nuevoEstado) {
        String anterior = proyecto.getEstadoEjecucion();
        proyecto.setEstadoEjecucion(nuevoEstado);
        repo.save(proyecto);

        historialRepo.save(
            new HistorialProyecto(anterior, nuevoEstado, proyecto)
        );
    }

    // Actualizar proyecto existente
    public Proyecto update(String id, Proyecto proyecto) {
        Optional<Proyecto> existente = repo.findById(id);
        if (existente.isPresent()) {
            proyecto.setId(id);
            return repo.save(proyecto);
        }
        return null;
    }

    public Proyecto save(Proyecto proyecto) {
        return repo.save(proyecto);
    }
    
    public void deleteById(String id) {
        repo.deleteById(id);
    }
}
