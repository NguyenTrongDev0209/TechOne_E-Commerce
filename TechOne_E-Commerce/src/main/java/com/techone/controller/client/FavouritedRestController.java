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

import com.techone.model.Account;
import com.techone.model.Favourite;
import com.techone.model.Variant;
import com.techone.repository.FavouriteRepository;
import com.techone.repository.VariantRepository;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/favorites")
public class FavouritedRestController {

    @Autowired
    private FavouriteRepository favouriteRepository;

    @Autowired
    private VariantRepository variantRepository;

    @PostMapping("/toggle")
    public ResponseEntity<?> toggleFavorite(@RequestBody Map<String, Object> payload, HttpSession session) {
        Account user = (Account) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(401).body("Vui lòng đăng nhập để thực hiện thao tác này");
        }

        Integer variantId = (Integer) payload.get("variantId");
        if (variantId == null) {
            return ResponseEntity.badRequest().body("ID không hợp lệ");
        }

        Optional<Variant> variantOpt = variantRepository.findById(variantId);
        if (!variantOpt.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        Variant variant = variantOpt.get();
        Optional<Favourite> favoriteOpt = favouriteRepository.findByAccountAndVariant(user, variant);

        Map<String, Object> response = new HashMap<>();
        if (favoriteOpt.isPresent()) {
            favouriteRepository.delete(favoriteOpt.get());
            response.put("favorited", false);
            response.put("message", "Đã xóa khỏi danh sách yêu thích");
        } else {
            Favourite favorite = new Favourite();
            favorite.setAccount(user);
            favorite.setVariant(variant);
            favouriteRepository.save(favorite);
            response.put("favorited", true);
            response.put("message", "Đã thêm vào danh sách yêu thích");
        }

        int favCount = favouriteRepository.findByAccount(user).size();
        session.setAttribute("favoriteCount", favCount);
        response.put("favoriteCount", favCount);

        return ResponseEntity.ok(response);
    }
}
