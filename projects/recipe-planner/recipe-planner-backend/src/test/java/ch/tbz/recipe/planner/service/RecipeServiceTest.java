package ch.tbz.recipe.planner.service;

import ch.tbz.recipe.planner.domain.Recipe;
import ch.tbz.recipe.planner.entities.RecipeEntity;
import ch.tbz.recipe.planner.mapper.RecipeEntityMapper;
import ch.tbz.recipe.planner.repository.RecipeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecipeServiceTest {

    @Mock
    private RecipeEntityMapper mapper;

    @Mock
    private RecipeRepository repository;

    private RecipeService service;

    private UUID id;
    private Recipe recipe;
    private RecipeEntity entity;

    @BeforeEach
    void setUp() {
        service = new RecipeService(mapper, repository);
        id = UUID.randomUUID();
        recipe = new Recipe(null, "Lasagne", "Lecker", "http://example.com/x.jpg", List.of());
        entity = new RecipeEntity(id, "Lasagne", "Lecker", "http://example.com/x.jpg", List.of());
    }

    @Test
    void updateRecipeSpeichertUnterDerUebergebenenIdUndGibtDasResultatZurueck() {
        Recipe erwartet = new Recipe(id, "Lasagne", "Lecker", "http://example.com/x.jpg", List.of());
        when(repository.findById(id)).thenReturn(Optional.of(entity));
        when(mapper.domainToEntity(recipe)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(entity);
        when(mapper.entityToDomain(entity)).thenReturn(erwartet);

        Recipe result = service.updateRecipe(id, recipe);

        assertEquals(id, recipe.getId(), "Die ID aus dem Pfad muss im Rezept stehen");
        assertEquals(erwartet, result);
        verify(repository).save(entity);
    }

    @Test
    void updateRecipeGibtNullZurueckUndSpeichertNichtWennDasRezeptFehlt() {
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertNull(service.updateRecipe(id, recipe));
        verify(repository, never()).save(any());
    }
}
