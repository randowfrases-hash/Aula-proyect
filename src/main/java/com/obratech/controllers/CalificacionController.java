package com.obratech.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

import com.obratech.entity.Calificacion;
import com.obratech.entity.Persona;
import com.obratech.entity.Proyecto;
import com.obratech.entity.Usuario;

import jakarta.servlet.http.HttpSession;
import java.util.List;

@Controller
@RequestMapping("/calificaciones")
public class CalificacionController {
    // Vista para calificar contratista desde proyecto (como postulaciones)
    @GetMapping("/calificar/{proyectoId}/{contratistaId}")
    public String calificarDesdeProyecto(@PathVariable String proyectoId, @PathVariable String contratistaId, jakarta.servlet.http.HttpSession session, org.springframework.ui.Model model) {
        com.obratech.entity.Usuario usuario = (com.obratech.entity.Usuario) session.getAttribute("usuario");
        if (usuario == null) return "redirect:/login";

        com.obratech.entity.Proyecto proyecto = proyectoRepository.findById(proyectoId).orElse(null);
        com.obratech.entity.Contratista contratista = contratistaRepository.findById(contratistaId).orElse(null);
        if (proyecto == null || contratista == null) return "redirect:/desboard";

        // Buscar o crear persona
        com.obratech.entity.Persona persona = null;
        if (contratista.getEmail() != null) {
            persona = personaRepository.findAll().stream()
                .filter(p -> contratista.getEmail().equalsIgnoreCase(p.getEmail()))
                .findFirst().orElse(null);
        }
        if (persona == null) {
            persona = new com.obratech.entity.Persona(contratista.getNombre() + " " + contratista.getApellido(), contratista.getEmail());
            personaRepository.save(persona);
        }

        // Revisar si ya existe calificación
        if (calificacionRepository.existsByProyectoIdAndContratistaId(proyectoId, persona.getId())) {
            com.obratech.entity.Calificacion existing = calificacionRepository.findByContratistaId(persona.getId()).stream()
                .filter(c -> c.getProyecto() != null && c.getProyecto().getId().equals(proyectoId))
                .findFirst().orElse(null);
            if (existing != null) return "redirect:/calificaciones/" + existing.getId() + "/editar";
        }

        model.addAttribute("proyecto", proyecto);
        model.addAttribute("contratista", persona);
        model.addAttribute("calificacion", new com.obratech.entity.Calificacion());
        return "clientes/calificar";
    }

    // Manejar URL con solo proyectoId para evitar 404 si falta el contratistaId en el enlace
    @GetMapping("/calificar/{proyectoId}")
    public String calificarDesdeProyectoSolo(@PathVariable String proyectoId, jakarta.servlet.http.HttpSession session) {
        com.obratech.entity.Proyecto proyecto = proyectoRepository.findById(proyectoId).orElse(null);
        if (proyecto == null) return "redirect:/desboard";

        if (proyecto.getContratistaAsignado() != null && proyecto.getContratistaAsignado().getId() != null) {
            return "redirect:/calificaciones/calificar/" + proyectoId + "/" + proyecto.getContratistaAsignado().getId();
        }

        session.setAttribute("error", "No hay contratista asignado para calificar en este proyecto.");
        return "redirect:/proyectos/" + proyectoId;
    }

    // Endpoint para intentar calificar usando solo el contratista: busca un proyecto asignado y redirige
    @GetMapping("/calificar-por-contratista/{contratistaId}")
    public String calificarPorContratista(@PathVariable String contratistaId, jakarta.servlet.http.HttpSession session) {
        com.obratech.entity.Contratista cont = contratistaRepository.findById(contratistaId).orElse(null);
        if (cont == null) return "redirect:/contratistas";

        // Buscar un proyecto donde este contratista esté asignado
        com.obratech.entity.Proyecto proyecto = proyectoRepository.findAll().stream()
                .filter(p -> p.getContratistaAsignado() != null && p.getContratistaAsignado().getId().equals(contratistaId))
                .findFirst().orElse(null);

        if (proyecto != null) {
            return "redirect:/calificaciones/calificar/" + proyecto.getId() + "/" + contratistaId;
        }

        // Si no hay proyecto asignado, redirigir al perfil del contratista con mensaje
        session.setAttribute("error", "No se encontró un proyecto asignado para este contratista.");
        return "redirect:/contratistas/" + contratistaId;
    }

