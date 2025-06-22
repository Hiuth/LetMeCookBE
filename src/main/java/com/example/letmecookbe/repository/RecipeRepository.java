package com.example.letmecookbe.repository;

import com.example.letmecookbe.entity.Recipe;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecipeRepository extends JpaRepository<Recipe, String> {
    @Query("select c from Recipe c where c.subCategory.id = :subCategoryId")
    List<Recipe> findRecipeBySubCategoryId(String subCategoryId);

    @Query("select c from Recipe c where c.title LIKE %:KeyWord% or c.subCategory.subCategoryName LIKE %:KeyWord%")
    List<Recipe> findRecipeByKeyword(String KeyWord);

    @Query("select c from Recipe c where c.account.id = :accountId")
    List<Recipe> findRecipeByAccountId(String accountId);

    @Query("SELECT c FROM Recipe c  WHERE c.status = 'APPROVED' ORDER BY c.totalLikes DESC  LIMIT 5")
    List<Recipe> findTop5RecipesByTotalLikes();

    @Query("SELECT c FROM Recipe c " +
            "WHERE c.subCategory.id = :subCategoryId AND c.status ='APPRROVED' " +
            "AND DATE(c.createAt) = CURRENT_DATE " +
            "ORDER BY c.totalLikes DESC LIMIT 6")
    List<Recipe> findRecipesBySubCategoryIdTodayOrderByLikes(String subCategoryId);

    @Query("SELECT COUNT(c) FROM Recipe c WHERE c.account.id = :accountId")
    int countRecipesByAccountId(String accountId);

    @Query("SELECT COUNT(c) FROM Recipe c WHERE c.subCategory.id = :subCategoryId")
    int countRecipesBySubCategoryId(String subCategoryId);

    @Query("SELECT COUNT(c) FROM Recipe c WHERE c.status = 'APPROVED'")
    int countApprovedRecipes();

    @Query("SELECT COUNT(c) FROM Recipe c WHERE c.subCategory.mainCategory.id = :mainCategoryId AND c.status ='APPROVED' ")
    int countRecipesByMainCategoryId(String mainCategoryId);

    @Query("SELECT COUNT(c) FROM Recipe c ")
    int countAllRecipes();

    @Query("SELECT COUNT(c) FROM Recipe c WHERE c.status = 'PENDING'")
    int countPendingRecipes();

    @Query("SELECT COUNT(c) FROM Recipe c WHERE c.status = 'NOT_APPROVED'")
    int countNotApprovedRecipes();

    @Query("SELECT r FROM Recipe r WHERE r.subCategory.mainCategory.id = :mainCategoryId")
    List<Recipe> findAllByMainCategoryId(String mainCategoryId);

    @Query("SELECT r FROM Recipe r WHERE r.subCategory.id = :subCategoryId AND r.status = 'APPROVED'")
    Page<Recipe> findRecipeBySubCategoryIdWithPagination(String subCategoryId, Pageable pageable);


}
