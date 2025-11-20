package heg.backendspring.repository;

import heg.backendspring.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RepositoryStudent extends JpaRepository<Student, Long> {

    Optional<Student> findStudentByEmail(String email);

    boolean existsByEmail(String email);
}
