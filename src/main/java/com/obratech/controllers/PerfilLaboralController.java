package com.obratech.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.obratech.entity.Trabajador;
import com.obratech.entity.Usuario;
import com.obratech.repository.TrabajadorRepository;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/perfil-laboral")
public class PerfilLaboralController {

    @Autowired
    private TrabajadorRepository trabajadorRepository;

    // Ver perfil laboral del trabajador
    @GetMapping
    public String verPerfilLaboral(HttpSession session, Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");

        if (usuario == null) {
            return "redirect:/login";
        }

        if (!usuario.getRole().equals("ROLE_WORKER")) {
            return "redirect:/desboard";
        }

        // Buscar o crear el perfil del trabajador
        // Por ahora mostramos un formulario vacío
        model.addAttribute("usuario", usuario);
        return "trabajadores/perfil-laboral";
    }

    // Guardar o actualizar perfil laboral
    @PostMapping
    public String guardarPerfilLaboral(
            @RequestParam String nombre,
            @RequestParam String apellido,
            @RequestParam String email,
            @RequestParam String telefono,
            @RequestParam String oficio,
            @RequestParam String experiencia,
            @RequestParam String disponibilidad,
            @RequestParam(required = false, defaultValue = "") String descripcion,
            HttpSession session,
            Model model) {

        Usuario usuario = (Usuario) session.getAttribute("usuario");

        if (usuario == null) {
            return "redirect:/login";
        }

        if (!usuario.getRole().equals("ROLE_WORKER")) {
            return "redirect:/desboard";
        }

        try {
            Trabajador trabajador = new Trabajador();
            trabajador.setNombre(nombre);
            trabajador.setApellido(apellido);
            trabajador.setEmail(email);
            trabajador.setTelefono(telefono);
            trabajador.setOficio(oficio);
            trabajador.setExperiencia(parseExperiencia(experiencia));
            trabajador.setDisponibilidad(Boolean.parseBoolean(disponibilidad));
            trabajador.setUsername(usuario.getUsername());
            trabajador.setRole(usuario.getRole());

            trabajadorRepository.save(trabajador);

            model.addAttribute("mensaje", "¡Perfil laboral actualizado exitosamente!");
            return "redirect:/desboard-trabajador";
        } catch (Exception e) {
            model.addAttribute("error", "Error al guardar el perfil: " + e.getMessage());
            model.addAttribute("usuario", usuario);
            return "trabajadores/perfil-laboral";
        }
    }

    // Método auxiliar para parsear experiencia
    private Integer parseExperiencia(String experiencia) {
        try {
            // Si es un formato como "2 a 5 años", extraer el primer número
            if (experiencia.contains(" ")) {
                String[] parts = experiencia.split(" ");
                if ("Menos".equals(parts[0])) return 0;
                return Integer.parseInt(parts[0]);
            }
            return Integer.parseInt(experiencia);
        } catch (Exception e) {
            // Asignar valor por defecto según categoría
            switch (experiencia.toLowerCase()) {
                case "menos de 1 año": return 0;
                case "1 a 2 años": return 1;
                case "2 a 5 años": return 2;
                case "5 a 10 años": return 5;
                case "más de 10 años": return 10;
                default: return 0;
            }
        }
    }
}
