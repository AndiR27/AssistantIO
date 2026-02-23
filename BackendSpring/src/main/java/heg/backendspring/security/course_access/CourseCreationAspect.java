package heg.backendspring.security.course_access;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class CourseCreationAspect {

    private final ServiceCourseAccess serviceCourseAccess;
    public CourseCreationAspect(ServiceCourseAccess serviceCourseAccess) {
        this.serviceCourseAccess = serviceCourseAccess;
    }
    /**
     * After course creation, grant OWNER to creator.
     */
    @AfterReturning(
            pointcut = "execution(* heg.backendspring.service.ServiceCourse.addCourse(..))",
            returning = "course"
    )
    public void afterCourseCreated(Object course) {
        try {
            String userEmail = getCurrentUserEmail();
            Long courseId = getCourseId(course);
            if (userEmail != null && courseId != null) {
                serviceCourseAccess.grantAccess(userEmail, courseId, CoursePermission.OWNER);
                log.info("Granted OWNER access to {} for new course {}", userEmail, courseId);
            }
        } catch (Exception e) {
            log.error("Failed to grant access after course creation: {}", e.getMessage());
        }
    }
    private String getCurrentUserEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Jwt jwt) {
            String email = jwt.getClaimAsString("email");
            return (email != null && !email.isBlank()) ? email : jwt.getSubject();
        }
        return null;
    }
    private Long getCourseId(Object course) {
        try {

            var method = course.getClass().getMethod("id");
            Object id = method.invoke(course);
            return id instanceof Long ? (Long) id : ((Number) id).longValue();
        } catch (Exception e) {
            return null;
        }
    }
}
