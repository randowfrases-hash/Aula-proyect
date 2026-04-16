package com.obratech.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.obratech.entity.Proyecto;
import com.obratech.entity.Usuario;
import com.obratech.repository.ProyectoRepository;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/postulaciones")
public class PostulacionController {

    @Autowired
    private ProyectoRepository proyectoRepository;
    @Autowired
    private com.obratech.repository.PostulacionRepository postulacionRepository;
    @Autowired
    private com.obratech.repository.ContratistaRepository contratistaRepository;
    // notificacionRepository removed per user request

    // Enviar postulación a un proyecto - PERSISTE EN DB
    @PostMapping("/postular/{id}")
    public String postularseProyecto(
            @PathVariable String id,
            HttpSession session,
            Model model) {

        Usuario usuario = (Usuario) session.getAttribute("usuario");

        if (usuario == null) {
            return "redirect:/login";
        }

        if (!usuario.getRole().equals("ROLE_WORKER") && !usuario.getRole().equals("ROLE_CONTRACTOR")) {
            return "redirect:/desboard";
        }

        Proyecto proyecto = proyectoRepository.findById(id).orElse(null);

        if (proyecto == null) {
            return "redirect:/postulaciones";
        }

        try {
            // Guardar postulación
            com.obratech.entity.Postulacion post = new com.obratech.entity.Postulacion();
            post.setProyecto(proyecto);
            post.setUsuario(usuario);
            // Si en el formulario se añadiera un mensaje se puede recuperar aquí (por ahora nulo)
            // Evitar duplicados: un mismo usuario no puede postular más de una vez al mismo proyecto
            boolean yaPostulo = postulacionRepository.existsByProyectoIdAndUsuarioId(id, usuario.getId());
            if (yaPostulo) {
                session.setAttribute("mensaje", "Ya te postulaste a este proyecto.");
                return "redirect:/postulaciones/" + id;
            }

            postulacionRepository.save(post);

            // Mensaje simple en sesión para mostrar en el detalle
            session.setAttribute("mensaje", "¡Postulación enviada exitosamente! El cliente revisará tu solicitud pronto.");
            // Redirigir al detalle del proyecto para ver confirmación
            return "redirect:/postulaciones/" + id;
        } catch (Exception e) {
            model.addAttribute("error", "Error al procesar tu postulación: " + e.getMessage());
            model.addAttribute("proyecto", proyecto);
            return "trabajadores/detalles-postulacion";
        }
    }

    // Ver todos los proyectos disponibles para postularse
    @GetMapping
    public String verProyectosDisponibles(HttpSession session, Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");

        if (usuario == null) {
            return "redirect:/login";
        }

        if (!usuario.getRole().equals("ROLE_WORKER") && !usuario.getRole().equals("ROLE_CONTRACTOR")) {
            return "redirect:/desboard";
        }

    List<Proyecto> proyectos = proyectoRepository.findAll()
        .stream()
        // Mostrar proyectos cuyo límite de postulación sea nulo (abierto) o no esté vencido
        .filter(p -> p.getFechaLimitePostulacion() == null ||
            !p.getFechaLimitePostulacion().isBefore(java.time.LocalDate.now()))
        .toList();

        model.addAttribute("proyectos", proyectos);
        model.addAttribute("usuario", usuario);
        model.addAttribute("totalProyectos", proyectos.size());

        return "trabajadores/proyectos-disponibles";
    }

    // Handle POST to root (redirect to avoid 405)
    @PostMapping
    public String postulacionesRoot() {
        return "redirect:/postulaciones";
    }

    // Ver detalles de proyecto para postulación - ONLY GET
    // Se expone en dos rutas para compatibilidad con plantillas:
    // GET /postulaciones/ver/{id} y GET /postulaciones/{id}
    @GetMapping({"/ver/{id}", "/{id}"})
    public String verDetallesProyecto(
            @PathVariable String id,
            HttpSession session,
            Model model) {

        Usuario usuario = (Usuario) session.getAttribute("usuario");

        if (usuario == null) {
            return "redirect:/login";
        }

        if (!usuario.getRole().equals("ROLE_WORKER") && !usuario.getRole().equals("ROLE_CONTRACTOR")) {
            return "redirect:/desboard";
        }

        Proyecto proyecto = proyectoRepository.findById(id).orElse(null);

        if (proyecto == null) {
            return "redirect:/postulaciones";
        }

        model.addAttribute("proyecto", proyecto);
        model.addAttribute("usuario", usuario);
        return "trabajadores/detalles-postulacion";
    }

