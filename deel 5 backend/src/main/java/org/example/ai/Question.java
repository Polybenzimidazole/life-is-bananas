package org.example.ai;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record Question(
        @NotBlank(message = "Vul een vraag in.")
        @Size(max = 2000, message = "Een vraag mag maximaal 2000 tekens bevatten.")
        String question,
        @Positive(message = "Kies een geldig recept.") int recipeId) {
}
