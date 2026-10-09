package be.lifeisbananas.dao;

import be.lifeisbananas.domain.RawMaterial;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RawMaterialRepository extends JpaRepository<RawMaterial, Long> {

	List<RawMaterial> findAllByOrderByNameAsc();

	Optional<RawMaterial> findByNameIgnoreCase(String name);
}
