package ch.tbz.recipe.planner.controller;

import ch.tbz.recipe.planner.domain.Ingredient;
import ch.tbz.recipe.planner.domain.Recipe;
import ch.tbz.recipe.planner.domain.Unit;
import ch.tbz.recipe.planner.mapper.RecipeEntityMapper;
import ch.tbz.recipe.planner.repository.RecipeRepository;
import ch.tbz.recipe.planner.service.RecipeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RecipeController.class)
class RecipeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RecipeService recipeService;

    @MockBean
    private RecipeEntityMapper recipeEntityMapper;

    @MockBean
    private RecipeRepository recipeRepository;

    private Recipe lasagne;

    @BeforeEach
    void setUp() {
        Ingredient tomato = new Ingredient(UUID.randomUUID(), "Tomato", "The big ones", Unit.PIECE, 5);
        lasagne = new Recipe(UUID.randomUUID(), "Lasagne al Forno", "Lecker", "http://example.com/lasagne.jpg", List.of(tomato));
    }

    @Test
    void getRecipesGibt200UndAlleRezepteZurueck() throws Exception {
        when(recipeService.getRecipes()).thenReturn(List.of(lasagne));

        mockMvc.perform(get("/api/recipes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Lasagne al Forno"))
                .andExpect(jsonPath("$[0].ingredients[0].name").value("Tomato"));
    }

    @Test
    void getRecipeGibt200UndDasGesuchteRezeptZurueck() throws Exception {
        when(recipeService.getRecipeById(lasagne.getId())).thenReturn(lasagne);

        mockMvc.perform(get("/api/recipes/recipe/{recipeId}", lasagne.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Lasagne al Forno"))
                .andExpect(jsonPath("$.description").value("Lecker"));
    }

    @Test
    void addRecipeGibt200UndDasGespeicherteRezeptZurueck() throws Exception {
        when(recipeService.addRecipe(org.mockito.ArgumentMatchers.any(Recipe.class))).thenReturn(lasagne);

        mockMvc.perform(post("/api/recipes")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(lasagne)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Lasagne al Forno"));
    }

    @Test
    void updateRecipeGibt200UndDasAktualisierteRezeptZurueck() throws Exception {
        when(recipeService.updateRecipe(org.mockito.ArgumentMatchers.eq(lasagne.getId()), org.mockito.ArgumentMatchers.any(Recipe.class)))
                .thenReturn(lasagne);

        mockMvc.perform(put("/api/recipes/{recipeId}", lasagne.getId())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(lasagne)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Lasagne al Forno"));
    }

    @Test
    void updateRecipeGibt404WennDasRezeptNichtExistiert() throws Exception {
        UUID unbekannt = UUID.randomUUID();
        when(recipeService.updateRecipe(org.mockito.ArgumentMatchers.eq(unbekannt), org.mockito.ArgumentMatchers.any(Recipe.class)))
                .thenReturn(null);

        mockMvc.perform(put("/api/recipes/{recipeId}", unbekannt)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(lasagne)))
                .andExpect(status().isNotFound());
    }
}
