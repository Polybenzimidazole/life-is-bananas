
package org.example.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.security.web.csrf.CsrfToken;

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

                        // Login en CSRF-token publiek toegankelijk
                        .requestMatchers(
                                "/login",
                                "/csrf",
                                "/recipes/csrf"
                        ).permitAll()

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

                        .anyRequest().authenticated()
                )

                .formLogin(form -> form
                        .loginProcessingUrl("/login")

                        .successHandler((request, response, authentication) -> {
                            response.setStatus(HttpStatus.OK.value());
                        })

                        .failureHandler((request, response, exception) -> {
                            response.sendError(
                                    HttpStatus.UNAUTHORIZED.value(),
                                    "Invalid username or password"
                            );
                        })

                        .permitAll()
                )

                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessHandler((request, response, authentication) -> {
                            response.setStatus(HttpStatus.OK.value());
                        })
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
