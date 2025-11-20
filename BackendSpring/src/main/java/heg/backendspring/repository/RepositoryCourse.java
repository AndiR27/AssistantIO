package heg.backendspring.repository;


import heg.backendspring.entity.Course;
import heg.backendspring.entity.Student;
import heg.backendspring.entity.TP;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface RepositoryCourse extends JpaRepository<Course, Long> {

    //Trouver TOUS les étudiants inscrits à un cours
    @Query("SELECT DISTINCT s FROM Course c JOIN c.students s WHERE c.id = :courseId")
    Set<Student> findStudentsByCourseId(Long courseId);

    //Trouver un cours selon son code
    Optional<Course> findCourseByCode(String code);

    // Trouver un TP dans le cours selon son numéro
    @Query("SELECT tp FROM TP tp WHERE tp.course.id = :courseId AND tp.no = :no")
    Optional<TP> findTPByCourseIdAndNo(Long courseId, int no);


    //Trouver tous les TP d'un cours
    @Query("SELECT c.tps FROM Course c WHERE c.id = :courseId")
    Set<TP> findAllTPsByCourseId(Long courseId);

}
