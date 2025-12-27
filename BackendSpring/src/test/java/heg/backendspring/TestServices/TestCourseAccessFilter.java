package heg.backendspring.TestServices;

import heg.backendspring.security.SecurityUtils;
import heg.backendspring.security.course_access.CourseAccessFilter;
import heg.backendspring.security.course_access.CoursePermission;
import heg.backendspring.security.course_access.ServiceCourseAccess;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import static org.mockito.Mockito.*;
@SpringBootTest
@ActiveProfiles("test")
public class TestCourseAccessFilter {

    @Autowired
    private CourseAccessFilter courseAccessFilter;

    @MockitoBean
    private ServiceCourseAccess serviceCourseAccess;

    @MockitoBean
    private SecurityUtils securityUtils;

    private static final String USER_A = "user-a@example.com";
    @Test
    @DisplayName("Admin bypasses all course access checks")
    void testAdminBypass() throws Exception {
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse res = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);
        when(req.getRequestURI()).thenReturn("localhost:8088/api/admin/courses/1/students");
        when(securityUtils.isGlobalAdmin()).thenReturn(true);
        courseAccessFilter.doFilter(req, res, chain);
        verify(chain).doFilter(req, res);
        verify(serviceCourseAccess, never()).hasPermission(any(), any(), any());
    }

    @Test
    @DisplayName("User with permission is allowed")
    void testUserAllowed() throws Exception {
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse res = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);
        when(req.getRequestURI()).thenReturn("/api/admin/courses/1");
        when(req.getMethod()).thenReturn("GET");
        when(req.getAttribute(anyString())).thenReturn(null);
        when(securityUtils.isGlobalAdmin()).thenReturn(false);
        when(securityUtils.getCurrentUserId()).thenReturn(USER_A);
        when(serviceCourseAccess.hasPermission(USER_A, 1L, CoursePermission.READ)).thenReturn(true);
        courseAccessFilter.doFilter(req, res, chain);
        verify(chain).doFilter(req, res);
    }

    @Test
    @DisplayName("User without permission is denied")
    void testUserDenied() throws Exception {
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse res = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);
        when(req.getRequestURI()).thenReturn("/api/admin/courses/1");
        when(req.getMethod()).thenReturn("GET");
        when(req.getAttribute(anyString())).thenReturn(null);
        when(securityUtils.isGlobalAdmin()).thenReturn(false);
        when(securityUtils.getCurrentUserId()).thenReturn(USER_A);
        when(serviceCourseAccess.hasPermission(USER_A, 1L, CoursePermission.READ)).thenReturn(false);
        courseAccessFilter.doFilter(req, res, chain);

        // Verify access denied - check whichever your filter uses:

        verify(res).sendError(eq(HttpServletResponse.SC_FORBIDDEN), anyString());
        // OR if your filter uses setStatus instead:
        // verify(res).setStatus(HttpServletResponse.SC_FORBIDDEN);
    }
}
