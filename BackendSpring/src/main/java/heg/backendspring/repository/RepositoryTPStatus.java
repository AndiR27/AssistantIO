package heg.backendspring.repository;

import heg.backendspring.entity.TPStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RepositoryTPStatus extends JpaRepository<TPStatus, Long> {

    // Trouver la liste des TP Status pour un TP donné
    List<TPStatus> findByTpId(Long tpId);

    // Trouver la liste des TP Status pour un étudiant donné
    List<TPStatus> findByStudentId(Long studentId);
}
