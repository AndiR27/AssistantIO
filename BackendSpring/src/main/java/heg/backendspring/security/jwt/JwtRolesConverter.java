package heg.backendspring.security.jwt;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.authority.SimpleGrantedAuthority; // <-- IMPORTANT


import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class JwtRolesConverter implements Converter<Jwt, Collection<GrantedAuthority>> {
    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        // Implémentation de la conversion des rôles JWT en GrantedAuthority
        // Exemple : extraire les rôles du JWT et les mapper aux autorités Spring Security
        // Retourner une collection de GrantedAuthority
        Map<String, Object> realmAccess = jwt.getClaim(JwtClaims.REALM_ACCESS);
        if (realmAccess == null) {
            return List.of();
        }

        Object rolesObj = realmAccess.get(JwtClaims.ROLES);
        if (!(rolesObj instanceof Collection<?> roles)) {
            return List.of();
        }

        return roles.stream()
                .filter(Objects::nonNull)
                .map(Object::toString)
                .map(String::trim)
                .filter(r -> !r.isBlank())
                .map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());
    }
}
