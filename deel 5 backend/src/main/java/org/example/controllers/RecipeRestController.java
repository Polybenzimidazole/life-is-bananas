package org.example.controllers;

import org.example.domain.User;
import org.example.service.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import jakarta.validation.Valid;
import org.example.dao.RecipeRepository;
import org.example.domain.recipe;
import org.example.formdata.RecipeData;
import org.example.service.LifeIsBananasService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.web.csrf.CsrfToken;
import java.util.List;

@RestController
@RequestMapping(path = "/recipes", produces = "application/json")
@CrossOrigin(
        origins = {
                "http://localhost:8889",
                "http://localhost:8083"
        },
        allowCredentials = "true"
)
public class RecipeRestController {

    @Autowired
    private LifeIsBananasService lifeisbananasservice;


    @GetMapping("/all")
    public List<recipe> getAll(){
        return lifeisbananasservice.GetAllRecipe();
    }

    @PostMapping("/add")
    public void addRecipe(@Valid @RequestBody RecipeData recipeData) {
        lifeisbananasservice.addRecipe(recipeData);
    }

    @PutMapping("/update/{id}")
    public void updateRecipe(@PathVariable("id") int id, @Valid @RequestBody RecipeData recipeData) {
        lifeisbananasservice.UpdateRecipe(id, recipeData);
    }
    @DeleteMapping("/delete/{id}")
    public void deleteRecipe(@PathVariable("id") int Id) {
        lifeisbananasservice.deleteRecipe(Id);
    }
    @GetMapping("/{id}")
    public recipe getRecipe(@PathVariable("id") int id) {
        return lifeisbananasservice.GetRecipe(id);
    }

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @GetMapping("/whoami")
    public User whoAmI() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String currentPrincipalName = authentication.getName();

        return (User) userDetailsService
                .loadUserByUsername(currentPrincipalName);
    }

    @GetMapping("/csrf")
    public CsrfToken csrf(CsrfToken token) {
        return token;
    }

}
