package org.example.dao;

import org.example.domain.RawMaterial;
import org.springframework.data.repository.CrudRepository;

public interface RawMaterialRepository
        extends CrudRepository<RawMaterial, Integer> {
}
