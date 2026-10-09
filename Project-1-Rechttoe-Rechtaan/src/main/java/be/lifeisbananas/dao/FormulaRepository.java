package be.lifeisbananas.dao;

import be.lifeisbananas.domain.Formula;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FormulaRepository extends JpaRepository<Formula, Long> {

	/** Zoekt op een stuk van de naam, hoofdletters maken niet uit. */
	List<Formula> findByNameContainingIgnoreCaseOrderByNameAsc(String deelVanDeNaam);

	List<Formula> findAllByOrderByNameAsc();

	/**
	 * Haalt de receptuur op mét haar ingrediënten. Zonder deze join zou de
	 * detailpagina die lijst pas aanraken als de sessie al gesloten is
	 * (open-in-view staat uit).
	 */
	@Query("""
			select f from Formula f
			left join fetch f.ingredients i
			left join fetch i.rawMaterial
			where f.id = :id
			""")
	Optional<Formula> findByIdWithIngredients(@Param("id") Long id);
}
