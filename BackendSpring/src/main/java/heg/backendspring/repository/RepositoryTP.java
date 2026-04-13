package heg.backendspring.repository;

import heg.backendspring.entity.TP;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface RepositoryTP extends JpaRepository<TP, Long> {

    /**
     * Trouve tous les TPs d'un cours par l'ID du cours.
     */
    List<TP> findAllByCourseId(Long courseId);

    /**
     * Trouve tous les TPs d'un cours (retourne un Set).
     */
    Set<TP> findByCourseId(Long courseId);

    /**
     * Trouve un TP par l'ID du cours et le numéro du TP.
     */
    Optional<TP> findByCourseIdAndNo(Long courseId, int no);

}
