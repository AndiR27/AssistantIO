package heg.backendspring.security.course_access;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@Slf4j
public class ServiceCourseAccess {

    private final RepositoryCourseAccess repository;

    public ServiceCourseAccess(RepositoryCourseAccess repository) {
        this.repository = repository;
    }

    // ==================== GRANT / REVOKE ====================

    /**
     * Grant or update permission for a user on a course.
     */
    public CourseAccess grantAccess(String userId, Long courseId, CoursePermission permission) {
        log.info("Granting {} access to user {} for course {}", permission, userId, courseId);

        Optional<CourseAccess> existing = repository.findByUserIdAndCourseId(userId, courseId);

        if (existing.isPresent()) {
            CourseAccess access = existing.get();
            access.setPermission(permission);
            return repository.save(access);
        }

        return repository.save(new CourseAccess(userId, courseId, permission));
    }

    /**
     * Revoke all access for a user on a course.
     */
    public void revokeAccess(String userId, Long courseId) {
        log.info("Revoking access for user {} on course {}", userId, courseId);
        repository.deleteByUserIdAndCourseId(userId, courseId);
    }

    // ==================== SHARING ====================

    /**
     * Share a course with another user (grant READ by default).
     */
    public CourseAccess shareCourse(Long courseId, String targetUserId) {
        return shareCourse(courseId, targetUserId, CoursePermission.READ);
    }

    /**
     * Share a course with another user with specific permission.
     */
    public CourseAccess shareCourse(Long courseId, String targetUserId, CoursePermission permission) {
        log.info("Sharing course {} with user {} (permission: {})", courseId, targetUserId, permission);
        return grantAccess(targetUserId, courseId, permission);
    }

    /**
     * Unshare a course (revoke access).
     */
    public void unshareCourse(Long courseId, String targetUserId) {
        log.info("Unsharing course {} from user {}", courseId, targetUserId);
        revokeAccess(targetUserId, courseId);
    }

    // ==================== PERMISSION CHECKS ====================

    /**
     * Check if a user has at least the required permission level.
     */
    @Transactional(readOnly = true)
    public boolean hasPermission(String userId, Long courseId, CoursePermission required) {
        return repository.findByUserIdAndCourseId(userId, courseId)
                .map(access -> access.getPermission().includes(required))
                .orElse(false);
    }

    /**
     * Get the permission level for a user on a course.
     */
    @Transactional(readOnly = true)
    public Optional<CoursePermission> getPermission(String userId, Long courseId) {
        return repository.findByUserIdAndCourseId(userId, courseId)
                .map(CourseAccess::getPermission);
    }

    // ==================== QUERIES ====================

    /**
     * Get all course IDs a user has access to.
     */
    @Transactional(readOnly = true)
    public List<Long> getAccessibleCourseIds(String userId) {
        return repository.findCourseIdsByUserId(userId);
    }

    /**
     * Get all users who have access to a course (for sharing UI).
     */
    @Transactional(readOnly = true)
    public List<CourseAccess> getCourseShares(Long courseId) {
        return repository.findByCourseId(courseId);
    }

    /**
     * Delete all access entries for a course (when course is deleted).
     */
    public void deleteAllAccessForCourse(Long courseId) {
        log.info("Deleting all access entries for course {}", courseId);
        repository.deleteByCourseId(courseId);
    }
}