    // Ver postulantes de un proyecto (para el cliente/empresa)
    @GetMapping("/{id}/postulantes")
    public String verPostulantesProyecto(
            @PathVariable String id,
            HttpSession session,
            Model model) {

        Usuario usuario = (Usuario) session.getAttribute("usuario");

        if (usuario == null) {
            return "redirect:/login";
        }

        // Solo el cliente (empresa) puede ver los postulantes de su proyecto
        if (!usuario.getRole().equals("ROLE_CLIENT") && !usuario.getRole().equals("ROLE_ADMIN")) {
            return "redirect:/desboard";
        }

        Proyecto proyecto = proyectoRepository.findById(id).orElse(null);
        if (proyecto == null) {
            return "redirect:/mis-proyectos";
        }

        // Cargar postulaciones
        java.util.List<com.obratech.entity.Postulacion> postulaciones = postulacionRepository.findByProyectoId(id);

        // Indicar si ya existe una postulación aceptada para este proyecto
        boolean tieneAceptada = postulaciones.stream().anyMatch(p -> "ACCEPTED".equalsIgnoreCase(p.getEstado()));

        model.addAttribute("proyecto", proyecto);
        model.addAttribute("postulaciones", postulaciones);
        model.addAttribute("usuario", usuario);
        model.addAttribute("tieneAceptada", tieneAceptada);
        return "clientes/postulantes-proyecto";
    }

    // Ver todas las postulaciones de los proyectos del cliente (vista agregada)
    @GetMapping("/mis-postulaciones")
    public String verMisPostulaciones(HttpSession session, Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");

        if (usuario == null) {
            return "redirect:/login";
        }

        if (!usuario.getRole().equals("ROLE_CLIENT") && !usuario.getRole().equals("ROLE_ADMIN")) {
            return "redirect:/desboard";
        }

    java.util.List<Proyecto> proyectos = proyectoRepository.findAll()
        .stream()
        // compare by username instead of id because Cliente and Usuario are separate entities
        .filter(p -> p.getCliente() != null && p.getCliente().getUsername() != null && p.getCliente().getUsername().equals(usuario.getUsername()))
        .toList();

        java.util.List<com.obratech.entity.Postulacion> todas = new java.util.ArrayList<>();
        for (Proyecto p : proyectos) {
            todas.addAll(postulacionRepository.findByProyectoId(p.getId()));
        }

        model.addAttribute("postulaciones", todas);
        model.addAttribute("usuario", usuario);
        model.addAttribute("totalPostulaciones", todas.size());
        return "clientes/mis-postulaciones";
    }

    // Aceptar una postulacion
    @PostMapping("/{proyectoId}/postulantes/{postId}/aceptar")
    public String aceptarPostulante(
            @PathVariable String proyectoId,
            @PathVariable String postId,
            HttpSession session,
            Model model) {

        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) return "redirect:/login";

        // Solo cliente/admin y dueño del proyecto
        if (!usuario.getRole().equals("ROLE_CLIENT") && !usuario.getRole().equals("ROLE_ADMIN")) {
            return "redirect:/desboard";
        }

        Proyecto proyecto = proyectoRepository.findById(proyectoId).orElse(null);
        if (proyecto == null) return "redirect:/mis-proyectos";

        // Verificar que el usuario es el dueño del proyecto o admin
        if (!usuario.getRole().equals("ROLE_ADMIN") && (proyecto.getCliente() == null || proyecto.getCliente().getUsername() == null || !proyecto.getCliente().getUsername().equals(usuario.getUsername()))) {
            return "redirect:/desboard";
        }

        com.obratech.entity.Postulacion post = postulacionRepository.findById(postId).orElse(null);
        if (post == null || post.getProyecto() == null || !post.getProyecto().getId().equals(proyectoId)) {
            return "redirect:/postulaciones/" + proyectoId + "/postulantes";
        }

