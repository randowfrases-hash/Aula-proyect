package com.obratech.repository;

import com.obratech.entity.Proyecto;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProyectoRepository extends MongoRepository<Proyecto, String> {
}
