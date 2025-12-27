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
    Optional<CourseAccess> findByUserIdAndCourseId(String userId, Long courseId);

    List<CourseAccess> findByUserId(String userId);

    List<CourseAccess> findByCourseId(Long courseId);

    boolean existsByUserIdAndCourseId(String userId, Long courseId);

    @Modifying
    @Query("DELETE FROM CourseAccess ca WHERE ca.userId = :userId AND ca.courseId = :courseId")
    void deleteByUserIdAndCourseId(@Param("userId") String userId, @Param("courseId") Long courseId);

    @Modifying
    void deleteByCourseId(Long courseId);

    @Query("SELECT ca.courseId FROM CourseAccess ca WHERE ca.userId = :userId")
    List<Long> findCourseIdsByUserId(@Param("userId") String userId);
}
