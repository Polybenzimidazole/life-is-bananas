package org.example.dao;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.ListCrudRepository;
import org.example.domain.recipe;
import java.util.List;

public interface RecipeRepository extends CrudRepository<recipe, Integer>  {

}
