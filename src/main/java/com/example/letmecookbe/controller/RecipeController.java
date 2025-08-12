package com.example.letmecookbe.controller;

import com.example.letmecookbe.dto.request.RecipeCreationRequest;
import com.example.letmecookbe.dto.request.RecipeUpdateRequest;
import com.example.letmecookbe.dto.response.ApiResponse;
import com.example.letmecookbe.dto.response.RecipeResponse;
import com.example.letmecookbe.service.RecipeService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/recipe")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RecipeController {
    RecipeService recipeService;

    @PostMapping(value="/create/{subCategoryId}",consumes =  MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<RecipeResponse> createRecipe(
            @PathVariable String subCategoryId,
            @RequestPart("title") String title,
            @RequestPart("description") String description,
            @RequestPart("difficulty") String difficulty,
            @RequestPart("cookingTime") String cookingTime,
            @RequestPart("file") MultipartFile file){
        RecipeCreationRequest request = RecipeCreationRequest.builder()
                .title(title)
                .description(description)
                .difficulty(difficulty)
                .cookingTime(cookingTime)
                .build();
        ApiResponse<RecipeResponse> response = new ApiResponse<>();
        response.setMessage("Create Recipe: "+ request.getTitle());
        response.setResult(recipeService.createRecipe(subCategoryId,request,file));
        return response;
    }

    @PutMapping(value = "/update/{id}",consumes =  MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<RecipeResponse> updateRecipe(
            @PathVariable String id,
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("difficulty") String difficulty,
            @RequestParam("cookingTime") String cookingTime,
            @RequestParam("subCategoryId") String subCategoryId,
            @RequestPart(value = "file",required = false) MultipartFile file){
        RecipeUpdateRequest request = RecipeUpdateRequest.builder()
                .title(title)
                .description(description)
                .difficulty(difficulty)
                .cookingTime(cookingTime)
                .subCategoryId(subCategoryId)
                .build();
        ApiResponse<RecipeResponse> response = new ApiResponse<>();
        response.setMessage("Update Recipe: "+ request.getTitle());
        response.setResult(recipeService.updateRecipe(id, request, file));
        return response;
    }

    @GetMapping("/getAll")
    public ApiResponse<Page<RecipeResponse>> getAllRecipe(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        ApiResponse<Page<RecipeResponse>> response = new ApiResponse<>();
        Pageable pageable = PageRequest.of(page, size);
        response.setMessage("Get all Recipes with pagination");
        response.setResult(recipeService.getAllRecipe(pageable));
        return response;
    }

    @GetMapping("/getBySubCategory/{id}")
    public ApiResponse<Page<RecipeResponse>> getRecipeBySubCategory(
            @PathVariable String id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size){
        ApiResponse<Page<RecipeResponse>> response = new ApiResponse<>();
        Pageable pageable = PageRequest.of(page, size);
        response.setMessage("Get all Recipe by Sub Category: "+ id);
        response.setResult(recipeService.getRecipeBySubCategoryId(id, pageable));
        return response;
    }


    @GetMapping("/findByKeyWord/{keyword}")
    public ApiResponse<List<RecipeResponse>> findRecipeByKeyWord(@PathVariable String keyword){
        ApiResponse<List<RecipeResponse>> response = new ApiResponse<>();
        response.setMessage("find all Recipe by keyword: "+ keyword);
        response.setResult(recipeService.findRecipeByKeyword(keyword));
        return response;
    }

    @GetMapping("/getRecipeByAccount")
    public ApiResponse<List<RecipeResponse>> getRecipeByAccount(){
        ApiResponse<List<RecipeResponse>> response = new ApiResponse<>();
        response.setMessage("Get all Recipe by Account: ");
        response.setResult(recipeService.getRecipeByAccountId());
        return response;
    }

    @GetMapping("/getRecipeByAccountUseForAdmin/{id}")
    public ApiResponse<Integer> getRecipeByAccountUseForAdmin(@PathVariable String id){
        ApiResponse<Integer> response = new ApiResponse<>();
        response.setMessage("Get all Recipe by Account: ");
        response.setResult(recipeService.getRecipeByAccountIdUseForAdmin(id));
        return response;
    }

    @GetMapping("/getRecipeById/{id}")
    public ApiResponse<RecipeResponse> getRecipeById(@PathVariable String id){
        ApiResponse<RecipeResponse> response = new ApiResponse<>();
        response.setMessage("Get Recipe by ID");
        response.setResult(recipeService.getRecipeById(id));
        return response;
    }

    @PostMapping("/changeStatus/{id}")
    public ApiResponse<RecipeResponse> changeStatus(@PathVariable String id){
        ApiResponse<RecipeResponse> response = new ApiResponse<>();
        response.setMessage("Change Status: "+ id);
        response.setResult(recipeService.changeStatusToApprove(id));
        return response;
    }


    @PostMapping("/changeStatusToNotApproved/{id}")
    public ApiResponse<RecipeResponse> changeStatusToNotApproved(@PathVariable String id){
        ApiResponse<RecipeResponse> response = new ApiResponse<>();
        response.setMessage("Change Status To Not Approved");
        response.setResult(recipeService.changeStatusToNotApproved(id));
        return response;
    }

    @PostMapping("/changeStatusToPending/{id}")
    public ApiResponse<RecipeResponse> changeStatusToPending(@PathVariable String id){
        ApiResponse<RecipeResponse> response = new ApiResponse<>();
        response.setMessage("Change Status To Pending");
        response.setResult(recipeService.changeStatusToPending(id));
        return response;
    }



    @DeleteMapping("/deleteRecipe/{id}")
    public ApiResponse<String> deleteRecipe(@PathVariable String id){
        ApiResponse<String> response = new ApiResponse<>();
        response.setResult(recipeService.deleteRecipe(id));
        return response;
    }

    @GetMapping("/getTop5Recipes")
    public ApiResponse<List<RecipeResponse>> getTop5Recipe(){
        ApiResponse<List<RecipeResponse>> response= new ApiResponse<>();
        response.setMessage("Top 5 recipe: ");
        response.setResult(recipeService.getTop5RecipesByTotalLikes());
        return response;
    }

    @GetMapping("/getRecipeBySubAndLike/{subCategoryId}")
    public ApiResponse<List<RecipeResponse>> getRecipeBySubAndLike(@PathVariable String subCategoryId){
         ApiResponse<List<RecipeResponse>> response = new ApiResponse<>();
         response.setMessage("Recipe By SubCategory AND TotalLike");
         response.setResult(recipeService.GetRecipesBySubCategoryIdTodayOrderByLikes(subCategoryId));
         return response;
    }

    @GetMapping("/getTotalRecipe")
    public ApiResponse<Integer> getTotalRecipe(){
        ApiResponse<Integer> response = new ApiResponse<>();
        response.setMessage("Get Total Recipe");
        response.setResult(recipeService.countRecipeBySubCategoryId());
        return response;
    }

    @GetMapping("/countRecipeBySub/{subCategoryId}")
    public ApiResponse<Integer> getRecipeCountBySubCategory(@PathVariable String subCategoryId) {
        ApiResponse<Integer> response = new ApiResponse<>();
        response.setMessage("Get Recipe Count for SubCategory: " + subCategoryId);
        response.setResult(recipeService.countRecipeBySubCategoryId(subCategoryId));
        return response;
    }

    @GetMapping("/countApprovedRecipes")
    public ApiResponse<Integer> getApprovedRecipeCount() {
        ApiResponse<Integer> response = new ApiResponse<>();
        response.setMessage("Get Approve Recipe Count");
        response.setResult(recipeService.countApprovedRecipes());
        return response;
    }

    @GetMapping("/countRecipeByMainCate/{mainCategory}")
    public ApiResponse<Integer> getRecipesCountByMainCategory(@PathVariable String mainCategory) {
        ApiResponse<Integer> response = new ApiResponse<>();
        response.setMessage("Get Recipe Count for MainCategory: " + mainCategory);
        response.setResult(recipeService.countRecipesByMainCategory(mainCategory));
        return response;
    }

    @GetMapping("/countAllRecipes")
    public ApiResponse<Integer> getAllRecipeCount() {
        ApiResponse<Integer> response = new ApiResponse<>();
        response.setMessage("Get All Recipe Count");
        response.setResult(recipeService.countAllRecipes());
        return response;
    }

    @GetMapping("/countPendingRecipes")
    public ApiResponse<Integer> getPendingRecipeCount() {
        ApiResponse<Integer> response = new ApiResponse<>();
        response.setMessage("Get Pending Recipe Count");
        response.setResult(recipeService.countPendingRecipes());
        return response;
    }

    @GetMapping("/countNotApprovedRecipes")
    public ApiResponse<Integer> getNotApprovedRecipeCount() {
        ApiResponse<Integer> response = new ApiResponse<>();
        response.setMessage("Get Not Approved Recipe Count");
        response.setResult(recipeService.countNotApprovedRecipes());
        return response;
    }

    @GetMapping("/trendingRecipe")
    public ApiResponse<List<RecipeResponse>> getTrendingRecipe(){
        ApiResponse<List<RecipeResponse>> response = new ApiResponse<>();
        response.setMessage("Get Trending Recipe");
        response.setResult(recipeService.getTrendingRecipes());
        return response;
    }

    @GetMapping("/newRecipeInMonth")
    public ApiResponse<List<RecipeResponse>> getNewRecipeInMonth(){
        ApiResponse<List<RecipeResponse>> response = new ApiResponse<>();
        response.setMessage("Get New Recipe In Month");
        response.setResult(recipeService.getNewRecipeInMonth());
        return response;
    }

    @GetMapping("/getFavouriteRecipeByAccount")
    public ApiResponse<List<RecipeResponse>> getFavouriteRecipeByAccount(){
        ApiResponse<List<RecipeResponse>> response = new ApiResponse<>();
        response.setMessage("Get Favourite Recipe By Account");
        response.setResult(recipeService.getFavouriteRecipeByAccountId());
        return response;
    }
}
