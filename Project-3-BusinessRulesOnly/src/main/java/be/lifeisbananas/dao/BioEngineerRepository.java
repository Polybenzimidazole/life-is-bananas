package be.lifeisbananas.dao;

import be.lifeisbananas.domain.BioEngineer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BioEngineerRepository extends JpaRepository<BioEngineer, Long> {

	Optional<BioEngineer> findByEmail(String email);
}
