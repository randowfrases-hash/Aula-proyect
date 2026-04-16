package com.obratech.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.obratech.entity.Postulacion;

public interface PostulacionRepository extends MongoRepository<Postulacion, String> {
    List<Postulacion> findByProyectoId(String proyectoId);
    List<Postulacion> findByUsuarioId(Long usuarioId);
    boolean existsByProyectoIdAndUsuarioId(String id, String usuarioId);
}
