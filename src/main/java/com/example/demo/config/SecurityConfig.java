package com.example.demo.config;

import com.example.demo.filter.JwtAuthFilter;
import com.example.demo.services.CustomOAuth2UserService;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
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
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final AuthenticationProvider authenticationProvider;
    private final JwtAuthFilter jwtAuthFilter;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

    @Value("${app.cors.allowed-origins}")
    private List<String> allowedOrigins;

    private static final String[] PUBLIC_PATHS = {
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v3/api-docs/**",
            "/swagger-resources/**",
            "/webjars/**",
            "/graphql/**",
            "/graphiql/**",
            "/api/v1/users/auth/register",
            "/api/v1/users/auth/login"
    };

    private static final String[] ADMIN_PATHS = {
            "/api/v1/users/**",
            "/api/v1/analytics/**"
    };

    private static final String[] POST_MODIFICATION_PATHS = {
            "/api/v1/posts/**"
    };

    private static final String[] COMMENT_MODIFICATION_PATHS = {
            "/api/v1/comments/**"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(httpRequest -> {
                    // 1. Explicitly authorize ASYNC dispatches for non-blocking controllers
                    httpRequest.dispatcherTypeMatchers(DispatcherType.ASYNC).permitAll();

                    // 2. Permit all public paths
                    httpRequest.requestMatchers(PUBLIC_PATHS).permitAll();
                    
                    // 3. Explicitly allow logout for ANY authenticated user
                    httpRequest.requestMatchers("/api/v1/users/auth/logout").authenticated();
                    
                    // 4. Admin only paths
                    httpRequest.requestMatchers(ADMIN_PATHS).hasRole("ADMIN");
                    
                    // 5. Post modification (Admin or Author)
                    httpRequest.requestMatchers(HttpMethod.POST, POST_MODIFICATION_PATHS).hasAnyRole("ADMIN", "AUTHOR");
                    httpRequest.requestMatchers(HttpMethod.PUT, POST_MODIFICATION_PATHS).hasAnyRole("ADMIN", "AUTHOR");
                    httpRequest.requestMatchers(HttpMethod.DELETE, POST_MODIFICATION_PATHS).hasAnyRole("ADMIN", "AUTHOR");

                    // 6. Comment creation (Admin, Author, or Reader)
                    httpRequest.requestMatchers(HttpMethod.POST, COMMENT_MODIFICATION_PATHS).hasAnyRole("ADMIN", "AUTHOR", "READER");
                    // Comment deletion (Admin or Author)
                    httpRequest.requestMatchers(HttpMethod.DELETE, COMMENT_MODIFICATION_PATHS).hasAnyRole("ADMIN", "AUTHOR");
                    
                    // 7. All other requests must be authenticated
                    httpRequest.anyRequest().authenticated();
                })
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
                        })
                )
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService)
                        )
                        .successHandler(oAuth2LoginSuccessHandler)
                );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Restrict origins to specific trusted domains in production
        configuration.setAllowedOrigins(allowedOrigins);
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Auth-Token"));
        configuration.setExposedHeaders(List.of("X-Auth-Token"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
