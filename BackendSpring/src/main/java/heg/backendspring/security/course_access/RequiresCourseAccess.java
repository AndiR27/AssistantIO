package heg.backendspring.security.course_access;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method as requiring course access.
 * The method MUST have a Long courseId as its first parameter.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresCourseAccess {

    /**
     * Required permission level (READ, WRITE, or OWNER).
     */
    CoursePermission value() default CoursePermission.READ;
}
