package com.obratech.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.obratech.entity.Cliente;
import com.obratech.entity.Contratista;
import com.obratech.entity.Proyecto;
import com.obratech.entity.Usuario;
import com.obratech.repository.ClienteRepository;
import com.obratech.repository.ContratistaRepository;
import com.obratech.repository.ProyectoRepository;
import com.obratech.repository.PersonaRepository;
import com.obratech.repository.CalificacionRepository;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired private ProyectoRepository proyectoRepository;
    @Autowired private ContratistaRepository contratistaRepository;
    @Autowired private PersonaRepository personaRepository;
    @Autowired private CalificacionRepository calificacionRepository;
    @Autowired private ClienteRepository clienteRepository;

    @GetMapping("/desboard")
    public String dashboardAdmin(HttpSession session, Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null || !"ROLE_ADMIN".equals(usuario.getRole())) return "redirect:/login";

        List<Proyecto> proyectosPendientes = proyectoRepository.findAll()
                .stream().filter(p -> "PENDIENTE".equals(p.getEstadoValidacion())).toList();

        List<Contratista> contratistas = contratistaRepository.findAll();
        List<Cliente> clientes = clienteRepository.findAll();

        model.addAttribute("usuario", usuario);
        model.addAttribute("proyectosPendientes", proyectosPendientes);
        model.addAttribute("totalProyectos", proyectosPendientes.size());
        model.addAttribute("contratistas", contratistas);
        model.addAttribute("totalContratistas", contratistas.size());
        model.addAttribute("clientes", clientes);
        model.addAttribute("totalClientes", clientes.size());

        return "desboard-admin";
    }

    @PostMapping("/proyectos/{id}/aprobar")
    public String aprobarProyecto(@PathVariable String id, HttpSession session) {
        if (!isAdmin(session)) return "redirect:/login";
        Proyecto p = proyectoRepository.findById(id).orElse(null);
        if (p != null) { p.setEstadoValidacion("APROBADO"); proyectoRepository.save(p); }
        return "redirect:/admin/desboard";
    }

    @PostMapping("/proyectos/{id}/rechazar")
    public String rechazarProyecto(@PathVariable String id, HttpSession session) {
        if (!isAdmin(session)) return "redirect:/login";
        Proyecto p = proyectoRepository.findById(id).orElse(null);
        if (p != null) { p.setEstadoValidacion("RECHAZADO"); proyectoRepository.save(p); }
        return "redirect:/admin/desboard";
    }

    @PostMapping("/contratistas/{id}/toggle")
    public String toggleContratista(@PathVariable String id, HttpSession session) {
        if (!isAdmin(session)) return "redirect:/login";
        Contratista c = contratistaRepository.findById(id).orElse(null);
        if (c != null) { c.setActivo(c.getActivo() == null ? false : !c.getActivo()); contratistaRepository.save(c); }
        return "redirect:/admin/desboard";
    }

    @PostMapping("/clientes/{id}/toggle")
    public String toggleCliente(@PathVariable String id, HttpSession session) {
        if (!isAdmin(session)) return "redirect:/login";
        Cliente c = clienteRepository.findById(id).orElse(null);
        if (c != null) { c.setActivo(c.getActivo() == null ? false : !c.getActivo()); clienteRepository.save(c); }
        return "redirect:/admin/desboard";
    }

    @GetMapping("/contratistas/{id}")
    public String verDetallesContratista(@PathVariable String id, HttpSession session, Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null || !"ROLE_ADMIN".equals(usuario.getRole())) return "redirect:/login";

        Contratista contratista = contratistaRepository.findById(id).orElse(null);
        if (contratista == null) return "redirect:/admin/desboard";

        model.addAttribute("contratista", contratista);

        if (contratista.getEmail() != null) {
            com.obratech.entity.Persona persona = personaRepository.findAll().stream()
                    .filter(p -> p.getEmail() != null && p.getEmail().equalsIgnoreCase(contratista.getEmail()))
                    .findFirst().orElse(null);
            if (persona != null) {
                model.addAttribute("calificaciones", calificacionRepository.findByContratistaId(persona.getId()));
            }
        }

        List<Proyecto> proyectosDelContratista = proyectoRepository.findAll().stream()
                .filter(p -> p.getContratistaAsignado() != null && p.getContratistaAsignado().getId().equals(id))
                .toList();
        model.addAttribute("proyectosDelContratista", proyectosDelContratista);
        model.addAttribute("usuario", usuario);
        return "admin-detalles-contratista";
    }

    @Autowired private com.obratech.repository.TrabajadorRepository trabajadorRepository;

    @PostMapping("/trabajadores/{id}/toggle")
    public String toggleTrabajador(@PathVariable String id, HttpSession session) {
        if (!isAdmin(session)) return "redirect:/login";
        com.obratech.entity.Trabajador t = trabajadorRepository.findById(id).orElse(null);
        if (t != null) {
            t.setActivo(!t.getActivo());
            trabajadorRepository.save(t);
        }
        return "redirect:/trabajadores";
    }

    private boolean isAdmin(HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        return usuario != null && "ROLE_ADMIN".equals(usuario.getRole());
    }
}
