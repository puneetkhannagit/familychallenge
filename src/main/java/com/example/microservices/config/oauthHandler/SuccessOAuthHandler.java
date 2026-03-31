// java
package com.example.microservices.config.oauthHandler;


import com.example.microservices.entity.User;
import com.example.microservices.repository.UserRepository;
import com.example.microservices.service.JwtService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseCookie;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;

@Configuration
public class SuccessOAuthHandler {

    private final UserRepository userRepository;

    public SuccessOAuthHandler(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Bean
    public AuthenticationSuccessHandler oauth2SuccessHandler(JwtService jwtService) {
        return (request, response, authentication) -> {
            Object principal = authentication.getPrincipal();

            String email = null;
            String firstName = null;
            String lastName = null;
            String pictureUrl = null;
            String locale = null;

            if (principal instanceof OidcUser oidcUser) {
                // OIDC standard claims
                email = oidcUser.getEmail();
                if (email == null) email = oidcUser.getAttribute("email");

                firstName = oidcUser.getGivenName();
                if (firstName == null) firstName = oidcUser.getAttribute("given_name");

                lastName = oidcUser.getFamilyName();
                if (lastName == null) lastName = oidcUser.getAttribute("family_name");

                pictureUrl = oidcUser.getPicture();
                if (pictureUrl == null) pictureUrl = oidcUser.getAttribute("picture");

                locale = oidcUser.getLocale();
                if (locale == null) locale = oidcUser.getAttribute("locale");
            } else if (principal instanceof OAuth2User oAuth2User) {
                // Non-OIDC providers: attributes are provider-specific but Google also returns these keys
                email = oAuth2User.getAttribute("email");
                firstName = oAuth2User.getAttribute("given_name");
                lastName = oAuth2User.getAttribute("family_name");
                pictureUrl = oAuth2User.getAttribute("picture");
                locale = oAuth2User.getAttribute("locale");

                // Some providers return nested structures; keep a small fallback to avoid surprises
                if (pictureUrl == null) {
                    Object picture = oAuth2User.getAttributes().get("picture");
                    if (picture instanceof Map<?, ?> m) {
                        Object url = m.get("url");
                        if (url instanceof String s) pictureUrl = s;
                    }
                }
            }

            String subject = authentication.getName();
            String username = (email != null && !email.isBlank()) ? email : subject;

            Optional<User> existing = userRepository.findByUsername(username);
            User user = existing.orElseGet(() -> {
                User u = new User();
                u.setUsername(username);
                u.setPassword("");
                u.setRole("ROLE_USER");
                return u;
            });

            // Persist or refresh profile info (safe even if null)
            if (email != null && !email.isBlank()) user.setEmail(email);
            if (firstName != null && !firstName.isBlank()) user.setFirstName(firstName);
            if (lastName != null && !lastName.isBlank()) user.setLastName(lastName);
            if (pictureUrl != null && !pictureUrl.isBlank()) user.setPictureUrl(pictureUrl);
            if (locale != null && !locale.isBlank()) user.setLocation(locale);

            userRepository.save(user);

            String token = jwtService.createToken(username);

            ResponseCookie cookie = ResponseCookie.from("jwt", token)
                    .httpOnly(true)
                    .secure(false) // true on HTTPS
                    .path("/")
                    .maxAge(Duration.ofDays(1))
                    .sameSite("Lax")
                    .build();
            response.addHeader("Set-Cookie", cookie.toString());

            response.sendRedirect("/");
        };
    }
}
