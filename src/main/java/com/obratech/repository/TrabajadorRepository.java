package com.obratech.repository;

import com.obratech.entity.Trabajador;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TrabajadorRepository extends MongoRepository<Trabajador, String> {
    Trabajador findByUsername(String username);
}
