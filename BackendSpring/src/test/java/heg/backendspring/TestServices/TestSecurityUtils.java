package heg.backendspring.TestServices;

import heg.backendspring.security.SecurityUtils;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Arrays.stream;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
public class TestSecurityUtils {

    @Autowired
    private SecurityUtils securityUtils;

    @AfterEach
    void tearDown() { SecurityContextHolder.clearContext(); }
    private void setupAuth(String... roles) {
        SecurityContext ctx = mock(SecurityContext.class);
        Authentication auth = mock(Authentication.class);
        when(ctx.getAuthentication()).thenReturn(auth);
        when(auth.isAuthenticated()).thenReturn(true);
        List<GrantedAuthority> authorities = stream(roles)
                .map(r -> (GrantedAuthority) new SimpleGrantedAuthority(r))
                .toList();

        when(auth.getAuthorities()).thenReturn((List) authorities);
        SecurityContextHolder.setContext(ctx);
    }

    @Test
    @DisplayName("Admin detection - ROLE_ADMIN and ADMIN recognized, others not")
    void testAdminDetection() {
        // ROLE_ADMIN is admin
        setupAuth("ROLE_ADMIN");
        assertTrue(securityUtils.isGlobalAdmin());
        // ADMIN (no prefix) is admin
        setupAuth("ADMIN");
        assertTrue(securityUtils.isGlobalAdmin());
        // ROLE_ASSISTANT is not admin
        setupAuth("ROLE_ASSISTANT");
        assertFalse(securityUtils.isGlobalAdmin());
        // Multi-role with ADMIN is admin
        setupAuth("ROLE_ASSISTANT", "ROLE_ADMIN");
        assertTrue(securityUtils.isGlobalAdmin());
        // Unauthenticated is not admin
        SecurityContextHolder.clearContext();
        assertFalse(securityUtils.isGlobalAdmin());
    }
}
