package com.obratech.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.obratech.entity.Usuario;
import com.obratech.service.UsuarioService;

@Controller
public class RegistroControllers {

    private final UsuarioService usuarioService;

    public RegistroControllers(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/registro")
    public String mostrarRegistro() {
        return "registro";
    }

    @PostMapping("/registro")
    public String procesarRegistro(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam(defaultValue = "none") String role,
            Model model) {

        // Validación extra server-side por seguridad
        if (role.equals("none")) {
            model.addAttribute("mensaje", "Debes seleccionar un tipo de usuario.");
            return "registro";
        }

        try {
            Usuario nuevo = new Usuario();
            nuevo.setUsername(username.trim().toLowerCase());
            nuevo.setPassword(password);
            nuevo.setRole(role);

            usuarioService.register(nuevo);

            // Redirige al login con mensaje de éxito
            return "redirect:/login?registered=true";

        } catch (IllegalArgumentException ex) {
            model.addAttribute("mensaje", ex.getMessage());
            return "registro";
        } catch (Exception ex) {
            model.addAttribute("mensaje", "Ocurrió un error inesperado. Por favor intenta de nuevo.");
            return "registro";
        }
    }
}
