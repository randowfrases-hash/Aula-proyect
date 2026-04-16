package com.obratech.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class LegacyRedirectController {

    // Maneja enlaces antiguos que apuntaban a /proyectos/postular y evita 404
    @GetMapping({"/proyectos/postular", "/proyectos/postular/"})
    public String redirectProyectosPostular() {
        return "redirect:/postulaciones";
    }

    // Si hay enlaces con id, redirigimos al detalle del proyecto en postulaciones
    @GetMapping("/proyectos/postular/{id}")
    public String redirectProyectosPostularWithId(@PathVariable Long id) {
        return "redirect:/postulaciones/" + id;
    }
}
