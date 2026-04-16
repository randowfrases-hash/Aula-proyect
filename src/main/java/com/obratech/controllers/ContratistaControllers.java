
package com.obratech.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.obratech.entity.Contratista;
import com.obratech.entity.Usuario;
import com.obratech.entity.Trabajador;
import com.obratech.repository.ContratistaRepository;
import com.obratech.repository.TrabajadorRepository;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/contratistas")
public class ContratistaControllers {

    @Autowired
    private ContratistaRepository contratistaRepository;
    @Autowired
    private com.obratech.repository.PersonaRepository personaRepository;
    @Autowired
    private com.obratech.repository.CalificacionRepository calificacionRepository;
    @Autowired
    private com.obratech.repository.ProyectoRepository proyectoRepository;
    @Autowired
    private TrabajadorRepository trabajadorRepository;
    @Autowired
    private org.springframework.data.mongodb.gridfs.GridFsTemplate gridFsTemplate;

    // Listar todos los contratistas
    @GetMapping
    public String listarContratistas(HttpSession session, Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");

        if (usuario == null) {
            return "redirect:/login";
        }

        List<Contratista> contratistas = contratistaRepository.findAll();
        model.addAttribute("contratistas", contratistas);
        model.addAttribute("usuario", usuario);
        model.addAttribute("totalContratistas", contratistas.size());

        return "listar-contratistas";
    }

    // Ver detalles de un contratista
    @GetMapping("/{id}")
    public String verDetallesContratista(
            @PathVariable String id,
            HttpSession session,
            Model model) {

        Usuario usuario = (Usuario) session.getAttribute("usuario");

        if (usuario == null) {
            return "redirect:/login";
        }

        Contratista contratista = contratistaRepository.findById(id).orElse(null);

        if (contratista == null) {
            return "redirect:/contratistas";
        }

        model.addAttribute("contratista", contratista);
        // Intentar encontrar una Persona relacionada por email para mostrar calificaciones
        if (contratista != null && contratista.getEmail() != null) {
            com.obratech.entity.Persona persona = personaRepository.findAll()
                    .stream()
                    .filter(p -> p.getEmail() != null && p.getEmail().equalsIgnoreCase(contratista.getEmail()))
                    .findFirst().orElse(null);

            if (persona != null) {
                java.util.List<com.obratech.entity.Calificacion> calificaciones = calificacionRepository.findByContratistaId(persona.getId());
                model.addAttribute("calificaciones", calificaciones);
                model.addAttribute("personaId", persona.getId());
            }
        }
        model.addAttribute("usuario", usuario);
        return "detalles-contratista";
    }

    // Ver detalles por username 
    @GetMapping("/por-username/{username}")
    public String verPorUsername(@PathVariable String username, HttpSession session, Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) return "redirect:/login";

        // buscar contratista por username 
        Contratista contratista = contratistaRepository.findByUsernameIgnoreCase(username);
        if (contratista == null) {
            return "redirect:/contratistas";
        }

        model.addAttribute("contratista", contratista);
        // Intentar encontrar persona relacionada por email para mostrar calificaciones
        if (contratista.getEmail() != null) {
            com.obratech.entity.Persona persona = personaRepository.findAll()
                    .stream()
                    .filter(p -> p.getEmail() != null && p.getEmail().equalsIgnoreCase(contratista.getEmail()))
                    .findFirst().orElse(null);

            if (persona != null) {
                java.util.List<com.obratech.entity.Calificacion> calificaciones = calificacionRepository.findByContratistaId(persona.getId());
                model.addAttribute("calificaciones", calificaciones);
                model.addAttribute("personaId", persona.getId());
            }
        }

