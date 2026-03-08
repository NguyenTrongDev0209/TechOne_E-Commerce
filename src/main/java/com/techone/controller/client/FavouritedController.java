package com.techone.controller.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import com.techone.domain.user.repository.FavouriteRepository;
import com.techone.domain.user.entity.Account;
import com.techone.domain.user.entity.Favourite;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import jakarta.servlet.http.HttpSession;

@Controller
public class FavouritedController {

	@Autowired
	private FavouriteRepository favouriteRepository;

	@GetMapping("/account/favourited")
	public String showFavourited(
			@RequestParam(name = "page", defaultValue = "0") int page,
			HttpSession session,
			Model model) {

		Account user = (Account) session.getAttribute("user");

		if (user == null) {
			return "redirect:/login";
		}

		Page<Favourite> favouritePage = favouriteRepository
				.findActiveFavouritesByAccount(
						user, PageRequest.of(page, 4));

		model.addAttribute("favouritePage", favouritePage);
		model.addAttribute("currentPage", page);
		return "views/client/favourited";
	}
}
