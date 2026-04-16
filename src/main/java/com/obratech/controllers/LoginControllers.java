package com.obratech.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginControllers {

    @GetMapping("/login")
    public String mostrarLogin(
            @RequestParam(required = false) String error,
            @RequestParam(required = false) String logout,
            @RequestParam(required = false) String expired,
            @RequestParam(required = false) String unauthorized,
            @RequestParam(required = false) String blocked,
            @RequestParam(required = false) String registered,
            Model model) {

        if (blocked != null) {
            model.addAttribute("tipoMensaje", "error");
            model.addAttribute("mensaje",
                "Cuenta bloqueada temporalmente por múltiples intentos fallidos. Intenta de nuevo en 15 minutos.");
        } else if (error != null) {
            model.addAttribute("tipoMensaje", "error");
            model.addAttribute("mensaje",
                "Usuario o contraseña incorrectos. Verifica tus datos e intenta de nuevo.");
        } else if (logout != null) {
            model.addAttribute("tipoMensaje", "success");
            model.addAttribute("mensaje", "Has cerrado sesión correctamente. ¡Hasta pronto!");
        } else if (expired != null) {
            model.addAttribute("tipoMensaje", "warning");
            model.addAttribute("mensaje",
                "Tu sesión ha expirado o fue iniciada en otro dispositivo. Por favor inicia sesión nuevamente.");
        } else if (unauthorized != null) {
            model.addAttribute("tipoMensaje", "warning");
            model.addAttribute("mensaje", "Debes iniciar sesión para acceder a esa sección.");
        } else if (registered != null) {
            model.addAttribute("tipoMensaje", "success");
            model.addAttribute("mensaje", "¡Cuenta creada exitosamente! Ya puedes iniciar sesión.");
        }

        return "login";
    }
}
