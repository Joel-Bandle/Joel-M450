package ch.tbz.recipe.planner.mapper;

import ch.tbz.recipe.planner.domain.Ingredient;
import ch.tbz.recipe.planner.domain.Recipe;
import ch.tbz.recipe.planner.domain.Unit;
import ch.tbz.recipe.planner.entities.IngredientEntity;
import ch.tbz.recipe.planner.entities.RecipeEntity;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

/**
 * Reiner Unit-Test ohne Spring-Kontext: der von MapStruct generierte
 * RecipeEntityMapperImpl laesst sich ganz normal per "new" instanzieren,
 * unabhaengig vom componentModel = "spring".
 *
 * Vorteil von SoftAssertions: Normale Assertions (assertThat/assertEquals)
 * brechen beim ERSTEN fehlschlagenden Feld sofort ab, man sieht also pro
 * Testlauf hoechstens einen Fehler und muss fixen-neu-ausfuehren-wiederholen.
 * SoftAssertions sammelt dagegen ALLE fehlgeschlagenen Pruefungen und wirft
 * sie erst bei softly.assertAll() gesammelt als eine Fehlermeldung -- bei
 * einem Mapper mit vielen Feldern sieht man so auf einen Blick, welche
 * Felder ALLE falsch gemappt wurden, statt sie einzeln nacheinander
 * aufzudecken.
 */
class RecipeEntityMapperTest {

    private final RecipeEntityMapper mapper = new RecipeEntityMapperImpl();

    private RecipeEntity recipeEntity;
    private Recipe recipe;

    @BeforeEach
    void setUp() {
        IngredientEntity tomatoEntity = new IngredientEntity(UUID.randomUUID(), "Tomato", "The big ones", Unit.PIECE, 5);
        recipeEntity = new RecipeEntity(UUID.randomUUID(), "Lasagne al Forno", "Lecker", "http://example.com/lasagne.jpg", List.of(tomatoEntity));

        Ingredient tomato = new Ingredient(UUID.randomUUID(), "Tomato", "The big ones", Unit.PIECE, 5);
        recipe = new Recipe(UUID.randomUUID(), "Spaghetti Bolognese", "Auch lecker", "http://example.com/spaghetti.jpg", List.of(tomato));
    }

    @Test
    void entityToDomainMapptAlleFelderKorrekt() {
        Recipe result = mapper.entityToDomain(recipeEntity);

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(result.getId()).isEqualTo(recipeEntity.getId());
        softly.assertThat(result.getName()).isEqualTo(recipeEntity.getName());
        softly.assertThat(result.getDescription()).isEqualTo(recipeEntity.getDescription());
        softly.assertThat(result.getImageUrl()).isEqualTo(recipeEntity.getImageUrl());
        softly.assertThat(result.getIngredients()).hasSize(1);
        softly.assertThat(result.getIngredients().get(0).getName()).isEqualTo("Tomato");
        softly.assertThat(result.getIngredients().get(0).getUnit()).isEqualTo(Unit.PIECE);
        softly.assertThat(result.getIngredients().get(0).getAmount()).isEqualTo(5);
        softly.assertAll();
    }

    @Test
    void domainToEntityMapptAlleFelderKorrekt() {
        RecipeEntity result = mapper.domainToEntity(recipe);

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(result.getId()).isEqualTo(recipe.getId());
        softly.assertThat(result.getName()).isEqualTo(recipe.getName());
        softly.assertThat(result.getDescription()).isEqualTo(recipe.getDescription());
        softly.assertThat(result.getImageUrl()).isEqualTo(recipe.getImageUrl());
        softly.assertThat(result.getIngredients()).hasSize(1);
        softly.assertThat(result.getIngredients().get(0).getName()).isEqualTo("Tomato");
        softly.assertAll();
    }

    @Test
    void entityToDomainMitNullGibtNullZurueck() {
        org.assertj.core.api.Assertions.assertThat(mapper.entityToDomain(null)).isNull();
    }
}
