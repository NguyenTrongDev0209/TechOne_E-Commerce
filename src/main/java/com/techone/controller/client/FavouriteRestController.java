package com.techone.controller.client;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.techone.domain.user.entity.Account;
import com.techone.domain.user.entity.Favourite;
import com.techone.domain.product.entity.Variant;
import com.techone.domain.user.repository.FavouriteRepository;
import com.techone.domain.product.repository.VariantRepository;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping({ "/api/favourite", "/api/favorites" })
public class FavouriteRestController {

    @Autowired
    private FavouriteRepository favouriteRepository;

    @Autowired
    private VariantRepository variantRepository;

    @PostMapping("/toggle")
    public ResponseEntity<?> toggleFavourite(@RequestBody Map<String, Object> payload, HttpSession session) {
        Account user = (Account) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(401).body("Vui lòng đăng nhập để thực hiện chức năng này");
        }

        Object variantIdObj = payload.get("variantId");
        Integer variantId = null;
        if (variantIdObj instanceof Number) {
            variantId = ((Number) variantIdObj).intValue();
        } else if (variantIdObj instanceof String) {
            try {
                variantId = Integer.parseInt((String) variantIdObj);
            } catch (NumberFormatException e) {
                // Keep null
            }
        }

        if (variantId == null) {
            return ResponseEntity.badRequest().body("Dữ liệu không hợp lệ");
        }

        Optional<Variant> variantOpt = variantRepository.findById(variantId);
        if (!variantOpt.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        Variant variant = variantOpt.get();
        Optional<Favourite> favOpt = favouriteRepository.findByAccountAndVariant(user, variant);

        boolean isFavourite;
        if (favOpt.isPresent()) {
            favouriteRepository.delete(favOpt.get());
            isFavourite = false;
        } else {
            Favourite fav = new Favourite();
            fav.setAccount(user);
            fav.setVariant(variant);
            favouriteRepository.save(fav);
            isFavourite = true;
        }

        int favoriteCount = favouriteRepository.findByAccount(user).size();
        session.setAttribute("favoriteCount", favoriteCount);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("favorited", isFavourite);
        response.put("favoriteCount", favoriteCount);
        response.put("message", isFavourite ? "Đã thêm vào danh sách yêu thích" : "Đã xóa khỏi danh sách yêu thích");

        return ResponseEntity.ok(response);
    }
}