    // API: obtener proyecto asignado para un contratista (JSON)
    @GetMapping("/api/proyecto-por-contratista/{contratistaId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> apiProyectoPorContratista(@PathVariable String contratistaId) {
        Map<String, Object> resp = new HashMap<>();
        com.obratech.entity.Contratista cont = contratistaRepository.findById(contratistaId).orElse(null);
        if (cont == null) {
            resp.put("ok", false);
            resp.put("message", "Contratista no encontrado");
            return ResponseEntity.status(404).body(resp);
        }

        com.obratech.entity.Proyecto proyecto = proyectoRepository.findAll().stream()
                .filter(p -> p.getContratistaAsignado() != null && p.getContratistaAsignado().getId().equals(contratistaId))
                .findFirst().orElse(null);

        if (proyecto == null) {
            resp.put("ok", false);
            resp.put("message", "No hay proyecto asignado para este contratista");
            return ResponseEntity.status(404).body(resp);
        }

        resp.put("ok", true);
        resp.put("proyectoId", proyecto.getId());
        resp.put("contratistaId", contratistaId);
        return ResponseEntity.ok(resp);
    }

    @Autowired
    private com.obratech.repository.CalificacionRepository calificacionRepository;

    @Autowired
    private com.obratech.repository.PersonaRepository personaRepository;

    @Autowired
    private com.obratech.repository.ProyectoRepository proyectoRepository;

    @Autowired
    private com.obratech.repository.ContratistaRepository contratistaRepository;

    // Lista de contratistas para calificar
    @GetMapping("/contratistas")
    public String contratistasParaCalificar(HttpSession session, Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) return "redirect:/desboard";

