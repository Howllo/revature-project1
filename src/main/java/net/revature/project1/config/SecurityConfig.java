package net.revature.project1.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
public class SecurityConfig {
    @Value("${app.cors.allowed-origins}")
    private String[] allowedOrigins;

    @Value("${argon2.saltLength}")
    private int saltLength;

    @Value("${argon2.hashLength}")
    private int hashLength;

    @Value("${argon2.parallelism}")
    private int parallelism;

    @Value("${argon2.memory}")
    private int memory;

    @Value("${argon2.iterations}")
    private int iterations;

    /**
     * Checks if the Argon2 configuration is correct and won't cause issues when encoding.
     */
    @PostConstruct
    public void validateConfig(){
        if(memory <= 0 || iterations <= 0 || saltLength <= 0 || hashLength <= 0 || parallelism <= 0){
            throw new IllegalArgumentException("All values must be a positive value.");
        }
    }

    /**
     * Used to encode the user's password.
     * @return Returns a new Argon2 password encoders with all the preset.
     */
    @Bean
    public PasswordEncoder passwordEncoder(){
        return new Argon2PasswordEncoder(saltLength, hashLength, parallelism, memory, iterations);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers("/image/**","/files/**", "/video/**","/css/**", "/js/**").permitAll()
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .maximumSessions(1)
                        .expiredUrl("/api/v1/auth/login")
                )
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(allowedOrigins));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}