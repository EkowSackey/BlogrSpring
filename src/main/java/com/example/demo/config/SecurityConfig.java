package com.example.demo.config;

import com.example.demo.filter.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final AuthenticationProvider authenticationProvider;
    private final JwtAuthFilter jwtAuthFilter;

    private static final String[] PUBLIC_SWAGGER_PATHS = {
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v3/api-docs/**",
            "/swagger-resources/**",
            "/webjars/**"
    };

    private static final String[] PUBLIC_GRAPHQL_PATHS = {
            "/graphql/**",
            "/graphiql/**"
    };

    private static final String[] PUBLIC_AUTH_PATHS = {
            "/api/v1/users/auth/register",
            "/api/v1/users/auth/login"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(httpRequest -> {
                    httpRequest.requestMatchers(PUBLIC_SWAGGER_PATHS).permitAll();
                    httpRequest.requestMatchers(PUBLIC_GRAPHQL_PATHS).permitAll();
                    httpRequest.requestMatchers(PUBLIC_AUTH_PATHS).permitAll();
                    
                    // User Management
                    httpRequest.requestMatchers("/api/v1/users/**").hasAnyAuthority("ADMIN");
                    
                    // Analytics
                    httpRequest.requestMatchers("/api/v1/analytics/**").hasAnyAuthority("ADMIN", "AUTHOR");

                    // Post Management
                    httpRequest.requestMatchers(HttpMethod.POST, "/api/v1/posts/**").hasAnyAuthority("ADMIN", "AUTHOR");
                    httpRequest.requestMatchers(HttpMethod.PUT, "/api/v1/posts/**").hasAnyAuthority("ADMIN", "AUTHOR");
                    httpRequest.requestMatchers(HttpMethod.DELETE, "/api/v1/posts/**").hasAnyAuthority("ADMIN", "AUTHOR");
                    httpRequest.requestMatchers(HttpMethod.GET, "/api/v1/posts/**").hasAnyAuthority("ADMIN", "AUTHOR", "READER");

                    // Comments
                    httpRequest.requestMatchers("/api/v1/comments/**").hasAnyAuthority("ADMIN", "AUTHOR", "READER");

                    httpRequest.anyRequest().authenticated();
                })
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Restrict origins to specific trusted domains in production
        configuration.setAllowedOrigins(List.of("http://localhost:3000", "http://localhost:4200"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Auth-Token"));
        configuration.setExposedHeaders(List.of("X-Auth-Token"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
