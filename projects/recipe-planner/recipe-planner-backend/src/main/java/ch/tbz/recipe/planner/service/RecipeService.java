package ch.tbz.recipe.planner.service;

import ch.tbz.recipe.planner.entities.RecipeEntity;
import ch.tbz.recipe.planner.mapper.RecipeEntityMapper;
import ch.tbz.recipe.planner.repository.RecipeRepository;
import ch.tbz.recipe.planner.domain.Recipe;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class RecipeService {

    RecipeEntityMapper mapper;

    RecipeRepository repository;

    public RecipeService(RecipeEntityMapper mapper, RecipeRepository repository) {
        this.mapper = mapper;
        this.repository = repository;
    }

    public List<Recipe> getRecipes() {
        List<RecipeEntity> recipesEntities = repository.findAll();
        return recipesEntities.stream().map(mapper::entityToDomain).toList();
    }

    public Recipe getRecipeById(UUID recipeId) {
        return mapper.entityToDomain(repository.findById(recipeId).orElse(null));
    }

    public Recipe addRecipe(Recipe recipe) {
        var createdRecipe = repository.save(mapper.domainToEntity(recipe));
        return mapper.entityToDomain(createdRecipe);
    }

    /**
     * Ueberschreibt ein bestehendes Rezept. Gibt null zurueck, wenn es kein
     * Rezept mit dieser ID gibt (der Controller macht daraus ein 404).
     */
    public Recipe updateRecipe(UUID recipeId, Recipe recipe) {
        if (repository.findById(recipeId).isEmpty()) {
            return null;
        }
        recipe.setId(recipeId);
        var updatedRecipe = repository.save(mapper.domainToEntity(recipe));
        return mapper.entityToDomain(updatedRecipe);
    }
}
