package ch.tbz.recipe.planner.mapper;

import ch.tbz.recipe.planner.domain.Ingredient;
import ch.tbz.recipe.planner.domain.Unit;
import ch.tbz.recipe.planner.entities.IngredientEntity;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

class IngredientEntityMapperTest {

    private final IngredientEntityMapper mapper = new IngredientEntityMapperImpl();

    private IngredientEntity ingredientEntity;
    private Ingredient ingredient;

    @BeforeEach
    void setUp() {
        ingredientEntity = new IngredientEntity(UUID.randomUUID(), "Tomato", "The big ones", Unit.PIECE, 5);
        ingredient = new Ingredient(UUID.randomUUID(), "Rice", "Basmati", Unit.GRAMM, 250);
    }

    @Test
    void entityToDomainMapptAlleFelderKorrekt() {
        Ingredient result = mapper.entityToDomain(ingredientEntity);

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(result.getId()).isEqualTo(ingredientEntity.getId());
        softly.assertThat(result.getName()).isEqualTo(ingredientEntity.getName());
        softly.assertThat(result.getComment()).isEqualTo(ingredientEntity.getComment());
        softly.assertThat(result.getUnit()).isEqualTo(ingredientEntity.getUnit());
        softly.assertThat(result.getAmount()).isEqualTo(ingredientEntity.getAmount());
        softly.assertAll();
    }

    @Test
    void domainToEntityMapptAlleFelderKorrekt() {
        IngredientEntity result = mapper.domainToEntity(ingredient);

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(result.getId()).isEqualTo(ingredient.getId());
        softly.assertThat(result.getName()).isEqualTo(ingredient.getName());
        softly.assertThat(result.getComment()).isEqualTo(ingredient.getComment());
        softly.assertThat(result.getUnit()).isEqualTo(ingredient.getUnit());
        softly.assertThat(result.getAmount()).isEqualTo(ingredient.getAmount());
        softly.assertAll();
    }

    @Test
    void entitiesToDomainsMapptGanzeListe() {
        List<Ingredient> result = mapper.entitiesToDomains(List.of(ingredientEntity));

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(result).hasSize(1);
        softly.assertThat(result.get(0).getName()).isEqualTo("Tomato");
        softly.assertThat(result.get(0).getUnit()).isEqualTo(Unit.PIECE);
        softly.assertAll();
    }
}
