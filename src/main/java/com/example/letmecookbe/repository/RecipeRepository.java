package com.example.letmecookbe.repository;

import com.example.letmecookbe.entity.Recipe;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RecipeRepository extends JpaRepository<Recipe, String> {
    @Query("select c from Recipe c where c.subCategory.id = :subCategoryId")
    List<Recipe> findRecipeBySubCategoryId(String subCategoryId);

    @Query("select c from Recipe c where c.title LIKE %:KeyWord%")
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

    // Lấy món ăn trending (được yêu thích nhiều trong thời gian gần đây)
    @Query("""
        SELECT f.recipe, COUNT(f.recipe) as favoriteCount 
        FROM FavouriteRecipe f 
        WHERE f.recipe.createAt >= :fromDate 
        GROUP BY f.recipe 
        ORDER BY favoriteCount DESC, f.recipe.totalLikes DESC LIMIT 6
    """)
    List<Recipe> findTrendingFavoriteRecipes(LocalDateTime fromDate);


    // Lấy công thức mới nhất trong tháng này
    @Query("SELECT r FROM Recipe r WHERE MONTH(r.createAt) = MONTH(CURRENT_DATE) AND YEAR(r.createAt) = YEAR(CURRENT_DATE) AND r.status = 'APPROVED' ORDER BY r.createAt DESC LIMIT 6")
    List<Recipe> findThisMonthRecipes();

    // Lấy công thức mới nhất với fallback (nếu tháng này không có thì lấy tháng trước)
    @Query("SELECT r FROM Recipe r WHERE r.createAt >= :fallbackDate AND r.status = 'APPROVED' ORDER BY r.createAt DESC LIMIT 6")
    List<Recipe> findRecentRecipesWithFallback( LocalDateTime fallbackDat);

    // Lấy công thức mới nhất trong N tháng gần đây
    @Query("SELECT r FROM Recipe r WHERE r.createAt >= :monthsAgo AND r.status = 'APPROVED' ORDER BY r.createAt DESC LIMIT 6")
    List<Recipe> findRecipesInLastMonths(LocalDateTime monthsAgo);

    // Kiểm tra có công thức nào trong tháng này không
    @Query("SELECT COUNT(r) FROM Recipe r WHERE MONTH(r.createAt) = MONTH(CURRENT_DATE) AND YEAR(r.createAt) = YEAR(CURRENT_DATE) AND r.status = 'APPROVED'")
    long countThisMonthRecipes();

    @Query("SELECT f.recipe FROM FavouriteRecipe f WHERE f.account.id = :accountId")
    List<Recipe> findFavouriteRecipesByAccountId(String accountId);

}
