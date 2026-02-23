package heg.backendspring.TestServices;

import heg.backendspring.security.SecurityUtils;
import heg.backendspring.security.course_access.CoursePermission;
import heg.backendspring.security.course_access.CourseSecurityCheck;
import heg.backendspring.security.course_access.ServiceCourseAccess;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
public class TestCourseSecurityCheck {

    @MockitoBean
    private ServiceCourseAccess serviceCourseAccess;

    @MockitoBean
    private SecurityUtils securityUtils;

    @Autowired
    private CourseSecurityCheck courseSecurityCheck;

    private static final String USER_EMAIL = "user@example.com";
    private static final Long COURSE_ID = 42L;

    @Test
    @DisplayName("Global admin bypasses all course-level checks")
    void testAdminBypass() {
        when(securityUtils.isGlobalAdmin()).thenReturn(true);

        assertTrue(courseSecurityCheck.hasAccess(COURSE_ID, "READ"));
        assertTrue(courseSecurityCheck.hasAccess(COURSE_ID, "WRITE"));
        assertTrue(courseSecurityCheck.hasAccess(COURSE_ID, "OWNER"));

        // Service must never be called when admin bypass applies
        verifyNoInteractions(serviceCourseAccess);
    }

    @Test
    @DisplayName("User with sufficient permission is granted access")
    void testAccessGrantedForSufficientPermission() {
        when(securityUtils.isGlobalAdmin()).thenReturn(false);
        when(securityUtils.getCurrentUserEmail()).thenReturn(USER_EMAIL);
        when(serviceCourseAccess.hasPermission(USER_EMAIL, COURSE_ID, CoursePermission.READ)).thenReturn(true);

        assertTrue(courseSecurityCheck.hasAccess(COURSE_ID, "READ"));
    }

    @Test
    @DisplayName("User without permission is denied access")
    void testAccessDeniedForInsufficientPermission() {
        when(securityUtils.isGlobalAdmin()).thenReturn(false);
        when(securityUtils.getCurrentUserEmail()).thenReturn(USER_EMAIL);
        when(serviceCourseAccess.hasPermission(USER_EMAIL, COURSE_ID, CoursePermission.OWNER)).thenReturn(false);

        assertFalse(courseSecurityCheck.hasAccess(COURSE_ID, "OWNER"));
    }

    @Test
    @DisplayName("Null courseId is always denied")
    void testNullCourseIdDenied() {
        assertFalse(courseSecurityCheck.hasAccess(null, "READ"));
        verifyNoInteractions(securityUtils);
        verifyNoInteractions(serviceCourseAccess);
    }
}
