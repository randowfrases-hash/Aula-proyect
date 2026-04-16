package com.obratech;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.obratech.entity.*;
import com.obratech.repository.UsuarioRepository;
import java.util.Optional;

@Component
public class DataSyncRunner implements ApplicationRunner {

    private final UsuarioRepository usuarioRepository;
    private final MongoTemplate mongoTemplate;
    private final PasswordEncoder passwordEncoder;

    public DataSyncRunner(UsuarioRepository usuarioRepository, MongoTemplate mongoTemplate, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.mongoTemplate = mongoTemplate;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // Inicializar colecciones vacías si no existen
        Class<?>[] entidades = {
                Usuario.class, Trabajador.class, Proyecto.class,
                Postulacion.class, Persona.class, HistorialProyecto.class,
                Contratista.class, Cliente.class, Calificacion.class
        };

        for (Class<?> clazz : entidades) {
            String collectionName = mongoTemplate.getCollectionName(clazz);
            if (!mongoTemplate.collectionExists(collectionName)) {
                mongoTemplate.createCollection(clazz);
                System.out.println("Colección creada: " + collectionName);
            }
        }

        // Crear administrador base si no existe
        Optional<Usuario> adminOpt = usuarioRepository.findByUsername("admin@gmail.com");
        if (adminOpt.isEmpty()) {
            Usuario admin = new Usuario();
            admin.setUsername("admin@gmail.com");
            admin.setPassword(passwordEncoder.encode("administrador2026"));
            admin.setRole("ROLE_ADMIN");
            usuarioRepository.save(admin);
            System.out.println("Administrador creado: admin@gmail.com");
        }
    }
}
