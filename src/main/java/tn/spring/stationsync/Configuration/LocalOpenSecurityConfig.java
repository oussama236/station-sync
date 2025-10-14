package tn.spring.stationsync.Configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;


@Profile("local")                 // <— active ONLY in 'local' profile
@Configuration
public class LocalOpenSecurityConfig {

    @Bean
    SecurityFilterChain relaxedSecurity(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable());

        http.authorizeHttpRequests(auth -> auth
                // matchers do NOT include the context path (/SS)
                .requestMatchers("/auth/**").permitAll()
                .requestMatchers("/api/bank-statement").permitAll() // if your controller uses @RequestMapping("/api")
                .anyRequest().authenticated()
        );

        http.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        // No JWT filter bean added here on purpose (or you can still add it if you want)
        return http.build();
    }
}
