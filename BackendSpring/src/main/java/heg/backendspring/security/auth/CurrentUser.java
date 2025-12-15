package heg.backendspring.security.auth;

import java.util.Set;

public record CurrentUser(
        String email,
        Set<String> roles
) {
}
