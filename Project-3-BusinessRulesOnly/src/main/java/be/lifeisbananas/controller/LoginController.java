package be.lifeisbananas.controller;

import be.lifeisbananas.config.SessionKeys;
import be.lifeisbananas.domain.BioEngineer;
import be.lifeisbananas.service.BioEngineerService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

/**
 * Aan- en afmelden van de bio-ingenieur.
 */
@Controller
@RequiredArgsConstructor
public class LoginController {

	private static final String VIEW_LOGIN = "login";

	private final BioEngineerService bioEngineerService;

	@GetMapping("/login.html")
	public String loginFormulier() {
		return VIEW_LOGIN;
	}

	@PostMapping("/login.html")
	public String aanmelden(@RequestParam("email") String email,
							@RequestParam("password") String password,
							HttpSession session,
							ModelMap model) {

		Optional<BioEngineer> bioEngineer = bioEngineerService.authenticate(email, password);
		if (bioEngineer.isEmpty()) {
			model.addAttribute("fout", "Onbekend e-mailadres of verkeerd wachtwoord");
			model.addAttribute("email", email);
			return VIEW_LOGIN;
		}

		session.setAttribute(SessionKeys.BIO_ENGINEER_ID, bioEngineer.get().getId());
		session.setAttribute(SessionKeys.BIO_ENGINEER_NAME, bioEngineer.get().getName());
		return "redirect:/recepten.html";
	}

	@GetMapping("/logout.html")
	public String afmelden(HttpSession session) {
		session.invalidate();
		return "redirect:/login.html";
	}
}
