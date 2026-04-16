
package com.obratech.repository;

import com.obratech.entity.Cliente;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ClienteRepository extends MongoRepository<Cliente, String> {
    Cliente findByUsername(String username);
}
