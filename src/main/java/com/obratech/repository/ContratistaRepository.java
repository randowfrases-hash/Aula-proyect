
package com.obratech.repository;

import com.obratech.entity.Contratista;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ContratistaRepository extends MongoRepository<Contratista, String> {
	Contratista findByUsername(String username);
	// case-insensitive lookup to avoid mismatches between Usuario.username and Contratista.username
	Contratista findByUsernameIgnoreCase(String username);
}
