package com.obratech.runners;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

import com.obratech.entity.Usuario;
import com.obratech.entity.Contratista;
import com.obratech.entity.Cliente;
import com.obratech.entity.Proyecto;
import com.obratech.entity.Postulacion;
import com.obratech.entity.Trabajador;
import com.obratech.entity.Persona;
import com.obratech.entity.Calificacion;

import com.obratech.repository.UsuarioRepository;
import com.obratech.repository.ContratistaRepository;
import com.obratech.repository.ClienteRepository;
import com.obratech.repository.ProyectoRepository;
import com.obratech.repository.PostulacionRepository;
import com.obratech.repository.TrabajadorRepository;
import com.obratech.repository.PersonaRepository;
import com.obratech.repository.CalificacionRepository;

@Component
@Order(1)
public class DataSeeder implements CommandLineRunner {

    private final UsuarioRepository usuarioRepo;
    private final ContratistaRepository contratistaRepo;
    private final ClienteRepository clienteRepo;
    private final ProyectoRepository proyectoRepo;
    private final PostulacionRepository postulacionRepo;
    private final TrabajadorRepository trabajadorRepo;
    private final PersonaRepository personaRepo;
    private final CalificacionRepository calificacionRepo;

    private final Random rnd = new Random();

    @Value("${app.seed.enabled:false}")
    private boolean enabled;

    public DataSeeder(UsuarioRepository usuarioRepo,
                      ContratistaRepository contratistaRepo,
                      ClienteRepository clienteRepo,
                      ProyectoRepository proyectoRepo,
                      PostulacionRepository postulacionRepo,
                      TrabajadorRepository trabajadorRepo,
                      PersonaRepository personaRepo,
                      CalificacionRepository calificacionRepo) {
        this.usuarioRepo = usuarioRepo;
        this.contratistaRepo = contratistaRepo;
        this.clienteRepo = clienteRepo;
        this.proyectoRepo = proyectoRepo;
        this.postulacionRepo = postulacionRepo;
        this.trabajadorRepo = trabajadorRepo;
        this.personaRepo = personaRepo;
        this.calificacionRepo = calificacionRepo;
    }

