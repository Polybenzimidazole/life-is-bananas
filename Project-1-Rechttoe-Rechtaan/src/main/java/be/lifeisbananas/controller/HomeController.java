package be.lifeisbananas.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * De wortel van de toepassing leidt naar de lijst van recepturen. Wie niet
 * aangemeld is, komt via de LoginInterceptor op het aanmeldscherm terecht.
 */
@Controller
public class HomeController {

	@GetMapping("/")
	public String index() {
		return "redirect:/recepten.html";
	}
}