        List<com.obratech.entity.Contratista> contratistas = contratistaRepository.findAll();
        model.addAttribute("contratistas", contratistas);
        model.addAttribute("usuario", usuario);
        model.addAttribute("totalContratistas", contratistas.size());
        // Si el proyectoId está en la sesión, pásalo al modelo
        Object proyectoId = session.getAttribute("proyectoId");
        if (proyectoId != null) {
            model.addAttribute("proyectoId", proyectoId);
        }
        return "clientes/contratistas-para-calificar";
    }

    // Redirigir al formulario de calificación
    @GetMapping("/crearPorContratista/{proyectoId}/{contratistaId}")
    public String crearPorContratista(@PathVariable String proyectoId, @PathVariable String contratistaId, HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) return "redirect:/desboard";

        Proyecto proyecto = proyectoRepository.findById(proyectoId).orElse(null);
        com.obratech.entity.Contratista cont = contratistaRepository.findById(contratistaId).orElse(null);

        if (proyecto == null || cont == null) return "redirect:/desboard";

        // Buscar o crear persona
        Persona persona = null;
        if (cont.getEmail() != null) {
            persona = personaRepository.findAll().stream()
                    .filter(p -> cont.getEmail().equalsIgnoreCase(p.getEmail()))
                    .findFirst().orElse(null);
        }

        if (persona == null) {
            persona = new Persona(cont.getNombre() + " " + cont.getApellido(), cont.getEmail());
            personaRepository.save(persona);
        }

        return "redirect:/calificaciones/crear/" + proyectoId + "/" + persona.getId();
    }

    // Formulario de calificación
    @GetMapping("/crear/{proyectoId}/{personaId}")
    public String formCalificar(@PathVariable String proyectoId, @PathVariable String personaId, HttpSession session, Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) return "redirect:/desboard";

        Proyecto proyecto = proyectoRepository.findById(proyectoId).orElse(null);
        Persona persona = personaRepository.findById(personaId).orElse(null);

        if (proyecto == null || persona == null) return "redirect:/desboard";

        // Revisar si ya existe calificación
        if (calificacionRepository.existsByProyectoIdAndContratistaId(proyectoId, personaId)) {
            Calificacion existing = calificacionRepository.findByContratistaId(personaId).stream()
                    .filter(c -> c.getProyecto() != null && c.getProyecto().getId().equals(proyectoId))
                    .findFirst().orElse(null);
            if (existing != null) return "redirect:/calificaciones/" + existing.getId() + "/editar";
        }

        model.addAttribute("proyecto", proyecto);
        model.addAttribute("contratista", persona);
        model.addAttribute("calificacion", new Calificacion());
        return "clientes/calificar";
    }

    // Crear calificación
    @PostMapping("/crear/{proyectoId}/{personaId}")
    public String crearCalificacion(@PathVariable String proyectoId, @PathVariable String personaId,
                                    Calificacion calificacion, HttpSession session) {
        try {
            Usuario usuario = (Usuario) session.getAttribute("usuario");
            if (usuario == null) return "redirect:/desboard";

            Proyecto proyecto = proyectoRepository.findById(proyectoId).orElse(null);
            Persona persona = personaRepository.findById(personaId).orElse(null);
            if (proyecto == null || persona == null) return "redirect:/desboard";

            // Evitar calificación duplicada
            if (calificacionRepository.existsByProyectoIdAndContratistaId(proyectoId, personaId)) {
                session.setAttribute("error", "Ya existe una calificación para este proyecto y contratista.");
                return "redirect:/calificaciones/contratistas";
            }

            calificacion.setContratista(persona);
            calificacion.setProyecto(proyecto);
            calificacion.setFecha(java.time.LocalDateTime.now());
            calificacionRepository.save(calificacion);

            // Recalcular promedio
            List<Calificacion> lista = calificacionRepository.findByContratistaId(personaId);
            double avg = lista.stream().mapToInt(Calificacion::getPuntuacion).average().orElse(0.0);
            if (persona.getEmail() != null) {
                com.obratech.entity.Contratista contratista = contratistaRepository.findAll().stream()
                        .filter(c -> persona.getEmail().equalsIgnoreCase(c.getEmail()))
                        .findFirst().orElse(null);
                if (contratista != null) {
                    contratista.setCalificacionPromedio(avg);
                    contratistaRepository.save(contratista);
                }
            }

            session.setAttribute("mensaje", "Calificación guardada.");
            return "redirect:/calificaciones/contratistas";

        } catch (Exception e) {
            return "redirect:/desboard";
        }
    }

    // Editar calificación existente
    @GetMapping("/{id}/editar")
    public String editarForm(@PathVariable String id, HttpSession session, Model model) {
        try {
            Usuario usuario = (Usuario) session.getAttribute("usuario");
            if (usuario == null) return "redirect:/desboard";

            Calificacion calificacion = calificacionRepository.findById(id).orElse(null);
            if (calificacion == null || calificacion.getProyecto() == null) return "redirect:/desboard";

            model.addAttribute("proyecto", calificacion.getProyecto());
            model.addAttribute("contratista", calificacion.getContratista());
            model.addAttribute("calificacion", calificacion);
            return "clientes/calificar";

        } catch (Exception e) {
            return "redirect:/desboard";
        }
    }

    @PostMapping("/{id}/editar")
    public String editarCalificacion(@PathVariable String id, Calificacion calificacionForm, HttpSession session) {
        try {
            Usuario usuario = (Usuario) session.getAttribute("usuario");
            if (usuario == null) return "redirect:/desboard";

            Calificacion calificacion = calificacionRepository.findById(id).orElse(null);
            if (calificacion == null || calificacion.getProyecto() == null) return "redirect:/desboard";

            calificacion.setPuntuacion(calificacionForm.getPuntuacion());
            calificacion.setComentario(calificacionForm.getComentario());
            calificacionRepository.save(calificacion);

            // Recalcular promedio
            List<Calificacion> lista = calificacionRepository.findByContratistaId(calificacion.getContratista().getId());
            double avg = lista.stream().mapToInt(Calificacion::getPuntuacion).average().orElse(0.0);
            if (calificacion.getContratista() != null && calificacion.getContratista().getEmail() != null) {
                com.obratech.entity.Contratista contratista = contratistaRepository.findAll().stream()
                        .filter(c -> calificacion.getContratista().getEmail().equalsIgnoreCase(c.getEmail()))
                        .findFirst().orElse(null);
                if (contratista != null) {
                    contratista.setCalificacionPromedio(avg);
                    contratistaRepository.save(contratista);
                }
            }

            session.setAttribute("mensaje", "Calificación actualizada.");
            return "redirect:/calificaciones/contratistas";

        } catch (Exception e) {
            return "redirect:/desboard";
        }
    }
}
