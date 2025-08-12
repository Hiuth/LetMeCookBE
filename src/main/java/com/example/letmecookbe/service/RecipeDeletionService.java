package com.example.letmecookbe.service;

import com.example.letmecookbe.entity.Recipe;
import com.example.letmecookbe.entity.RecipeIngredients;
import com.example.letmecookbe.entity.RecipeSteps;
import com.example.letmecookbe.entity.Comment;
import com.example.letmecookbe.repository.*;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RecipeDeletionService {
    private final CommentRepository commentRepository;
    private final RecipeIngredientsRepository recipeIngredientsRepository;
    private final RecipeStepsRepository recipeStepsRepository;
    private final RecipeRepository recipeRepository;
    private final LikeRecipeRepository likeRecipeRepository;
    private final FavouriteRecipeRepository favouriteRecipeRepository;
    private final LikeCommentRepository likeCommentRepository;


    @Transactional
    public void deleteRecipesAndRelatedData(List<Recipe> recipes) {
        System.out.println("deleteRecipesAndRelatedData called with " + recipes.size() + " recipes");
        for (Recipe recipe : recipes) {
            // Bước 1: Lấy tất cả comments của recipe
            List<Comment> comments = commentRepository.findByRecipeId(recipe.getId());
            System.out.println("comments: " + comments);
            for (Comment comment : comments) {
                System.out.println("comment " + comment.getId() + " has like, delete it");
                likeCommentRepository.deleteAllByCommentId(comment.getId());
            }


            // Bước 3: Sau đó mới xóa comments
            commentRepository.deleteByRecipeId(recipe.getId());

            // Bước 4: Xóa like recipes
            likeRecipeRepository.deleteByRecipeId(recipe.getId());

            // Bước 5: Xóa favourite recipes
            favouriteRecipeRepository.deleteByRecipeId(recipe.getId());

            // Bước 6: Xóa recipe ingredients
            List<RecipeIngredients> recipeIngredients =
                    recipeIngredientsRepository.findAllByRecipeId(recipe.getId());
            if (!recipeIngredients.isEmpty()) {
                recipeIngredientsRepository.deleteAll(recipeIngredients);
            }

            // Bước 7: Xóa recipe steps
            List<RecipeSteps> recipeSteps =
                    recipeStepsRepository.findRecipeStepsByRecipeId(recipe.getId());
            if (!recipeSteps.isEmpty()) {
                recipeStepsRepository.deleteAll(recipeSteps);
            }
        }

        // Bước 8: Cuối cùng xóa recipes
        if (!recipes.isEmpty()) {
            recipeRepository.deleteAll(recipes);
        }
    }
}
