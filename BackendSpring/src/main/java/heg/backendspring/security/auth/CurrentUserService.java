package heg.backendspring.security.auth;

import heg.backendspring.security.jwt.JwtClaims;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CurrentUserService {
    public CurrentUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        String email = extractEmail(authentication);
        // ex: ROLE_ASSISTANT
        Set<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(r -> r.equals("ROLE_ADMIN")
                        || r.equals("ROLE_ASSISTANT")
                        || r.equals("ROLE_VISITOR"))
                .collect(Collectors.toSet());

        return new CurrentUser(email, roles);
    }

    private String extractEmail(Authentication authentication) {
        Object principal = authentication.getPrincipal();

        // Le principal est typiquement un Jwt avec oauth2ResourceServer()
        if (principal instanceof Jwt jwt) {
            String email = jwt.getClaimAsString(JwtClaims.EMAIL);
            if (email != null && !email.isBlank()) {
                return email;
            }
        }

        // fallback (souvent sub / username selon config)
        return authentication.getName();
    }
}
