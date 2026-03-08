package com.techone.common.advice;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.techone.domain.product.entity.Category;
import com.techone.domain.user.entity.Favourite;
import com.techone.domain.user.entity.Account;
import com.techone.dto.BrandCountDto;
import com.techone.domain.product.repository.CategoryRepository;
import com.techone.domain.product.repository.BrandRepository;
import com.techone.domain.order.repository.CartRepository;
import com.techone.domain.order.repository.CartItemRepository;
import com.techone.domain.user.repository.FavouriteRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.ui.Model;

@ControllerAdvice
public class GlobalDataAdvice {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private BrandRepository brandRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private FavouriteRepository favouriteRepository;

    @Autowired
    private HttpSession session;

    @ModelAttribute("parentCategories")
    public List<Category> getParentCategories() {
        return categoryRepository.findByTypeAndParentIsNullAndStatus(true, true);
    }

    @ModelAttribute("activeBrands")
    public List<BrandCountDto> getActiveBrands() {
        return brandRepository.findActiveBrandsWithProductCount();
    }

    @ModelAttribute
    public void addGlobalCounts(Model model) {
        Account user = (Account) session.getAttribute("user");
        if (user != null) {
            // Cart count
            cartRepository.findByAccount(user).ifPresent(cart -> {
                int cartCount = cartItemRepository.findActiveItemsByCart(cart).size();
                session.setAttribute("cartCount", cartCount);
                model.addAttribute("cartCount", cartCount);
            });

            // Favorite count & IDs
            List<Favourite> favourites = favouriteRepository.findByAccount(user);
            int favCount = favourites.size();
            List<Integer> favoritedVariantIds = favourites.stream()
                    .filter(f -> f.getVariant() != null)
                    .map(f -> f.getVariant().getId())
                    .toList();

            session.setAttribute("favoriteCount", favCount);
            model.addAttribute("favoriteCount", favCount);
            model.addAttribute("favoritedVariantIds", favoritedVariantIds);
        }
    }
}
