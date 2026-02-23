package heg.backendspring.security.course_access;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RepositoryCourseAccess extends JpaRepository<CourseAccess, Long> {
    Optional<CourseAccess> findByUserEmailAndCourseId(String userEmail, Long courseId);

    List<CourseAccess> findByUserEmail(String userEmail);

    List<CourseAccess> findByCourseId(Long courseId);

    boolean existsByUserEmailAndCourseId(String userEmail, Long courseId);

    @Modifying
    @Query("DELETE FROM CourseAccess ca WHERE ca.userEmail = :userEmail AND ca.courseId = :courseId")
    void deleteByUserEmailAndCourseId(@Param("userEmail") String userEmail, @Param("courseId") Long courseId);

    @Modifying
    void deleteByCourseId(Long courseId);

    @Query("SELECT ca.courseId FROM CourseAccess ca WHERE ca.userEmail = :userEmail")
    List<Long> findCourseIdsByUserEmail(@Param("userEmail") String userEmail);
}