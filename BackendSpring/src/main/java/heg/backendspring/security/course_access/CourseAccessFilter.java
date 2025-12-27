package heg.backendspring.security.course_access;

import heg.backendspring.security.SecurityUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Order(1)
@Slf4j
public class CourseAccessFilter extends OncePerRequestFilter {

    // Matches /api/courses/{id} or /api/courses/{id}/anything
    private static final Pattern COURSE_PATTERN =
            Pattern.compile("^/(?:api/)?admin/courses/(\\d+)(?:/.*)?$");



    private final ServiceCourseAccess courseAccessService;
    private final SecurityUtils securityUtils;

    public CourseAccessFilter(ServiceCourseAccess courseAccessService,
                              SecurityUtils securityUtils) {
        this.courseAccessService = courseAccessService;
        this.securityUtils = securityUtils;
    }

    /**
     * Méthode de filtrage pour vérifier les permissions d'accès aux cours.
     * @param request : HttpServletRequest
     * @param response : HttpServletResponse
     * @param filterChain : FilterChain : chaîne de filtres à exécuter
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        String path = request.getRequestURI();
        Matcher matcher = COURSE_PATTERN.matcher(path);
        if (matcher.matches()) {
            Long courseId = Long.parseLong(matcher.group(1));
            // Si l'utilisateur est un administrateur global, il contourne les vérifications
            // d'accès au niveau du cours
            if (securityUtils.isGlobalAdmin()) {
                log.debug("Global admin bypass for course {}", courseId);
                filterChain.doFilter(request, response);
                return;
            }

            String userId;
            try {
                userId = securityUtils.getCurrentUserId();
            } catch (Exception e) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authentication required");
                return;
            }
            CoursePermission required = getRequiredPermission(request.getMethod());
            if (!courseAccessService.hasPermission(userId, courseId, required)) {
                log.warn("Access denied: user={}, course={}, required={}", userId, courseId, required);
                response.sendError(HttpServletResponse.SC_FORBIDDEN,
                        "Access denied to course " + courseId);
                return;
            }
            log.debug("Access granted: user={}, course={}", userId, courseId);
        }
        filterChain.doFilter(request, response);
    }
    private CoursePermission getRequiredPermission(String method) {
        return switch (method) {
            case "DELETE" -> CoursePermission.OWNER;
            case "POST", "PUT", "PATCH" -> CoursePermission.WRITE;
            default -> CoursePermission.READ;
        };
    }
}
