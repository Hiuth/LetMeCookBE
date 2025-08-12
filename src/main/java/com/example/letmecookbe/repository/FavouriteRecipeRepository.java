package com.example.letmecookbe.repository;

import com.example.letmecookbe.entity.FavouriteRecipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FavouriteRecipeRepository extends JpaRepository<FavouriteRecipe, String> {
    @Query("select c from FavouriteRecipe c where c.account.id =:accountId and c.recipe.id =:recipeId")
    FavouriteRecipe findFavouriteRecipeByRecipeIdAndAccountId(String recipeId, String accountId);

    boolean existsByAccountIdAndRecipeId(String accountId, String recipeId);

    List<FavouriteRecipe> findFavouriteRecipeByAccountId(String accountId);


    void deleteByRecipeIdAndAccountId(String recipeId, String accountId);

    void deleteByRecipeId(String recipeId);

}
