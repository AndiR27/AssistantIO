package heg.backendspring.repository;

import heg.backendspring.entity.TP;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RepositoryTP extends JpaRepository<TP, Long> {

    List<TP> findAllByCourseId(Long courseId);


}
