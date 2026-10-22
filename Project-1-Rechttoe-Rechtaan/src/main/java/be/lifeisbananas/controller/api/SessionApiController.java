package be.lifeisbananas.controller.api;

import be.lifeisbananas.config.SessionKeys;
import be.lifeisbananas.service.BioEngineerService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/session")
@RequiredArgsConstructor
public class SessionApiController {
    private final BioEngineerService engineers;

    @PostMapping
    public UserView login(@Valid @RequestBody Login request, HttpServletRequest http) {
        var engineer = engineers.authenticate(request.email(), request.password())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "E-mailadres of wachtwoord is onjuist"));
        var previous = http.getSession(false);
        if (previous != null) previous.invalidate();
        var session = http.getSession(true);
        session.setAttribute(SessionKeys.BIO_ENGINEER_ID, engineer.getId());
        session.setAttribute(SessionKeys.BIO_ENGINEER_NAME, engineer.getName());
        return new UserView(engineer.getId(), engineer.getName());
    }

    @GetMapping
    public UserView current(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute(SessionKeys.BIO_ENGINEER_ID) == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Meld je eerst aan");
        }
        return new UserView((Long) session.getAttribute(SessionKeys.BIO_ENGINEER_ID),
                (String) session.getAttribute(SessionKeys.BIO_ENGINEER_NAME));
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(HttpServletRequest request) {
        var session = request.getSession(false);
        if (session != null) session.invalidate();
    }

    public record Login(@NotBlank @Email String email, @NotBlank String password) {}
    public record UserView(Long id, String name) {}
}
