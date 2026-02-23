package heg.backendspring.rest;

import heg.backendspring.api.CourseAccessApi;
import heg.backendspring.models.CourseAccessDto;
import heg.backendspring.security.course_access.CoursePermission;
import heg.backendspring.security.course_access.ServiceCourseAccess;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.NativeWebRequest;

import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@Slf4j
public class ControllerCourseAccess implements CourseAccessApi {

    private final ServiceCourseAccess service;

    @Override
    public Optional<NativeWebRequest> getRequest() {
        return CourseAccessApi.super.getRequest();
    }

    @GetMapping("/course-access/my-accesses")
    public ResponseEntity<List<CourseAccessDto>> getMyAccesses() {
        return ResponseEntity.ok(service.getMyAccesses());
    }

    @PreAuthorize("@courseSecurity.hasAccess(#courseId, 'OWNER')")
    @Override
    public ResponseEntity<Void> deleteAllAccessForCourse(Long courseId) {
        service.deleteAllAccessForCourse(courseId);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<List<Long>> getAccessibleCourseIds(String userId) {
        return ResponseEntity.ok(service.getAccessibleCourseIds(userId));
    }

    @PreAuthorize("@courseSecurity.hasAccess(#courseId, 'READ')")
    @Override
    public ResponseEntity<String> getCoursePermission(String userId, Long courseId) {
        return service.getPermission(userId, courseId)
                .map(p -> ResponseEntity.ok(p.name()))
                .orElse(ResponseEntity.noContent().build());
    }

    @PreAuthorize("@courseSecurity.hasAccess(#courseId, 'OWNER')")
    @Override
    public ResponseEntity<List<CourseAccessDto>> getCourseShares(Long courseId) {
        return ResponseEntity.ok(service.getCourseShares(courseId));
    }


    @PreAuthorize("@courseSecurity.hasAccess(#courseId, 'OWNER')")
    @Override
    public ResponseEntity<CourseAccessDto> grantCourseAccess(String userId, Long courseId, String permission) {
        CourseAccessDto dto = service.grantAccess(userId, courseId, CoursePermission.fromString(permission));
        return ResponseEntity.ok(dto);
    }

    @PreAuthorize("@courseSecurity.hasAccess(#courseId, 'READ')")
    @Override
    public ResponseEntity<Boolean> hasCoursePermission(String userId, Long courseId, String required) {
        return ResponseEntity.ok(service.hasPermission(userId, courseId, CoursePermission.fromString(required)));
    }

    @PreAuthorize("@courseSecurity.hasAccess(#courseId, 'OWNER')")
    @Override
    public ResponseEntity<Void> revokeCourseAccess(String userId, Long courseId) {
        service.revokeAccess(userId, courseId);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("@courseSecurity.hasAccess(#courseId, 'OWNER')")
    @Override
    public ResponseEntity<CourseAccessDto> shareCourseDefaultPermission(Long courseId, String targetUserId) {
        return ResponseEntity.ok(service.shareCourse(courseId, targetUserId));
    }

    @PreAuthorize("@courseSecurity.hasAccess(#courseId, 'OWNER')")
    @Override
    public ResponseEntity<CourseAccessDto> shareCourseWithPermission(Long courseId, String targetUserId, String permission) {
        return ResponseEntity.ok(service.shareCourse(courseId, targetUserId, CoursePermission.fromString(permission)));
    }

    @PreAuthorize("@courseSecurity.hasAccess(#courseId, 'OWNER')")
    @Override
    public ResponseEntity<Void> unshareCourse(Long courseId, String targetUserId) {
        service.unshareCourse(courseId, targetUserId);
        return ResponseEntity.noContent().build();
    }
}