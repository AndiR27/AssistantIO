package heg.backendspring.security.course_access;

import heg.backendspring.mapping.MapperCourseAccess;
import heg.backendspring.models.CourseAccessDto;
import heg.backendspring.security.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@Slf4j
public class ServiceCourseAccess {

    private final RepositoryCourseAccess repository;
    private final MapperCourseAccess mapper;
    private final SecurityUtils securityUtils;

    public ServiceCourseAccess(RepositoryCourseAccess repository,
                                MapperCourseAccess mapper,
                                SecurityUtils securityUtils) {
        this.repository = repository;
        this.mapper = mapper;
        this.securityUtils = securityUtils;
    }

    // ==================== GRANT / REVOKE ====================

    /**
     * Grant or update permission for a user (by email) on a course.
     */
    public CourseAccessDto grantAccess(String userEmail, Long courseId, CoursePermission permission) {
        log.info("Granting {} access to {} for course {}", permission, userEmail, courseId);

        Optional<CourseAccess> existing = repository.findByUserEmailAndCourseId(userEmail, courseId);

        if (existing.isPresent()) {
            CourseAccess access = existing.get();
            access.setPermission(permission);
            return mapper.toDto(repository.save(access));
        }

        return mapper.toDto(repository.save(new CourseAccess(userEmail, courseId, permission)));
    }

    /**
     * Revoke all access for a user (by email) on a course.
     * Prevents a user from revoking their own OWNER access.
     */
    public void revokeAccess(String userEmail, Long courseId) {
        String currentEmail = securityUtils.getCurrentUserEmail();
        if (userEmail.equalsIgnoreCase(currentEmail)
                && hasPermission(currentEmail, courseId, CoursePermission.OWNER)) {
            throw new AccessDeniedException(
                    "Vous ne pouvez pas retirer votre propre accès OWNER sur ce cours.");
        }

        log.info("Revoking access for {} on course {}", userEmail, courseId);
        repository.deleteByUserEmailAndCourseId(userEmail, courseId);
    }

    // ==================== SHARING ====================

    /**
     * Share a course with another user by email (grant READ by default).
     */
    public CourseAccessDto shareCourse(Long courseId, String targetUserEmail) {
        return shareCourse(courseId, targetUserEmail, CoursePermission.READ);
    }

    /**
     * Share a course with another user by email with specific permission.
     */
    public CourseAccessDto shareCourse(Long courseId, String targetUserEmail, CoursePermission permission) {
        log.info("Sharing course {} with {} (permission: {})", courseId, targetUserEmail, permission);
        return grantAccess(targetUserEmail, courseId, permission);
    }

    /**
     * Unshare a course (revoke access). Delegates to revokeAccess which guards against self-revoke.
     */
    public void unshareCourse(Long courseId, String targetUserEmail) {
        log.info("Unsharing course {} from {}", courseId, targetUserEmail);
        revokeAccess(targetUserEmail, courseId);
    }

    // ==================== PERMISSION CHECKS ====================

    /**
     * Check if a user has at least the required permission level.
     */
    @Transactional(readOnly = true)
    public boolean hasPermission(String userEmail, Long courseId, CoursePermission required) {
        return repository.findByUserEmailAndCourseId(userEmail, courseId)
                .map(access -> access.getPermission().includes(required))
                .orElse(false);
    }

    /**
     * Get the permission level for a user on a course.
     */
    @Transactional(readOnly = true)
    public Optional<CoursePermission> getPermission(String userEmail, Long courseId) {
        return repository.findByUserEmailAndCourseId(userEmail, courseId)
                .map(CourseAccess::getPermission);
    }

    // ==================== QUERIES ====================

    /**
     * Get all course IDs a user has access to.
     */
    @Transactional(readOnly = true)
    public List<Long> getAccessibleCourseIds(String userEmail) {
        return repository.findCourseIdsByUserEmail(userEmail);
    }

    /**
     * Get all users who have access to a course (for sharing UI).
     */
    @Transactional(readOnly = true)
    public List<CourseAccessDto> getCourseShares(Long courseId) {
        return repository.findByCourseId(courseId).stream()
                .map(mapper::toDto)
                .toList();
    }

    /**
     * Get all course accesses for the currently logged-in user.
     */
    @Transactional(readOnly = true)
    public List<CourseAccessDto> getMyAccesses() {
        String email = securityUtils.getCurrentUserEmail();
        return repository.findByUserEmail(email).stream()
                .map(mapper::toDto)
                .toList();
    }

    /**
     * Delete all access entries for a course (when course is deleted).
     */
    public void deleteAllAccessForCourse(Long courseId) {
        log.info("Deleting all access entries for course {}", courseId);
        repository.deleteByCourseId(courseId);
    }
}