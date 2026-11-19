package org.example.controllers;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Set;

@Controller
public class HomeController {

    private static final Set<String> LOOPBACK_HOSTS = Set.of("localhost", "127.0.0.1", "::1", "[::1]");
    private final URI frontendUrl;

    public HomeController(@Value("${app.frontend-url:http://localhost:8889}") URI frontendUrl) {
        this.frontendUrl = frontendUrl;
    }

    @GetMapping("/")
    public ResponseEntity<Void> home(HttpServletRequest request) {
        URI target = frontendUrl;
        // Session cookies belong to a hostname, so keep the browser's loopback alias.
        if (LOOPBACK_HOSTS.contains(frontendUrl.getHost())
                && LOOPBACK_HOSTS.contains(request.getServerName())) {
            target = UriComponentsBuilder.fromUri(frontendUrl)
                    .host(request.getServerName())
                    .build().toUri();
        }
        return ResponseEntity.status(HttpStatus.FOUND).location(target).build();
    }
}
