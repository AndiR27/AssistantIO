package heg.backendspring.repository;


import heg.backendspring.entity.Course;
import heg.backendspring.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface RepositoryCourse extends JpaRepository<Course, Long> {

    //Trouver TOUS les étudiants inscrits à un cours
    @Query("SELECT DISTINCT s FROM Course c JOIN c.students s WHERE c.id = :courseId")
    Set<Student> findStudentsByCourseId(Long courseId);

    //Trouver un etudiant inscrit à un cours selon son id
    @Query("SELECT s FROM Course c JOIN c.students s WHERE c.id = :courseId AND s.id = :studentId")
    Optional<Student> findStudentByCourseIdAndStudentId(@Param("courseId") Long courseId, @Param("studentId") Long studentId);

    //Trouver un cours selon son code
    Optional<Course> findCourseByCode(String code);

    /**
     * Trouve un cours avec ses etudiants charges (evite LazyInitializationException).
     */
    @Query("SELECT DISTINCT c FROM Course c LEFT JOIN FETCH c.students WHERE c.id = :courseId")
    Optional<Course> findByIdWithStudents(@Param("courseId") Long courseId);

}
