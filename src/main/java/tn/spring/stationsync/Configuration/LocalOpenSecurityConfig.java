package tn.spring.stationsync.Configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.time.Duration;
import java.util.List;


@Profile("local")
@Configuration
public class LocalOpenSecurityConfig {

    @Bean
    SecurityFilterChain relaxedSecurity(HttpSecurity http) throws Exception {
        http
                .cors(cors -> {})
                .csrf(csrf -> csrf.disable());

        http.authorizeHttpRequests(auth -> auth
                .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers("/auth/**").permitAll()
                .requestMatchers("/api/bank-statement").permitAll()
                .requestMatchers("/Shell/**").permitAll()
                .requestMatchers("/Banque/**").permitAll()
                .requestMatchers("/Prelevement/**").permitAll()
                .requestMatchers("/api/ai/**").permitAll()
                .requestMatchers("/addPrelevement").permitAll()
                .requestMatchers("/api/bank-statement/pdf").permitAll()
                .requestMatchers("/notifications/**").permitAll()
                .requestMatchers("/register", "/login", "/SS/register", "/SS/login").permitAll()
                .anyRequest().authenticated()
        );

        http.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOriginPatterns(List.of(
                "http://localhost:4200",
                "http://127.0.0.1:4200",
                "http://localhost:8089",
                "http://192.168.74.128:8089",
                "https://station-sync-front.onrender.com"
        ));

        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));

        // 👇 ici la correction
        config.setAllowedHeaders(List.of(
                "Authorization",
                "Content-Type",
                "X-Requested-With",
                "ngrok-skip-browser-warning"   // 👈 c’était ça qui manquait
        ));

        config.setExposedHeaders(List.of("Authorization"));
        config.setAllowCredentials(true);
        config.setMaxAge(Duration.ofHours(1));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}



