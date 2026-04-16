package com.obratech.controllers;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.obratech.entity.Calificacion;
import com.obratech.entity.Cliente;
import com.obratech.entity.Contratista;
import com.obratech.entity.Proyecto;
import com.obratech.entity.Usuario;
import com.obratech.repository.CalificacionRepository;
import com.obratech.repository.ClienteRepository;
import com.obratech.repository.ContratistaRepository;
import com.obratech.repository.ProyectoRepository;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/clientes")
public class ClienteControllers {

    @Autowired private ClienteRepository clienteRepository;
    @Autowired private ProyectoRepository proyectoRepository;
    @Autowired private ContratistaRepository contratistaRepository;
    @Autowired private CalificacionRepository calificacionRepository;

    @GetMapping
    public String listarClientes(HttpSession session, Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) return "redirect:/login";

        List<Cliente> clientes = clienteRepository.findAll();
        model.addAttribute("clientes", clientes);
        model.addAttribute("usuario", usuario);
        model.addAttribute("totalClientes", clientes.size());
        return "listar-clientes";
    }

    @GetMapping("/mis-contratistas")
    public String misContratistas(HttpSession session, Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null || !"ROLE_CLIENT".equals(usuario.getRole())) return "redirect:/login";

        List<Proyecto> proyectosDelCliente = proyectoRepository.findAll().stream()
                .filter(p -> p.getCliente() != null && usuario.getUsername().equals(p.getCliente().getUsername()))
                .collect(Collectors.toList());

        List<Contratista> contratistas = proyectosDelCliente.stream()
                .filter(p -> p.getContratistaAsignado() != null)
                .map(Proyecto::getContratistaAsignado)
                .distinct()
                .collect(Collectors.toList());

        model.addAttribute("usuario", usuario);
        model.addAttribute("proyectos", proyectosDelCliente);
        model.addAttribute("contratistas", contratistas);
        return "clientes/mis-contratistas";
    }

    @GetMapping("/reportes")
    public String reportes(HttpSession session, Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null || !"ROLE_CLIENT".equals(usuario.getRole())) return "redirect:/login";

        List<Proyecto> todosLosProyectos = proyectoRepository.findAll().stream()
                .filter(p -> p.getCliente() != null && usuario.getUsername().equals(p.getCliente().getUsername()))
                .collect(Collectors.toList());

        long completados = todosLosProyectos.stream().filter(p -> "COMPLETADO".equals(p.getEstadoEjecucion())).count();
        long enProgreso  = todosLosProyectos.stream().filter(p -> "En Progreso".equals(p.getEstadoEjecucion())).count();
        long pendientes  = todosLosProyectos.stream().filter(p -> "Pendiente".equals(p.getEstadoEjecucion())).count();
        long conContratista = todosLosProyectos.stream().filter(p -> p.getContratistaAsignado() != null).count();
        long calificacionesDadas = calificacionRepository.findAll().size();

        model.addAttribute("usuario", usuario);
        model.addAttribute("totalProyectos", todosLosProyectos.size());
        model.addAttribute("proyectosCompletados", completados);
        model.addAttribute("proyectosEnProgreso", enProgreso);
        model.addAttribute("proyectosPendientes", pendientes);
        model.addAttribute("proyectosConContratista", conContratista);
        model.addAttribute("calificacionesDadas", calificacionesDadas);
        model.addAttribute("proyectos", todosLosProyectos);
        return "clientes/reportes";
    }
}
