package com.obratech.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.obratech.entity.Cliente;
import com.obratech.entity.Contratista;
import com.obratech.entity.Usuario;
import com.obratech.repository.CalificacionRepository;
import com.obratech.repository.ClienteRepository;
import com.obratech.repository.ContratistaRepository;
import com.obratech.repository.PersonaRepository;

import jakarta.servlet.http.HttpSession;

@Controller
public class IndexControllers {

    @Autowired
    private ContratistaRepository contratistaRepository;
    
    @Autowired
    private ClienteRepository clienteRepository;
    
    @Autowired
    private PersonaRepository personaRepository;
    
    @Autowired
    private CalificacionRepository calificacionRepository;

    @GetMapping({"/index"})
    public String mostrarIndex() {
        return "index"; 
    }

    // Ver perfil del contratista logueado
    @GetMapping("/perfil-contratista")
    public String verPerfilContratista(HttpSession session, Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");

        if (usuario == null) {
            return "redirect:/login";
        }

        if (!"ROLE_CONTRACTOR".equals(usuario.getRole())) {
            return "redirect:/desboard";
        }

        // Buscar contratista por username
        Contratista contratista = contratistaRepository.findByUsernameIgnoreCase(usuario.getUsername());

        if (contratista == null) {
            return "redirect:/desboard";
        }

        model.addAttribute("contratista", contratista);
        model.addAttribute("usuario", usuario);

        // Intentar encontrar persona relacionada por email para mostrar calificaciones
        if (contratista.getEmail() != null) {
            com.obratech.entity.Persona persona = personaRepository.findAll()
                    .stream()
                    .filter(p -> p.getEmail() != null && p.getEmail().equalsIgnoreCase(contratista.getEmail()))
                    .findFirst()
                    .orElse(null);

            if (persona != null) {
                java.util.List<com.obratech.entity.Calificacion> calificaciones = calificacionRepository
                        .findByContratistaId(persona.getId());
                model.addAttribute("calificaciones", calificaciones);
                model.addAttribute("personaId", persona.getId());
            }
        }

        return "perfil-contratista";
    }

    // Ver perfil del cliente logueado
    @GetMapping("/perfil-cliente")
    public String verPerfilCliente(HttpSession session, Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");

        if (usuario == null) {
            return "redirect:/login";
        }

        if (!"ROLE_CLIENT".equals(usuario.getRole())) {
            return "redirect:/desboard";
        }

        // Buscar cliente por username
        Cliente cliente = clienteRepository.findByUsername(usuario.getUsername());

        // Si no existe el cliente, crear uno vacío
        if (cliente == null) {
            cliente = new Cliente();
            cliente.setUsername(usuario.getUsername());
            cliente.setNombre("Cliente");
            cliente.setApellido("");
            cliente.setTelefono("");
            cliente.setEmpresa("");
            cliente.setActivo(true);
            clienteRepository.save(cliente);
        }

        model.addAttribute("cliente", cliente);
        model.addAttribute("usuario", usuario);

        return "perfil-cliente";
    }

    // Mostrar formulario para editar perfil del cliente
    @GetMapping("/perfil-cliente/editar")
    public String mostrarFormularioEditarPerfilCliente(HttpSession session, Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");

        if (usuario == null) {
            return "redirect:/login";
        }

        if (!"ROLE_CLIENT".equals(usuario.getRole())) {
            return "redirect:/desboard";
        }

        // Buscar cliente por username
        Cliente cliente = clienteRepository.findByUsername(usuario.getUsername());

        // Si no existe el cliente, crear uno vacío
        if (cliente == null) {
            cliente = new Cliente();
            cliente.setUsername(usuario.getUsername());
            cliente.setNombre("Cliente");
            cliente.setApellido("");
            cliente.setTelefono("");
            cliente.setEmpresa("");
            cliente.setActivo(true);
            clienteRepository.save(cliente);
        }

        model.addAttribute("cliente", cliente);
        model.addAttribute("usuario", usuario);

        return "editar-perfil-cliente";
    }

    // Guardar cambios en perfil del cliente
    @PostMapping("/perfil-cliente/editar")
    public String guardarPerfilCliente(
            @RequestParam String nombre,
            @RequestParam String apellido,
            @RequestParam String telefono,
            @RequestParam String empresa,
            HttpSession session,
            Model model) {

        Usuario usuario = (Usuario) session.getAttribute("usuario");

        if (usuario == null) {
            return "redirect:/login";
        }

        if (!"ROLE_CLIENT".equals(usuario.getRole())) {
            return "redirect:/desboard";
        }

        // Buscar cliente por username
        Cliente cliente = clienteRepository.findByUsername(usuario.getUsername());

        if (cliente == null) {
            cliente = new Cliente();
            cliente.setUsername(usuario.getUsername());
            cliente.setNombre("Cliente");
            cliente.setActivo(true);
        }

        // Actualizar datos del cliente
        cliente.setNombre(nombre);
        cliente.setApellido(apellido);
        cliente.setTelefono(telefono);
        cliente.setEmpresa(empresa);

        // Guardar cambios
        clienteRepository.save(cliente);

        return "redirect:/perfil-cliente?exito=true";
    }
}
