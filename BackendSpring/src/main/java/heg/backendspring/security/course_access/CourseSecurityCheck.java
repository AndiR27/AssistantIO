package heg.backendspring.security.course_access;

import heg.backendspring.security.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Single bean for course-level access control.
 * Used with @PreAuthorize("@courseSecurity.hasAccess(#courseId, 'READ')") on service methods.
 */
@Component("courseSecurity")
@Slf4j
public class CourseSecurityCheck {

    private final ServiceCourseAccess courseAccessService;
    private final SecurityUtils securityUtils;

    public CourseSecurityCheck(ServiceCourseAccess courseAccessService,
                               SecurityUtils securityUtils) {
        this.courseAccessService = courseAccessService;
        this.securityUtils = securityUtils;
    }

    /**
     * Check if the current user has at least the given permission on a course.
     * Global admins bypass all checks.
     *
     * @param courseId   the course to check access for
     * @param permission the minimum required permission (READ, WRITE, or OWNER)
     * @return true if access is granted
     */
    public boolean hasAccess(Long courseId, String permission) {
        if (courseId == null) {
            log.warn("Course ID is null in access check");
            return false;
        }

        // Global admin bypasses all course-level checks
        if (securityUtils.isGlobalAdmin()) {
            log.debug("Global admin bypass for course {}", courseId);
            return true;
        }

        String userEmail = securityUtils.getCurrentUserEmail();
        CoursePermission required = CoursePermission.fromString(permission);
        boolean allowed = courseAccessService.hasPermission(userEmail, courseId, required);

        if (!allowed) {
            log.warn("Access denied: user={}, course={}, required={}", userEmail, courseId, required);
        } else {
            log.debug("Access granted: user={}, course={}, permission={}", userEmail, courseId, required);
        }

        return allowed;
    }
}
