package org.example.dao;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import org.example.domain.Recipe;

@RepositoryRestResource
public interface RecipeRepository extends JpaRepository<Recipe, Integer> {
}
