package heg.backendspring.security.course_access;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component
@Slf4j
public class CoursePermissionEvaluator implements PermissionEvaluator {

    private static final String COURSE_TYPE = "Course";
    private final ServiceCourseAccess serviceCourseAccess;

    public CoursePermissionEvaluator(ServiceCourseAccess serviceCourseAccess) {
        this.serviceCourseAccess = serviceCourseAccess;
    }

    @Override
    public boolean hasPermission(Authentication auth, Object targetDomainObject, Object permission) {
        return false; // Not used
    }

    @Override
    public boolean hasPermission(Authentication auth, Serializable targetId,
                                 String targetType, Object permission) {
        if (auth == null || !auth.isAuthenticated() || targetId == null || permission == null) {
            return false;
        }
        if (!COURSE_TYPE.equalsIgnoreCase(targetType)) {
            return false;
        }
        try {
            String userId = extractUserId(auth);
            Long courseId = toLong(targetId);
            CoursePermission required = CoursePermission.fromString(permission.toString());
            return serviceCourseAccess.hasPermission(userId, courseId, required);
        } catch (Exception e) {
            log.error("Error checking permission: {}", e.getMessage());
            return false;
        }
    }

    private String extractUserId(Authentication auth) {
        if (auth.getPrincipal() instanceof Jwt jwt) {
            return jwt.getSubject();
        }
        return auth.getName();
    }

    private Long toLong(Serializable id) {
        if (id instanceof Long) return (Long) id;
        if (id instanceof Number) return ((Number) id).longValue();
        return Long.parseLong(id.toString());
    }
}
