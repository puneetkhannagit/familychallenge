// java
package com.example.microservices.config;


import com.example.microservices.filter.CustomJwtAuthFilter;
import com.example.microservices.filter.LoggingFilter;
import com.example.microservices.service.JwtService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.Set;

@Configuration
public class SecurityConfig {

    @Bean
    public CustomJwtAuthFilter jwtAuthFilter(JwtService jwtService, UserDetailsService userDetailsService) {
        return new CustomJwtAuthFilter(jwtService, userDetailsService);
    }

    @Bean
    public LoggingFilter loggingFilter() {
        return new LoggingFilter();
    }

    @Bean
    public AuthenticationFailureHandler oauth2FailureHandler() {
        return new SimpleUrlAuthenticationFailureHandler("/auth/failure");
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           CustomJwtAuthFilter jwtAuthFilter,
                                           LoggingFilter loggingFilter,
                                           AuthenticationSuccessHandler oAuthHandler,
                                           AuthenticationFailureHandler oauth2FailureHandler) throws Exception {

        http
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET, "/login", "/favicon.ico", "/default-ui.css", "/auth/failure").permitAll()
                        .requestMatchers("/error", "/oauth2/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/*").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(loggingFilter, CustomJwtAuthFilter.class)
                .oauth2Login(o -> o
                        .userInfoEndpoint(u -> u.oidcUserService(oidcUserService())) // fallback on 503
                        .successHandler(oAuthHandler)
                        .failureHandler(oauth2FailureHandler)
                );

        return http.build();
    }

    @Bean
    public OidcUserService oidcUserService() {
        OidcUserService delegate = new OidcUserService();
        return new OidcUserService() {
            @Override
            public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
                try {
                    return delegate.loadUser(userRequest);
                } catch (OAuth2AuthenticationException ex) {
                    if ("invalid_user_info_response".equals(ex.getError().getErrorCode())) {
                        // Fallback to ID Token only
                        OidcIdToken idToken = userRequest.getIdToken();
                        return new DefaultOidcUser(Set.of(), idToken, "email");
                    }
                    throw ex;
                }
            }
        };
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}