        // Aceptar la postulación y rechazar las demás para garantizar que solo haya UNA aceptada por proyecto
        try {
            // Marcar otras postulaciones como REJECTED
            java.util.List<com.obratech.entity.Postulacion> todas = postulacionRepository.findByProyectoId(proyectoId);
            for (com.obratech.entity.Postulacion p : todas) {
                if (!p.getId().equals(postId)) {
                    p.setEstado("REJECTED");
                    postulacionRepository.save(p);
                }
            }

            // Marcar la seleccionada como ACCEPTED
            post.setEstado("ACCEPTED");
            postulacionRepository.save(post);

            // Asignar contratista al proyecto si existe
            String username = post.getUsuario() != null ? post.getUsuario().getUsername() : null;
            if (username != null) {
                com.obratech.entity.Contratista cont = contratistaRepository.findByUsernameIgnoreCase(username);
                if (cont != null) {
                    proyecto.setContratistaAsignado(cont);
                    proyecto.setEstadoAsignacion("Seleccionado (pendiente contratación)");
                    proyectoRepository.save(proyecto);
                    session.setAttribute("mensaje", "Has seleccionado a " + username + " como asignado (pendiente contratación).");
                }
            }
        } catch (Exception ex) {
            session.setAttribute("error", "Ocurrió un error al aceptar la postulación: " + ex.getMessage());
        }

        return "redirect:/postulaciones/" + proyectoId + "/postulantes";
    }

    // Rechazar una postulacion
    @PostMapping("/{proyectoId}/postulantes/{postId}/rechazar")
    public String rechazarPostulante(
            @PathVariable String proyectoId,
            @PathVariable String postId,
            HttpSession session,
            Model model) {

        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) return "redirect:/login";

        if (!usuario.getRole().equals("ROLE_CLIENT") && !usuario.getRole().equals("ROLE_ADMIN")) {
            return "redirect:/desboard";
        }

        Proyecto proyecto = proyectoRepository.findById(proyectoId).orElse(null);
        if (proyecto == null) return "redirect:/mis-proyectos";

        if (!usuario.getRole().equals("ROLE_ADMIN") && (proyecto.getCliente() == null || proyecto.getCliente().getUsername() == null || !proyecto.getCliente().getUsername().equals(usuario.getUsername()))) {
            return "redirect:/desboard";
        }

        com.obratech.entity.Postulacion post = postulacionRepository.findById(postId).orElse(null);
        if (post == null || post.getProyecto() == null || !post.getProyecto().getId().equals(proyectoId)) {
            return "redirect:/postulaciones/" + proyectoId + "/postulantes";
        }

        post.setEstado("REJECTED");
        postulacionRepository.save(post);

        // Notificación eliminada (funcionalidad removida)

        return "redirect:/postulaciones/" + proyectoId + "/postulantes";
    }

    // Eliminar una postulacion
    @PostMapping("/{proyectoId}/postulantes/{postId}/eliminar")
    public String eliminarPostulante(
            @PathVariable String proyectoId,
            @PathVariable String postId,
            HttpSession session,
            Model model) {

        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) return "redirect:/login";

        if (!usuario.getRole().equals("ROLE_CLIENT") && !usuario.getRole().equals("ROLE_ADMIN")) {
            return "redirect:/desboard";
        }

        Proyecto proyecto = proyectoRepository.findById(proyectoId).orElse(null);
        if (proyecto == null) return "redirect:/mis-proyectos";

        if (!usuario.getRole().equals("ROLE_ADMIN") && (proyecto.getCliente() == null || proyecto.getCliente().getUsername() == null || !proyecto.getCliente().getUsername().equals(usuario.getUsername()))) {
            return "redirect:/desboard";
        }

        com.obratech.entity.Postulacion post = postulacionRepository.findById(postId).orElse(null);
        if (post == null || post.getProyecto() == null || !post.getProyecto().getId().equals(proyectoId)) {
            return "redirect:/postulaciones/" + proyectoId + "/postulantes";
        }

        // Si el contratista eliminado era el asignado, desasignarlo
        if (proyecto.getContratistaAsignado() != null && 
            post.getUsuario() != null &&
            proyecto.getContratistaAsignado().getUsername() != null &&
            proyecto.getContratistaAsignado().getUsername().equals(post.getUsuario().getUsername())) {
            proyecto.setContratistaAsignado(null);
            proyecto.setEstadoAsignacion(null);
            proyectoRepository.save(proyecto);
        }

        postulacionRepository.delete(post);
        session.setAttribute("mensaje", "Postulación eliminada exitosamente.");

        return "redirect:/postulaciones/mis-postulaciones";
    }
}
