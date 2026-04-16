package com.obratech.controllers;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.obratech.entity.Contratista;
import com.obratech.entity.Proyecto;
import com.obratech.entity.Trabajador;
import com.obratech.entity.Usuario;
import com.obratech.repository.ContratistaRepository;
import com.obratech.repository.ProyectoRepository;
import com.obratech.repository.TrabajadorRepository;

import jakarta.servlet.http.HttpSession;

@Controller
public class DesboardController {

    @Autowired
    private ContratistaRepository contratistaRepository;
    
    @Autowired
    private TrabajadorRepository trabajadorRepository;
    
    @Autowired
    private ProyectoRepository proyectoRepository;

    @GetMapping("/desboard")
    public String mostrarDesboard(HttpSession session, Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");

        if (usuario == null) {
            // No hay sesión activa, redirige al login
            return "redirect:/login";
        }

        // Añadir el usuario al modelo para que Thymeleaf lo reconozca
        model.addAttribute("usuario", usuario);

        // Si es contratista, buscar y agregar sus datos
        if ("ROLE_CONTRACTOR".equals(usuario.getRole())) {
            Contratista contratista = contratistaRepository.findByUsername(usuario.getUsername());
            if (contratista != null) {
                model.addAttribute("contratista", contratista);
                // Preparar datos seguros para la plantilla (evita accesos nulos en Thymeleaf)
                model.addAttribute("contratistaUsername", 
                    usuario.getUsername() != null ? usuario.getUsername().substring(0, 1).toUpperCase() : "U");
                model.addAttribute("contratistaCalificacionPromedio", 
                    contratista.getCalificacionPromedio() != null ? String.format("%.1f", contratista.getCalificacionPromedio()) : "0.0");
                model.addAttribute("contratistaEmail", 
                    contratista.getEmail() != null ? contratista.getEmail() : "correo@ejemplo.com");
                model.addAttribute("contratistaEspecialidad", 
                    contratista.getEspecialidad() != null ? contratista.getEspecialidad() : "N/A");
                model.addAttribute("contratistaTelefono", 
                    contratista.getTelefono() != null ? contratista.getTelefono() : "N/A");
                } else {
                // Si no existe Contratista, proporciona valores por defecto
                model.addAttribute("contratistaUsername", "U");
                model.addAttribute("contratistaCalificacionPromedio", "0.0");
                model.addAttribute("contratistaEmail", "correo@ejemplo.com");
                model.addAttribute("contratistaEspecialidad", "N/A");
                model.addAttribute("contratistaTelefono", "N/A");
                model.addAttribute("contratistaUbicacion", "N/A");
            }
        }

        // Redireccionar según el rol del usuario
        if (usuario.getRole() != null) {
            return switch (usuario.getRole()) {
                case "ROLE_CONTRACTOR" -> "desboard-contratista";
                case "ROLE_CLIENT" -> "desboard";
                case "ROLE_WORKER" -> "redirect:/desboard-trabajador";
                case "ROLE_ADMIN" -> "redirect:/admin/desboard";
                default -> "redirect:/login";
            };
        }

        // Si no hay rol, redirige al login
        return "redirect:/login";
    }

    @GetMapping("/desboard-contratista")
    public String mostrarDesboardContratista(HttpSession session, Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");

        if (usuario == null || !"ROLE_CONTRACTOR".equals(usuario.getRole())) {
            return "redirect:/login";
        }

        model.addAttribute("usuario", usuario);

        Contratista contratista = contratistaRepository.findByUsername(usuario.getUsername());
        if (contratista != null) {
            model.addAttribute("contratista", contratista);
            model.addAttribute("contratistaUsername", 
                usuario.getUsername() != null ? usuario.getUsername().substring(0, 1).toUpperCase() : "U");
            model.addAttribute("contratistaCalificacionPromedio", 
                contratista.getCalificacionPromedio() != null ? String.format("%.1f", contratista.getCalificacionPromedio()) : "0.0");
            model.addAttribute("contratistaEmail", 
                contratista.getEmail() != null ? contratista.getEmail() : "correo@ejemplo.com");
            model.addAttribute("contratistaEspecialidad", 
                contratista.getEspecialidad() != null ? contratista.getEspecialidad() : "N/A");
            model.addAttribute("contratistaTelefono", 
                contratista.getTelefono() != null ? contratista.getTelefono() : "N/A");
        } else {
            model.addAttribute("contratistaUsername", "U");
            model.addAttribute("contratistaCalificacionPromedio", "0.0");
            model.addAttribute("contratistaEmail", "correo@ejemplo.com");
            model.addAttribute("contratistaEspecialidad", "N/A");
            model.addAttribute("contratistaTelefono", "N/A");
        }

        return "desboard-contratista";
    }

    @GetMapping("/desboard-trabajador")
    public String mostrarDesboardTrabajador(HttpSession session, Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null || !"ROLE_WORKER".equals(usuario.getRole())) return "redirect:/login";

        model.addAttribute("usuario", usuario);
        
        // Buscar trabajador por username
        Trabajador trabajador = trabajadorRepository.findByUsername(usuario.getUsername());
        
        // Si no existe, crear uno vacío para evitar errores en la vista
        if (trabajador == null) {
            Trabajador nuevoTrabajador = new Trabajador();
            nuevoTrabajador.setUsername(usuario.getUsername());
            nuevoTrabajador.setActivo(true);
            nuevoTrabajador.setNombre(usuario.getUsername());
            nuevoTrabajador.setApellido("");
            nuevoTrabajador.setDisponibilidad(false);
            trabajador = nuevoTrabajador;
        }
        
        final Trabajador trabajadorFinal = trabajador;
        model.addAttribute("trabajador", trabajador);

        // Agregar proyectos asignados si el trabajador tiene un ID
        if (trabajador.getId() != null) {
            List<Proyecto> proyectosAsignados = proyectoRepository.findAll().stream()
                .filter(p -> p.getTrabajadoresAsignados() != null && p.getTrabajadoresAsignados().contains(trabajadorFinal))
                .collect(Collectors.toList());
            model.addAttribute("proyectosAsignados", proyectosAsignados);
        } else {
            model.addAttribute("proyectosAsignados", new java.util.ArrayList<>());
        }

        return "desboard-trabajador";
    }
}
