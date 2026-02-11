package heg.backendspring.repository;

import heg.backendspring.entity.Evaluation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RepositoryEvaluation extends JpaRepository<Evaluation, Long> {

    public Evaluation findEvaluationByName(String name);

    @Query("SELECT e FROM Evaluation e WHERE e.course.id = :courseId")
    public List<Evaluation> findAllByCourseId(Long courseId);
}
