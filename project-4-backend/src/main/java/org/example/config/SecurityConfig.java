
package org.example.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http)
            throws Exception {

        http
                .cors(cors -> {})

                .authorizeHttpRequests(auth -> auth

                        // Publieke loginpagina
                        .requestMatchers("/login").permitAll()

                        // Ingelogde gebruikers mogen recepten en stock bekijken
                        .requestMatchers(
                                HttpMethod.GET,
                                "/recipes/**",
                                "/stocks/**"
                        ).authenticated()

                        // Alleen ADMIN mag recepten en stock wijzigen
                        .requestMatchers(
                                "/recipes/**",
                                "/stocks/**"
                        ).hasRole("ADMIN")

                        // Informatie over ingelogde gebruiker
                        .requestMatchers("/auth/me").authenticated()

                        // Alle overige requests vereisen authenticatie
                        .anyRequest().authenticated()
                )

                // Standaard Spring Security-loginpagina
                // Standaard Spring Security-loginpagina
                .formLogin(form -> form
                        .defaultSuccessUrl("http://localhost:8889/", true)
                        .permitAll()
                )

                // Logout
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .permitAll()
                );

        return http.build();
    }

    @Bean
    public UrlBasedCorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOrigins(
                List.of("http://localhost:8889")
        );

        config.setAllowedMethods(
                List.of("GET", "POST", "PUT", "DELETE", "OPTIONS")
        );

        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", config);

        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories
                .createDelegatingPasswordEncoder();
    }
}
