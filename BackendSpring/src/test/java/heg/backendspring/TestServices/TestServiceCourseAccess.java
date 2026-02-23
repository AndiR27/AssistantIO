package heg.backendspring.TestServices;

import heg.backendspring.models.CourseAccessDto;
import heg.backendspring.security.SecurityUtils;
import heg.backendspring.security.course_access.CourseAccess;
import heg.backendspring.security.course_access.CoursePermission;
import heg.backendspring.security.course_access.RepositoryCourseAccess;
import heg.backendspring.security.course_access.ServiceCourseAccess;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
public class TestServiceCourseAccess {

    @MockitoBean
    private RepositoryCourseAccess repositoryCourseAccess;

    @MockitoBean
    private SecurityUtils securityUtils;

    @Autowired
    private ServiceCourseAccess serviceCourseAccess;

    private static final String USER_A = "user-a@example.com";
    private static final String USER_B = "user-b@example.com";
    private static final Long COURSE_1 = 1L;
    private static final Long COURSE_2 = 2L;
    private static final Long COURSE_3 = 3L;

    @Test
    @DisplayName("User isolation - each user sees only their courses")
    void testUserIsolation() {
        // User A owns COURSE_1, User B owns COURSE_2
        when(repositoryCourseAccess.findCourseIdsByUserEmail(USER_A)).thenReturn(List.of(COURSE_1));
        when(repositoryCourseAccess.findCourseIdsByUserEmail(USER_B)).thenReturn(List.of(COURSE_2));
        when(repositoryCourseAccess.findByUserEmailAndCourseId(USER_A, COURSE_2)).thenReturn(Optional.empty());
        // User A sees only their course
        List<Long> userACourses = serviceCourseAccess.getAccessibleCourseIds(USER_A);
        assertEquals(1, userACourses.size());
        assertTrue(userACourses.contains(COURSE_1));
        assertFalse(userACourses.contains(COURSE_2));
        // User B sees only their course
        List<Long> userBCourses = serviceCourseAccess.getAccessibleCourseIds(USER_B);
        assertTrue(userBCourses.contains(COURSE_2));
        assertFalse(userBCourses.contains(COURSE_1));
        // User A cannot access User B's course
        assertFalse(serviceCourseAccess.hasPermission(USER_A, COURSE_2, CoursePermission.READ));
    }

    @Test
    @DisplayName("Sharing - grant, access, and revoke")
    void testSharing() {
        when(repositoryCourseAccess.findByUserEmailAndCourseId(USER_B, COURSE_1)).thenReturn(Optional.empty());
        when(repositoryCourseAccess.save(any(CourseAccess.class))).thenAnswer(inv -> inv.getArgument(0));
        // Share with READ
        CourseAccessDto shared = serviceCourseAccess.shareCourse(COURSE_1, USER_B);
        assertEquals(CoursePermission.READ, shared.permission());
        assertEquals(USER_B, shared.userId());

        // Shared user can access
        when(repositoryCourseAccess.findByUserEmailAndCourseId(USER_B, COURSE_1))
                .thenReturn(Optional.of(new CourseAccess(USER_B, COURSE_1, CoursePermission.READ)));
        assertTrue(serviceCourseAccess.hasPermission(USER_B, COURSE_1, CoursePermission.READ));
        assertFalse(serviceCourseAccess.hasPermission(USER_B, COURSE_1, CoursePermission.WRITE));

        // Unshare
        when(securityUtils.getCurrentUserEmail()).thenReturn(USER_A);
        serviceCourseAccess.unshareCourse(COURSE_1, USER_B);
        verify(repositoryCourseAccess).deleteByUserEmailAndCourseId(USER_B, COURSE_1);
    }

    @Test
    @DisplayName("Permission hierarchy - OWNER > WRITE > READ")
    void testPermissionHierarchy() {
        assertTrue(CoursePermission.OWNER.includes(CoursePermission.READ));
        assertTrue(CoursePermission.OWNER.includes(CoursePermission.WRITE));
        assertTrue(CoursePermission.WRITE.includes(CoursePermission.READ));
        assertFalse(CoursePermission.READ.includes(CoursePermission.WRITE));
        assertFalse(CoursePermission.WRITE.includes(CoursePermission.OWNER));
    }

    @Test
    @DisplayName("grantAccess upgrades an existing permission")
    void testGrantAccessUpgrade() {
        CourseAccess existing = new CourseAccess(USER_A, COURSE_1, CoursePermission.READ);
        when(repositoryCourseAccess.findByUserEmailAndCourseId(USER_A, COURSE_1)).thenReturn(Optional.of(existing));
        when(repositoryCourseAccess.save(any(CourseAccess.class))).thenAnswer(inv -> inv.getArgument(0));

        CourseAccessDto result = serviceCourseAccess.grantAccess(USER_A, COURSE_1, CoursePermission.WRITE);

        assertEquals(CoursePermission.WRITE, result.permission());
        assertEquals(USER_A, result.userId());
        verify(repositoryCourseAccess).save(existing);
    }

    @Test
    @DisplayName("getCourseShares returns all access entries for a course")
    void testGetCourseShares() {
        List<CourseAccess> accesses = List.of(
                new CourseAccess(USER_A, COURSE_1, CoursePermission.OWNER),
                new CourseAccess(USER_B, COURSE_1, CoursePermission.READ)
        );
        when(repositoryCourseAccess.findByCourseId(COURSE_1)).thenReturn(accesses);

        List<CourseAccessDto> shares = serviceCourseAccess.getCourseShares(COURSE_1);

        assertEquals(2, shares.size());
        assertTrue(shares.stream().anyMatch(s -> s.userId().equals(USER_A) && s.permission() == CoursePermission.OWNER));
        assertTrue(shares.stream().anyMatch(s -> s.userId().equals(USER_B) && s.permission() == CoursePermission.READ));
    }

    @Test
    @DisplayName("deleteAllAccessForCourse delegates to repository")
    void testDeleteAllAccessForCourse() {
        serviceCourseAccess.deleteAllAccessForCourse(COURSE_1);
        verify(repositoryCourseAccess).deleteByCourseId(COURSE_1);
    }

    @Test
    @DisplayName("CoursePermission.fromString rejects null and unknown values")
    void testInvalidPermissionString() {
        assertThrows(IllegalArgumentException.class, () -> CoursePermission.fromString(null));
        assertThrows(IllegalArgumentException.class, () -> CoursePermission.fromString("INVALID"));
        assertThrows(IllegalArgumentException.class, () -> CoursePermission.fromString(""));
    }
}
