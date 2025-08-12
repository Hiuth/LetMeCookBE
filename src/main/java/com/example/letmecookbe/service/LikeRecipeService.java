package com.example.letmecookbe.service;
import com.example.letmecookbe.entity.UserInfo;
import lombok.extern.slf4j.Slf4j;
import com.example.letmecookbe.service.AuthService;
import com.example.letmecookbe.mapper.LikeRecipeMapper;
import com.example.letmecookbe.repository.UserInfoRepository;
import com.example.letmecookbe.dto.request.LikeRecipeRequest;
import com.example.letmecookbe.dto.response.LikeRecipeResponse;
import com.example.letmecookbe.dto.response.RecipeResponse;
import com.example.letmecookbe.entity.Account;
import com.example.letmecookbe.entity.LikeRecipe;
import com.example.letmecookbe.entity.Recipe;
import com.example.letmecookbe.exception.AppException;
import com.example.letmecookbe.exception.ErrorCode;
import com.example.letmecookbe.mapper.LikeRecipeMapper;
import com.example.letmecookbe.mapper.RecipeMapper;
import com.example.letmecookbe.repository.AccountRepository;
import com.example.letmecookbe.repository.LikeRecipeRepository;
import com.example.letmecookbe.repository.RecipeRepository;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)

public class LikeRecipeService {
    AccountService accountService;
    SimpMessagingTemplate messagingTemplate;
    LikeRecipeRepository likeRecipeRepository;
    UserInfoRepository userInfoRepository;
    LikeRecipeMapper likeRecipeMapper;
    RecipeRepository recipeRepository;
    AccountRepository accountRepository;

    private String getAccountIdFromContext() {
        var context = SecurityContextHolder.getContext();
        String email = context.getAuthentication().getName();
        Account account = accountRepository.findAccountByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));
        return account.getId();
    }

    @PreAuthorize("hasAuthority('LIKE')")
    public LikeRecipeResponse createLike(String id, LikeRecipeRequest request) {
        Recipe recipe = recipeRepository.findById(id).orElseThrow(
                () -> new AppException(ErrorCode.RECIPE_NOT_FOUND)
        );
        Account account = accountRepository.findById(getAccountIdFromContext()).orElseThrow(
                ()->new AppException(ErrorCode.ACCOUNT_NOT_FOUND)
        );
        if(likeRecipeRepository.existsByRecipeIdAndAccountId(getAccountIdFromContext(), id)){
            throw new AppException(ErrorCode.LIKE_RECIPE_EXISTED);
        }
        recipe.setTotalLikes(recipe.getTotalLikes() + 1);
        recipeRepository.save(recipe);
        sendLikeUpdateToSocket(recipe.getId(), recipe.getTotalLikes());
        LikeRecipe likeRecipe = likeRecipeMapper.toLikeRecipe(request);
        likeRecipe.setAccount(account);
        likeRecipe.setRecipe(recipe);
        likeRecipeRepository.save(likeRecipe);
        return likeRecipeMapper.toLikeRecipeResponse(likeRecipe);
    }

    @PreAuthorize("hasAuthority('GET_ALL_ACCOUNT_LIKE')")
    public List<LikeRecipeResponse> getAllLikeRecipeByAccountId(String recipeId){
        List<LikeRecipe> likeRecipes = likeRecipeRepository.getAllLikeRecipeByAccountIdAndRecipeId(getAccountIdFromContext(),recipeId);
        return likeRecipes.stream()
                .map(likeRecipeMapper::toLikeRecipeResponse)
                .toList();
    }
    public Account getCurrentAccount() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return accountRepository.findAccountByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));
    }

    private void sendLikeUpdateToSocket(String recipeId, int totalLikes) {
        Account currentAccount = getCurrentAccount();
        UserInfo userInfo = userInfoRepository.findByAccountId(currentAccount.getId()).orElse(null);

        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "LIKE"); // thêm type
        payload.put("recipeId", recipeId);
        payload.put("totalLikes", totalLikes);
        payload.put("senderId", currentAccount.getId());
        payload.put("senderUsername", currentAccount.getUsername());
        payload.put("senderAvatar", userInfo != null ? userInfo.getAvatar() : "");

        log.info("🔔 Sending like update to /topic/recipe-likes | payload: {}", payload);

        messagingTemplate.convertAndSend("/topic/recipe-likes", payload);
    }




    @Transactional
    @PreAuthorize("hasAuthority('DISLIKE')")
    public String disLike(String id) {
        Recipe recipe = recipeRepository.findById(id).orElseThrow(
                () -> new AppException(ErrorCode.RECIPE_NOT_FOUND)
        );
        recipe.setTotalLikes(recipe.getTotalLikes() - 1);
        recipeRepository.save(recipe);
        sendLikeUpdateToSocket(recipe.getId(), recipe.getTotalLikes());
        likeRecipeRepository.deleteByRecipeIdAndAccountId(id, getAccountIdFromContext());
        if(likeRecipeRepository.existsByRecipeIdAndAccountId(id, getAccountIdFromContext())){
            return "delete dislike recipe failed";
        }
        return "delete dislike recipe successfully";

    }
}
