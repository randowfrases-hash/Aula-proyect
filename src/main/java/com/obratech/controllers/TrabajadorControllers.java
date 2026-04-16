package com.obratech.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.mongodb.client.gridfs.model.GridFSFile;
import com.obratech.entity.Trabajador;
import com.obratech.entity.Usuario;
import com.obratech.repository.UsuarioRepository;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/trabajadores")
public class TrabajadorControllers {

    @Autowired
    private com.obratech.repository.TrabajadorRepository trabajadorRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private com.obratech.service.UsuarioService usuarioService;

    @Autowired
    private GridFsTemplate gridFsTemplate;

    // GET: Mostrar formulario para crear trabajador
    @GetMapping("/crear")
    public String mostrarFormularioCrear(HttpSession session, Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null || !"ROLE_ADMIN".equals(usuario.getRole())) {
            return "redirect:/login";
        }
        model.addAttribute("trabajador", new Trabajador());
        model.addAttribute("usuario", usuario);
        return "crear-trabajador";
    }

    // POST: Crear nuevo trabajador
    @PostMapping("/crear")
    public String crearTrabajador(
            @ModelAttribute Trabajador trabajador,
            HttpSession session,
            Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null || !"ROLE_ADMIN".equals(usuario.getRole())) {
            return "redirect:/login";
        }

        try {
            // Validar que el email no esté registrado ya
            if (usuarioRepository.findByUsername(trabajador.getEmail()).isPresent()) {
                model.addAttribute("error", "El email ya está registrado.");
                model.addAttribute("usuario", usuario);
                model.addAttribute("trabajador", trabajador);
                return "crear-trabajador";
            }

            // Crear Usuario para el trabajador
            Usuario nuevoUsuario = new Usuario();
            nuevoUsuario.setUsername(trabajador.getEmail()); // El email es el username
            // Generar contraseña aleatoria temporal
            String passwordTemporal = generarPasswordTemporal();
            nuevoUsuario.setPassword(passwordTemporal); // Se encriptará en UsuarioService
            nuevoUsuario.setRole("trabajador"); // El UsuarioService mapeará esto a ROLE_WORKER

            // Registrar usuario (encripta la contraseña y valida)
            usuarioService.register(nuevoUsuario);

            // Crear registro de Trabajador
            trabajador.setUsername(trabajador.getEmail()); // Vincular al username
            trabajador.setActivo(true);
            trabajadorRepository.save(trabajador);

            model.addAttribute("mensaje", "Trabajador creado exitosamente. Contraseña temporal: " + passwordTemporal);
            model.addAttribute("usuario", usuario);
            return "redirect:/trabajadores?exito=true";

        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("usuario", usuario);
            model.addAttribute("trabajador", trabajador);
            return "crear-trabajador";
        } catch (Exception e) {
            model.addAttribute("error", "Error al crear el trabajador: " + e.getMessage());
            model.addAttribute("usuario", usuario);
            model.addAttribute("trabajador", trabajador);
            return "crear-trabajador";
        }
    }

    // Generar contraseña temporal aleatoria
    private String generarPasswordTemporal() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!@#$%";
        StringBuilder pass = new StringBuilder();
        java.util.Random random = new java.util.Random();
        for (int i = 0; i < 12; i++) {
            pass.append(chars.charAt(random.nextInt(chars.length())));
        }
        return pass.toString();
    }

    // Listar todos los trabajadores (Para Contratista / Admin)
    @GetMapping
    public String listarTrabajadores(HttpSession session, Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) return "redirect:/login";
        if ("ROLE_WORKER".equals(usuario.getRole())) return "redirect:/desboard-trabajador";

        List<Trabajador> todos = trabajadorRepository.findAll();
        List<Trabajador> trabajadores;

        // El admin ve todos; los demás solo ven los aprobados y con perfil completo
        if ("ROLE_ADMIN".equals(usuario.getRole())) {
            trabajadores = todos;
        } else {
            trabajadores = todos.stream()
                .filter(t -> Boolean.TRUE.equals(t.getActivo())
                          && t.getNombre() != null && !t.getNombre().isBlank())
                .collect(java.util.stream.Collectors.toList());
        }

        model.addAttribute("trabajadores", trabajadores);
        model.addAttribute("usuario", usuario);
        model.addAttribute("totalTrabajadores", trabajadores.size());
        model.addAttribute("totalPendientes", todos.stream().filter(t -> !Boolean.TRUE.equals(t.getActivo())).count());
        return "listar-trabajadores";
    }

    // Ver detalles de un trabajador (Para Contratista / Admin)
    @GetMapping("/{id}")
    public String verDetallesTrabajador(@PathVariable String id, HttpSession session, Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) return "redirect:/login";
        if ("ROLE_WORKER".equals(usuario.getRole())) return "redirect:/desboard-trabajador";

        Trabajador trabajador = trabajadorRepository.findById(id).orElse(null);
        if (trabajador == null) return "redirect:/trabajadores";

        model.addAttribute("trabajador", trabajador);
        model.addAttribute("usuario", usuario);
        return "detalles-trabajador";
    }

    // Perfil Trabajador (Para sí mismo)
    @GetMapping("/perfil")
    public String perfilTrabajador(HttpSession session, Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null || !"ROLE_WORKER".equals(usuario.getRole())) return "redirect:/login";

        Trabajador trabajador = trabajadorRepository.findByUsername(usuario.getUsername());
        if (trabajador == null) {
            trabajador = new Trabajador();
            trabajador.setUsername(usuario.getUsername());
        }
        
        model.addAttribute("usuario", usuario);
        model.addAttribute("trabajador", trabajador);
        return "editar-trabajador";
    }

    // Actualizar Perfil Trabajador
    @PostMapping("/perfil")
    public String actualizarPerfil(
            HttpSession session,
            @ModelAttribute Trabajador trabajadorEditado,
            @org.springframework.web.bind.annotation.RequestParam(value="cvFile", required=false) org.springframework.web.multipart.MultipartFile cvFile,
            Model model) {

        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null || !"ROLE_WORKER".equals(usuario.getRole())) return "redirect:/login";

        Trabajador trabajador = trabajadorRepository.findByUsername(usuario.getUsername());
        if (trabajador != null) {
            trabajador.setNombre(trabajadorEditado.getNombre());
            trabajador.setApellido(trabajadorEditado.getApellido());
            trabajador.setEmail(trabajadorEditado.getEmail());
            trabajador.setTelefono(trabajadorEditado.getTelefono());
            trabajador.setOficio(trabajadorEditado.getOficio());
            trabajador.setExperiencia(trabajadorEditado.getExperiencia());
            trabajador.setDisponibilidad(trabajadorEditado.getDisponibilidad());

            if (cvFile != null && !cvFile.isEmpty()) {
                try {
                    String fileName = java.util.UUID.randomUUID().toString() + "_" + cvFile.getOriginalFilename();
                    org.bson.types.ObjectId fileId = gridFsTemplate.store(cvFile.getInputStream(), fileName, cvFile.getContentType());
                    trabajador.setCvUrl(fileId.toString());
                } catch (Exception e) {
                    model.addAttribute("error", "Error al subir CV: " + e.getMessage());
                }
            }

            trabajadorRepository.save(trabajador);
        }
        return "redirect:/trabajadores/perfil?exito=true";
    }

    @GetMapping("/disponibles")
    public String listarDisponibles(HttpSession session, Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) return "redirect:/login";

        List<Trabajador> trabajadores = trabajadorRepository.findAll()
                .stream()
                .filter(t -> t.getDisponibilidad() != null && t.getDisponibilidad())
                .toList();

        model.addAttribute("trabajadores", trabajadores);
        model.addAttribute("usuario", usuario);
        model.addAttribute("totalTrabajadores", trabajadores.size());
        return "listar-trabajadores";
    }

    // Endpoint para ver / descargar CV
    @GetMapping("/cv/{fileId}")
    public org.springframework.http.ResponseEntity<org.springframework.core.io.Resource> descargarCv(@PathVariable String fileId) {
        try {
            GridFSFile gridFSFile = gridFsTemplate.findOne(new Query(Criteria.where("_id").is(new org.bson.types.ObjectId(fileId))));
            if (gridFSFile != null) {
                org.springframework.data.mongodb.gridfs.GridFsResource resource = gridFsTemplate.getResource(gridFSFile);
                return org.springframework.http.ResponseEntity.ok()
                        .contentType(org.springframework.http.MediaType.parseMediaType(resource.getContentType()))
                        .body(resource);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return org.springframework.http.ResponseEntity.notFound().build();
    }
}