        model.addAttribute("usuario", usuario);
        return "detalles-contratista";
    }

    // Mostrar formulario para crear contratista
    @GetMapping("/crear")
    public String mostrarFormularioCrear(HttpSession session, Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");

        if (usuario == null) {
            return "redirect:/login";
        }

        // Solo administradores pueden crear contratistas
        if (!usuario.getRole().equals("ROLE_ADMIN")) {
            return "redirect:/desboard";
        }

        model.addAttribute("contratista", new Contratista());
        model.addAttribute("usuario", usuario);
        return "crear-contratista";
    }
    // El contratista edita su propio perfil
    @GetMapping("/editar")
    public String mostrarFormularioEditarPerfil(HttpSession session, Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null || !"ROLE_CONTRACTOR".equals(usuario.getRole())) return "redirect:/login";

        Contratista contratista = contratistaRepository.findByUsernameIgnoreCase(usuario.getUsername());
        if (contratista == null) return "redirect:/perfil-contratista";

        model.addAttribute("contratista", contratista);
        model.addAttribute("usuario", usuario);
        return "editar-contratista";
    }

    @PostMapping("/editar")
    public String guardarPerfilPropio(
            @org.springframework.web.bind.annotation.ModelAttribute Contratista form,
            @org.springframework.web.bind.annotation.RequestParam(value = "cvFile", required = false) org.springframework.web.multipart.MultipartFile cvFile,
            HttpSession session,
            Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null || !"ROLE_CONTRACTOR".equals(usuario.getRole())) return "redirect:/login";

        Contratista contratista = contratistaRepository.findByUsernameIgnoreCase(usuario.getUsername());
        if (contratista == null) return "redirect:/perfil-contratista";

        contratista.setNombre(form.getNombre());
        contratista.setApellido(form.getApellido());
        contratista.setEmail(form.getEmail());
        contratista.setTelefono(form.getTelefono());
        contratista.setEspecialidad(form.getEspecialidad());
        contratista.setDescripcion(form.getDescripcion());
        contratista.setUbicacion(form.getUbicacion());
        if (form.getExperiencia() != null && form.getExperiencia() >= 0) contratista.setExperiencia(form.getExperiencia());

        // Subir CV si se proporcionó
        if (cvFile != null && !cvFile.isEmpty()) {
            try {
                String fileName = java.util.UUID.randomUUID() + "_cv_" + cvFile.getOriginalFilename();
                org.bson.types.ObjectId fileId = gridFsTemplate.store(cvFile.getInputStream(), fileName, cvFile.getContentType());
                contratista.setCvUrl(fileId.toString());
            } catch (Exception e) {
                // Si falla el CV, igualmente guardar los datos del perfil
            }
        }

        contratistaRepository.save(contratista);
        return "redirect:/perfil-contratista?exito=true";
    }

    // Mostrar formulario para contratar a un trabajador
    @GetMapping("/contratar/{trabajadorId}")
    public String formularioContratar(@PathVariable String trabajadorId, HttpSession session, Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null || !"ROLE_CONTRACTOR".equals(usuario.getRole())) return "redirect:/login";

        Contratista contratista = contratistaRepository.findByUsernameIgnoreCase(usuario.getUsername());
        if (contratista == null) return "redirect:/desboard";

        Trabajador trabajador = trabajadorRepository.findById(trabajadorId).orElse(null);
        if (trabajador == null) return "redirect:/trabajadores";

        // Proyectos activos del contratista
        java.util.List<com.obratech.entity.Proyecto> proyectosActivos = proyectoRepository.findAll()
                .stream()
                .filter(p -> p.getContratistaAsignado() != null && p.getContratistaAsignado().getId().equals(contratista.getId()))
                .filter(p -> !"COMPLETADO".equals(p.getEstadoEjecucion()))
                .toList();

        model.addAttribute("usuario", usuario);
        model.addAttribute("trabajador", trabajador);
        model.addAttribute("proyectosActivos", proyectosActivos);
        return "seleccionar-proyecto-contrato";
    }

    // Procesar asignación de trabajador a proyecto
    @PostMapping("/contratar/{trabajadorId}")
    public String contratarTrabajador(@PathVariable String trabajadorId, @org.springframework.web.bind.annotation.RequestParam("proyectoId") String proyectoId, HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null || !"ROLE_CONTRACTOR".equals(usuario.getRole())) return "redirect:/login";

        Contratista contratista = contratistaRepository.findByUsernameIgnoreCase(usuario.getUsername());
        Trabajador trabajador = trabajadorRepository.findById(trabajadorId).orElse(null);
        com.obratech.entity.Proyecto proyecto = proyectoRepository.findById(proyectoId).orElse(null);

        if (contratista != null && trabajador != null && proyecto != null) {
            // Validar que el proyecto realmente es del contratista
            if (proyecto.getContratistaAsignado() != null && proyecto.getContratistaAsignado().getId().equals(contratista.getId())) {
                proyecto.getTrabajadoresAsignados().add(trabajador);
                proyectoRepository.save(proyecto);
            }
        }
        return "redirect:/contratistas/proyectos-asignados";
    }

    // Proyectos asignados al contratista
    @GetMapping("/proyectos-asignados")
    public String proyectosAsignados(HttpSession session, Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) return "redirect:/login";

        // Buscar contratista por username
        Contratista contratista = contratistaRepository.findByUsernameIgnoreCase(usuario.getUsername());
        if (contratista == null) return "redirect:/desboard";

        // Buscar proyectos donde está asignado
        java.util.List<com.obratech.entity.Proyecto> proyectos = proyectoRepository.findAll()
                .stream()
                .filter(p -> p.getContratistaAsignado() != null && p.getContratistaAsignado().getId().equals(contratista.getId()))
                .filter(p -> !"COMPLETADO".equals(p.getEstadoEjecucion()))
                .toList();
        model.addAttribute("proyectos", proyectos);
        model.addAttribute("usuario", usuario);
        return "contratista-proyectos-asignados";
    }

    // Historial de proyectos (Completados)
    @GetMapping("/historial")
    public String historialProyectos(HttpSession session, Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) return "redirect:/login";

        Contratista contratista = contratistaRepository.findByUsernameIgnoreCase(usuario.getUsername());
        if (contratista == null) return "redirect:/desboard";

        java.util.List<com.obratech.entity.Proyecto> historial = proyectoRepository.findAll()
                .stream()
                .filter(p -> p.getContratistaAsignado() != null && p.getContratistaAsignado().getId().equals(contratista.getId()))
                .filter(p -> "COMPLETADO".equals(p.getEstadoEjecucion()))
                .toList();
        model.addAttribute("proyectos", historial);
        model.addAttribute("usuario", usuario);
        model.addAttribute("esHistorial", true);
        return "contratista-historial-proyectos";
    }

    // Buscar contratistas por especialidad
    @GetMapping("/buscar/{especialidad}")
    public String buscarPorEspecialidad(
            @PathVariable String especialidad,
            HttpSession session,
            Model model) {

        Usuario usuario = (Usuario) session.getAttribute("usuario");

        if (usuario == null) {
            return "redirect:/login";
        }

        List<Contratista> contratistas = contratistaRepository.findAll()
                .stream()
                .filter(c -> c.getEspecialidad() != null && 
                            c.getEspecialidad().toLowerCase().contains(especialidad.toLowerCase()))
                .toList();

        model.addAttribute("contratistas", contratistas);
        model.addAttribute("usuario", usuario);
        model.addAttribute("especialidad", especialidad);
        model.addAttribute("totalContratistas", contratistas.size());

        return "listar-contratistas";
    }

    @GetMapping("/para-calificar/{proyectoId}")
public String contratistasParaCalificar(
        @PathVariable String proyectoId,
        HttpSession session,
        Model model) {

    Usuario usuario = (Usuario) session.getAttribute("usuario");
    if (usuario == null) return "redirect:/login";

    List<Contratista> contratistas = contratistaRepository.findAll();
    model.addAttribute("contratistas", contratistas);
    model.addAttribute("usuario", usuario);
    model.addAttribute("totalContratistas", contratistas.size());

    // Agregar proyectoId al modelo para Thymeleaf
    model.addAttribute("proyectoId", proyectoId);

    return "clientes/contratistas-para-calificar"; // tu plantilla HTML
}

}