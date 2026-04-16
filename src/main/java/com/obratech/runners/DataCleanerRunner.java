package com.obratech.runners;

import java.util.List;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.obratech.entity.Contratista;
import com.obratech.entity.Trabajador;
import com.obratech.repository.ContratistaRepository;
import com.obratech.repository.TrabajadorRepository;

@Component
public class DataCleanerRunner implements ApplicationRunner {

    private final ContratistaRepository contratistaRepository;
    private final TrabajadorRepository trabajadorRepository;

    public DataCleanerRunner(ContratistaRepository contratistaRepository,
            TrabajadorRepository trabajadorRepository) {
        this.contratistaRepository = contratistaRepository;
        this.trabajadorRepository = trabajadorRepository;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        System.out.println("DataCleanerRunner: iniciando limpieza de campos vacíos...");

        int fixedContratistas = 0;
        List<Contratista> contratistas = contratistaRepository.findAll();
        for (Contratista c : contratistas) {
            boolean changed = false;
            if (c.getNombre() != null && c.getNombre().trim().isEmpty()) { c.setNombre(null); changed = true; }
            if (c.getApellido() != null && c.getApellido().trim().isEmpty()) { c.setApellido(null); changed = true; }
            if (c.getEmail() != null && c.getEmail().trim().isEmpty()) { c.setEmail(null); changed = true; }
            if (c.getTelefono() != null && c.getTelefono().trim().isEmpty()) { c.setTelefono(null); changed = true; }
            if (c.getEspecialidad() != null && c.getEspecialidad().trim().isEmpty()) { c.setEspecialidad(null); changed = true; }
            if (changed) {
                contratistaRepository.save(c);
                fixedContratistas++;
            }
        }

        int fixedTrabajadores = 0;
        List<Trabajador> trabajadores = trabajadorRepository.findAll();
        for (Trabajador t : trabajadores) {
            boolean changed = false;
            if (t.getNombre() != null && t.getNombre().trim().isEmpty()) { t.setNombre(null); changed = true; }
            if (t.getApellido() != null && t.getApellido().trim().isEmpty()) { t.setApellido(null); changed = true; }
            if (t.getEmail() != null && t.getEmail().trim().isEmpty()) { t.setEmail(null); changed = true; }
            if (t.getTelefono() != null && t.getTelefono().trim().isEmpty()) { t.setTelefono(null); changed = true; }
            if (t.getOficio() != null && t.getOficio().trim().isEmpty()) { t.setOficio(null); changed = true; }
            if (changed) {
                trabajadorRepository.save(t);
                fixedTrabajadores++;
            }
        }

        System.out.println("DataCleanerRunner: limpieza completada. Contratistas actualizados: " + fixedContratistas
                + ", Trabajadores actualizados: " + fixedTrabajadores);
    }
}
