package com.techone.controller.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class FavouritedController {

	@Autowired
	private com.techone.repository.FavouriteRepository favouriteRepository;

	@GetMapping("/account/favourited")
	public String showFavourited(
			@org.springframework.web.bind.annotation.RequestParam(name = "page", defaultValue = "0") int page,
			jakarta.servlet.http.HttpSession session,
			org.springframework.ui.Model model) {
		com.techone.model.Account user = (com.techone.model.Account) session.getAttribute("user");
		if (user == null) {
			return "redirect:/login";
		}

		org.springframework.data.domain.Page<com.techone.model.Favourite> favouritePage = favouriteRepository
				.findActiveFavouritesByAccount(
						user, org.springframework.data.domain.PageRequest.of(page, 4));

		model.addAttribute("favouritePage", favouritePage);
		model.addAttribute("currentPage", page);
		return "views/client/favourited";
	}
}
