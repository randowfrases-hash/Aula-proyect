package com.obratech.controllers;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import jakarta.servlet.http.HttpSession;
import com.obratech.entity.Proyecto;
import com.obratech.entity.Usuario;
import com.obratech.repository.ProyectoRepository;

@Controller
public class MisProyectosController {

    @Autowired
    private ProyectoRepository proyectoRepository;

    @GetMapping("/mis-proyectos")
    public String verMisProyectos(HttpSession session, Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) {
            return "redirect:/login";
        }

        // Solo cliente/admin puede ver sus proyectos
        if (!usuario.getRole().equals("ROLE_CLIENT") && !usuario.getRole().equals("ROLE_ADMIN")) {
            return "redirect:/desboard";
        }

        // Filtrar proyectos del cliente logueado
        List<Proyecto> proyectos = proyectoRepository.findAll()
            .stream()
            .filter(p -> p.getCliente() != null 
                      && p.getCliente().getUsername() != null
                      && p.getCliente().getUsername().equals(usuario.getUsername()))
            .toList();

        model.addAttribute("proyectos", proyectos);
        model.addAttribute("usuario", usuario);
        model.addAttribute("totalProyectos", proyectos.size());

        return "/mis-proyectos"; // <-- asegúrate de tener este HTML en templates/clientes/
    }
}
