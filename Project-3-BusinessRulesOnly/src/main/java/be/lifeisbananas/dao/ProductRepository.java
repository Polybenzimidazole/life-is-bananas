package be.lifeisbananas.dao;

import be.lifeisbananas.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

	List<Product> findByFormulaIdOrderByNameAsc(Long formulaId);
}