    @Override
    public void run(String... args) throws Exception {
        if (!enabled) return; // no hacer nada salvo se active la propiedad

        // Si ya existen usuarios, asumimos que la DB ya fue sembrada y salimos para evitar duplicados
        long existingUsers = usuarioRepo.count();
        System.out.println("[DataSeeder] DB check: usuarios=" + existingUsers);


        // Queremos aprox 1000 filas distribuidas. Haremos:
        // Usuarios: 300, Contratistas: 100, Clientes: 50, Proyectos: 200, Postulaciones: 200, Trabajadores: 100, Personas: 30, Calificaciones: 20

        List<Usuario> usuarios = new ArrayList<>();
        for (int i = 0; i < 300; i++) {
            Usuario u = new Usuario();
            u.setUsername(String.format("user%04d@example.com", i));
            u.setPassword("password");
            u.setRole(i % 3 == 0 ? "ROLE_CONTRACTOR" : (i % 3 == 1 ? "ROLE_CLIENT" : "ROLE_USER"));
            usuarios.add(u);
        }
        // Guardar sólo los usuarios que no existan (por si acaso se ejecuta parcialmente antes)
        List<Usuario> usuariosToSave = new ArrayList<>();
        for (Usuario u : usuarios) {
            if (usuarioRepo.findByUsername(u.getUsername()).isEmpty()) {
                usuariosToSave.add(u);
            }
        }
        if (!usuariosToSave.isEmpty()) {
            usuarioRepo.saveAll(usuariosToSave);
        }

        // ====== Crear usuario Administrador =====
        if (usuarioRepo.findByUsername("admin@gmail.com").isEmpty()) {
            Usuario admin = new Usuario();
            admin.setUsername("admin@gmail.com");
            admin.setPassword("administrador2026");
            admin.setRole("ROLE_ADMIN");
            usuarioRepo.save(admin);
            System.out.println("[DataSeeder] Administrador creado: admin@gmail.com");
        }

        // Crear clientes y contratistas derivados de los usuarios
        // Arrays de nombres realistas
        String[] nombresPersonas = {"Juan", "Carlos", "Pedro", "Miguel", "Luis", "Andrés", "Felipe", "Diego", "Roberto", "Jorge", "Fernando", "Tomás", "Santiago", "Mateo", "Sebastián", "Alejandro"};
        String[] apellidosPersonas = {"Gómez", "López", "Martínez", "García", "Rodríguez", "Hernández", "Silva", "Pérez", "González", "Ruiz", "Sánchez", "Ramírez", "Cruz", "Morales"};
        String[] especialidadesContr = {"Diseño de Interiores", "Estructuras Metálicas", "Acabados y Pintura", "Jardinería y Paisajismo", "Instalaciones Eléctricas", "Obra Civil", "Restauración"};
        
        // Crear clientes y contratistas derivados de los usuarios
        List<Contratista> contratistas = new ArrayList<>();
        List<Cliente> clientes = new ArrayList<>();

        int contIndex = 0;
        int cliIndex = 0;
        for (Usuario u : usuarios) {
            if ("ROLE_CONTRACTOR".equals(u.getRole()) && contIndex < 100) {
                Contratista c = new Contratista();
                c.setUsername(u.getUsername());
                c.setNombre(nombresPersonas[rnd.nextInt(nombresPersonas.length)]);
                c.setApellido(apellidosPersonas[rnd.nextInt(apellidosPersonas.length)]);
                c.setEmail(u.getUsername());
                c.setTelefono("300" + String.format("%07d", contIndex));
                c.setEspecialidad(especialidadesContr[rnd.nextInt(especialidadesContr.length)]);
                c.setUbicacion("Ciudad" + (contIndex % 20));
                contratistas.add(c);
                contIndex++;
            }
            if ("ROLE_CLIENT".equals(u.getRole()) && cliIndex < 50) {
                Cliente cl = new Cliente();
                cl.setUsername(u.getUsername());
                cl.setRole(u.getRole());
                cl.setNombre(nombresPersonas[rnd.nextInt(nombresPersonas.length)]);
                cl.setApellido(apellidosPersonas[rnd.nextInt(apellidosPersonas.length)]);
                cl.setEmpresa("Empresa " + cliIndex);
                cl.setTelefono("320" + String.format("%07d", cliIndex));
                clientes.add(cl);
                cliIndex++;
            }
        }
        if (contratistaRepo.count() == 0) {
            contratistaRepo.saveAll(contratistas);
        }
        if (clienteRepo.count() == 0) {
            clienteRepo.saveAll(clientes);
        }

        // Personas (pequeñas)
        if (personaRepo.count() == 0) {
            List<Persona> personas = new ArrayList<>();
            for (int i = 0; i < 30; i++) {
                Persona p = new Persona();
                p.setNombre(nombresPersonas[rnd.nextInt(nombresPersonas.length)]);
                p.setEmail("persona" + i + "@example.com");
                personas.add(p);
            }
            personaRepo.saveAll(personas);
        }

        // Trabajadores
        if (trabajadorRepo.count() == 0) {
            String[] oficiosTrab = {"Albañil", "Plomero", "Electricista", "Carpintero", "Pintor", "Maestro de Obra", "Soldador", "Cristalero", "Topógrafo"};

            List<Trabajador> trabajadores = new ArrayList<>();
            for (int i = 0; i < 100; i++) {
                Trabajador t = new Trabajador();
                String nombre = nombresPersonas[rnd.nextInt(nombresPersonas.length)];
                String apellido = apellidosPersonas[rnd.nextInt(apellidosPersonas.length)];
                t.setNombre(nombre);
                t.setApellido(apellido);
                t.setEmail(nombre.toLowerCase() + "." + apellido.toLowerCase() + rnd.nextInt(100) + "@example.com");
                t.setTelefono("310" + String.format("%07d", i));
                t.setOficio(oficiosTrab[rnd.nextInt(oficiosTrab.length)]);
                t.setExperiencia(1 + rnd.nextInt(20));
                trabajadores.add(t);
            }
            trabajadorRepo.saveAll(trabajadores);
        }
        // Proyectos -> 200, asignar aleatoriamente algunos contratistas y clientes
        if (proyectoRepo.count() == 0) {
            String[] nombresProy = {"Construcción de Edificio Residencial", "Remodelación de Oficina Central", "Diseño Arquitectónico Moderno", "Reparación de Fachada Histórica", "Instalación Eléctrica Industrial", "Modernización de Fontanería", "Construcción de Piscina", "Ampliación de Planta Baja", "Pavimentación de Vía Principal", "Impermeabilización de Techos", "Ampliación de Bodega", "Restauración de Centro Comercial", "Mantenimiento de Zonas Comunes"};
            String[] ubicacionesProy = {"Bogotá", "Medellín", "Cali", "Barranquilla", "Cartagena", "Bucaramanga", "Pereira", "Manizales", "Santa Marta", "Villavicencio"};

            List<Proyecto> proyectos = new ArrayList<>();
            for (int i = 0; i < 200; i++) {
                Proyecto p = new Proyecto();
                p.setTitulo(nombresProy[rnd.nextInt(nombresProy.length)] + " " + (100+i));
                p.setDescripcion("Descripción detallada para el proyecto de " + p.getTitulo().toLowerCase());
                p.setTipoProyecto(i % 2 == 0 ? "Residencial" : "Comercial");
                p.setUbicacion(ubicacionesProy[rnd.nextInt(ubicacionesProy.length)]);
                p.setPresupuesto(10000.0 + rnd.nextInt(500000));
                p.setEstadoAsignacion(i % 4 == 0 ? "Seleccionado" : "Sin asignar");
                p.setEstadoEjecucion(i % 3 == 0 ? "En Progreso" : "Pendiente");
                p.setFechaInicio(LocalDate.now().minusDays(rnd.nextInt(30)));
                p.setFechaEntrega(LocalDate.now().plusDays(rnd.nextInt(90)));
                
                if (clienteRepo.count() > 0) {
                    List<Cliente> clis = clienteRepo.findAll();
                    Cliente c = clis.get(rnd.nextInt(clis.size()));
                    Usuario clienteUsuario = usuarioRepo.findByUsername(c.getUsername()).orElse(null);
                    p.setCliente(clienteUsuario);
                }
                
                if (contratistaRepo.count() > 0 && rnd.nextBoolean()) {
                    List<Contratista> conts = contratistaRepo.findAll();
                    Contratista ca = conts.get(rnd.nextInt(conts.size()));
                    p.setContratistaAsignado(ca);
                }
                proyectos.add(p);
            }
            proyectoRepo.saveAll(proyectos);
        }

        // Postulaciones: 200 random
        if (postulacionRepo.count() == 0 && proyectoRepo.count() > 0) {
            List<Proyecto> allProyectos = proyectoRepo.findAll();
            List<Usuario> allUsuarios = usuarioRepo.findAll();
            List<Postulacion> postulaciones = new ArrayList<>();
            for (int i = 0; i < 200; i++) {
                Postulacion post = new Postulacion();
                Proyecto p = allProyectos.get(rnd.nextInt(allProyectos.size()));
                Usuario u = allUsuarios.get(rnd.nextInt(allUsuarios.size()));
                post.setProyecto(p);
                post.setUsuario(u);
                post.setEstado(rnd.nextBoolean() ? "PENDING" : (rnd.nextBoolean() ? "ACCEPTED" : "REJECTED"));
                postulaciones.add(post);
            }
            postulacionRepo.saveAll(postulaciones);
        }

        // Calificaciones: 20
        if (calificacionRepo.count() == 0 && proyectoRepo.count() > 0 && personaRepo.count() > 0) {
            List<Proyecto> allProyectos = proyectoRepo.findAll();
            List<Persona> allPersonas = personaRepo.findAll();
            List<Calificacion> califs = new ArrayList<>();
            for (int i = 0; i < 20; i++) {
                Calificacion c = new Calificacion();
                c.setComentario("Buen trabajo " + i);
                c.setPuntuacion(1 + rnd.nextInt(5));
                c.setProyecto(allProyectos.get(rnd.nextInt(allProyectos.size())));
                c.setContratista(allPersonas.get(rnd.nextInt(allPersonas.size())));
                califs.add(c);
            }
            calificacionRepo.saveAll(califs);
        }

        System.out.println("[DataSeeder] Seed check/run complete.");
    }
}
