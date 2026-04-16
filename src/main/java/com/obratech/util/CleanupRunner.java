package com.obratech.util;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.obratech.repository.ClienteRepository;
import com.obratech.repository.ContratistaRepository;

import org.springframework.transaction.annotation.Transactional;

@Component
public class CleanupRunner implements CommandLineRunner {

    private final ClienteRepository clienteRepository;
    private final ContratistaRepository contratistaRepository;

    public CleanupRunner(ClienteRepository clienteRepository, ContratistaRepository contratistaRepository) {
        this.clienteRepository = clienteRepository;
        this.contratistaRepository = contratistaRepository;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // Eliminar clientes sin username
        var clientes = clienteRepository.findAll();
        long removedClientes = clientes.stream()
                .filter(c -> c.getUsername() == null || c.getUsername().trim().isEmpty())
                .peek(c -> clienteRepository.deleteById(c.getId()))
                .count();

        // Eliminar contratistas sin username
        var contratistas = contratistaRepository.findAll();
        long removedContratistas = contratistas.stream()
                .filter(c -> c.getUsername() == null || c.getUsername().trim().isEmpty())
                .peek(c -> contratistaRepository.deleteById(c.getId()))
                .count();

        if (removedClientes > 0 || removedContratistas > 0) {
            System.out.println("[CleanupRunner] Removed empty Cliente rows: " + removedClientes +
                    ", Contratista rows: " + removedContratistas);
        } else {
            System.out.println("[CleanupRunner] No empty Cliente/Contratista rows found.");
        }
    }
}
