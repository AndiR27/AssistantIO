package heg.backendspring.TestServices;

import heg.backendspring.security.course_access.CourseAccess;
import heg.backendspring.security.course_access.CoursePermission;
import heg.backendspring.security.course_access.RepositoryCourseAccess;
import heg.backendspring.security.course_access.ServiceCourseAccess;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
public class TestServiceCourseAccess {

    @MockitoBean
    private RepositoryCourseAccess repositoryCourseAccess;

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
        when(repositoryCourseAccess.findCourseIdsByUserId(USER_A)).thenReturn(List.of(COURSE_1));
        when(repositoryCourseAccess.findCourseIdsByUserId(USER_B)).thenReturn(List.of(COURSE_2));
        when(repositoryCourseAccess.findByUserIdAndCourseId(USER_A, COURSE_2)).thenReturn(Optional.empty());
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
        when(repositoryCourseAccess.findByUserIdAndCourseId(USER_B, COURSE_1)).thenReturn(Optional.empty());
        when(repositoryCourseAccess.save(any(CourseAccess.class))).thenAnswer(inv -> inv.getArgument(0));
        // Share with READ
        CourseAccess shared = serviceCourseAccess.shareCourse(COURSE_1, USER_B);
        assertEquals(CoursePermission.READ, shared.getPermission());
        assertEquals(USER_B, shared.getUserId());

        // Shared user can access
        when(repositoryCourseAccess.findByUserIdAndCourseId(USER_B, COURSE_1))
                .thenReturn(Optional.of(new CourseAccess(USER_B, COURSE_1, CoursePermission.READ)));
        assertTrue(serviceCourseAccess.hasPermission(USER_B, COURSE_1, CoursePermission.READ));
        assertFalse(serviceCourseAccess.hasPermission(USER_B, COURSE_1, CoursePermission.WRITE));

        // Unshare
        serviceCourseAccess.unshareCourse(COURSE_1, USER_B);
        verify(repositoryCourseAccess).deleteByUserIdAndCourseId(USER_B, COURSE_1);
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
}
