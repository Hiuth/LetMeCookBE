package com.example.letmecookbe.repository;


import com.example.letmecookbe.entity.LikeRecipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LikeRecipeRepository extends JpaRepository<LikeRecipe, String> {
    boolean existsByRecipeIdAndAccountId(String recipeId, String accountId);

    void deleteByRecipeIdAndAccountId(String recipeId, String accountId);

    List<LikeRecipe> getAllLikeRecipeByAccountIdAndRecipeId(String accountId, String recipeId);

    void deleteByRecipeId(String recipeId);

}
