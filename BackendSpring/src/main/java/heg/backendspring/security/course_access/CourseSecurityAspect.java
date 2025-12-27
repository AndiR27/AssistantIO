package heg.backendspring.security.course_access;


import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.core.annotation.Order;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Order(1)
@Slf4j
public class CourseSecurityAspect {

    // Global admin role (from Keycloak)
    private static final String GLOBAL_ADMIN_ROLE = "ROLE_ADMIN";
    private final ServiceCourseAccess serviceCourseAccess;

    public CourseSecurityAspect(ServiceCourseAccess serviceCourseAccess) {
        this.serviceCourseAccess = serviceCourseAccess;
    }

    /**
     * Intercepts methods annotated with @RequiresCourseAccess.
     * Expects courseId as the first parameter.
     */
    @Before("@annotation(requiresCourseAccess) && args(courseId, ..)")
    public void checkCourseAccess(JoinPoint joinPoint,
                                  RequiresCourseAccess requiresCourseAccess,
                                  Long courseId) {

        if (courseId == null) {
            throw new IllegalArgumentException("Course ID cannot be null");
        }
        // Global admin bypasses all course-level checks
        if (isGlobalAdmin()) {
            log.debug("Global admin bypass for course {}", courseId);
            return;
        }
        String userId = getCurrentUserId();
        if (userId == null) {
            throw new AccessDeniedException("Authentication required");
        }
        // Get required permission from annotation
        CoursePermission required = requiresCourseAccess.value();
        if (!serviceCourseAccess.hasPermission(userId, courseId, required)) {
            log.warn("Access denied: user={}, course={}, required={}",
                    userId, courseId, required);
            throw new AccessDeniedException(
                    "You don't have " + required + " access to this course");
        }
        log.debug("Access granted: user={}, course={}, permission={}",
                userId, courseId, required);
    }

    /**
     * Check if current user has the global ADMIN role (from Keycloak).
     */
    private boolean isGlobalAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;

        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(GLOBAL_ADMIN_ROLE::equals);
    }

    private String getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Jwt jwt) {
            return jwt.getSubject();
        }
        return null;
    }
}
