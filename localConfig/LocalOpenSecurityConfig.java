package tn.spring.stationsync.Configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;


@Profile("local")
@Configuration
public class LocalOpenSecurityConfig {

    @Bean
    SecurityFilterChain relaxedSecurity(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable());

        http.authorizeHttpRequests(auth -> auth
                // Do NOT include the context path (/SS) here.
                .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers("/auth/**").permitAll()
                .requestMatchers("/api/bank-statement").permitAll()
                .requestMatchers("/Shell/**").permitAll()
                .requestMatchers("/Banque/**").permitAll()

                // Open all notifications endpoints
                .requestMatchers("/notifications/**").permitAll()

                .anyRequest().authenticated()
        );

        http.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }
}



