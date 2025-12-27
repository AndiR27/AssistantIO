package heg.backendspring.security.course_access;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "course_access",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_course_access_user_course",
                columnNames = {"user_id", "course_id"}
        ),
        indexes = {
                @Index(name = "idx_course_access_user", columnList = "user_id"),
                @Index(name = "idx_course_access_course", columnList = "course_id")
        })
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class CourseAccess {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    //keycloak stub user id
    @Column(name = "user_id", nullable = false, length = 255)
    private String userId;

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CoursePermission permission;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public CourseAccess() {
        this.createdAt = LocalDateTime.now();
    }

    public CourseAccess(String userId, Long courseId, CoursePermission permission) {
        this();
        this.userId = userId;
        this.courseId = courseId;
        this.permission = permission;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

}
