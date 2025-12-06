package heg.backendspring.repository;

import heg.backendspring.entity.TPStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RepositoryTPStatus extends JpaRepository<TPStatus, Long> {

    // Trouver la liste des TP Status pour un TP donné
    List<TPStatus> findByTpId(Long tpId);

    // Trouver la liste des TP Status pour un étudiant donné
    List<TPStatus> findByStudentId(Long studentId);

    //Trouver un TP Status pour un étudiant et un TP donnés
    @Query("SELECT tps FROM TPStatus tps WHERE tps.student.id = :studentId AND tps.tp.id = :tpId")
    Optional<TPStatus> findByStudentIdAndTpId(Long studentId, Long tpId);
}
