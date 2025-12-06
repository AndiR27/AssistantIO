package heg.backendspring.service;

import heg.backendspring.entity.Student;
import heg.backendspring.entity.TP;
import heg.backendspring.entity.TPStatus;
import heg.backendspring.enums.StudentSubmissionType;
import heg.backendspring.mapping.MapperTPStatus;
import heg.backendspring.models.TPStatusDto;
import heg.backendspring.repository.RepositoryTPStatus;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ServiceTPStatus {

    private final RepositoryTPStatus repositoryTPStatus;
    private final MapperTPStatus mapperTPStatus;

    // Methodes CRUD pour TPStatus peuvent être ajoutées ici

    /**
     * Trouver un TPStatus par son id
     */
    public Optional<TPStatusDto> findTPStatus(Long id) {
        return repositoryTPStatus.findById(id).map(mapperTPStatus::toDto);
    }


    /**
     * Créer un nouveau TPStatus : si déjà existant, retourne l'entité existante,
     * sinon crée et retourne la nouvelle entité.
     */
    @Transactional
    public TPStatusDto createTPStatus(TPStatusDto tpStatusDto) {
        // Vérifier si le TPStatus existe déjà (par exemple, par nom ou autre critère unique)
        Optional<TPStatus> existingStatus = repositoryTPStatus.findById(tpStatusDto.id());
        if (existingStatus.isPresent()) {
            log.info("TPStatus already exists for student {} and TP {}", existingStatus.get().getStudent().getEmail(),
                    existingStatus.get().getTp().getNo());
            return mapperTPStatus.toDto(existingStatus.get());
        }
        // Sinon, créer un nouveau TPStatus
        log.info("Creating new TPStatus for student {} and TP {}", tpStatusDto.studentId(), tpStatusDto.tpId());
        TPStatus newTPStatus = mapperTPStatus.toEntity(tpStatusDto);
        return mapperTPStatus.toDto(repositoryTPStatus.save(newTPStatus));
    }

    /**
     * Met à jour le statut de soumission d'un TPStatus existant
     */
    @Transactional
    public TPStatusDto updateTPStatus(Long idTPStatus, StudentSubmissionType studentSubmissionType) {
        Optional<TPStatus> tpStatusOpt = repositoryTPStatus.findById(idTPStatus);
        if (tpStatusOpt.isPresent()) {
            TPStatus existingStatus = tpStatusOpt.get();
            existingStatus.setStudentSubmission(studentSubmissionType);
            repositoryTPStatus.save(existingStatus);
            log.info("Updated TPStatus id {} with submission type {}", existingStatus.getId(), studentSubmissionType);
            return mapperTPStatus.toDto(existingStatus);
        } else {
            log.warn("TPStatus id {} not found for update", idTPStatus);
        }
        return null;
    }

    /**
     * Supprime un TPStatus par son id
     */
    @Transactional
    public void deleteTPStatus(Long id) {
        if (repositoryTPStatus.existsById(id)) {
            repositoryTPStatus.deleteById(id);
            log.info("Deleted TPStatus with id {}", id);
        }
    }

    @Transactional
    public TPStatusDto checkExistingTPstatus(Student student, TP tp) {
        Optional<TPStatus> existingStatus = repositoryTPStatus.findByStudentIdAndTpId(student.getId(), tp.getId());
        if (existingStatus.isPresent()) {
            return mapperTPStatus.toDto(existingStatus.get());
        } else {
            TPStatus newTPStatus = new TPStatus(student, tp, null);
            return mapperTPStatus.toDto(repositoryTPStatus.save(newTPStatus));
        }
    }

    @Transactional
    public TPStatus getOrCreateTPStatus(Student student, TP tp) {
        if (tp.getStatusStudents() == null) {
            tp.setStatusStudents(new HashSet<>());
        }

        // 1) Chercher dans la collection du TP
        return tp.getStatusStudents().stream()
                .filter(status -> status.getStudent() != null
                        && status.getStudent().getId() != null
                        && status.getStudent().getId().equals(student.getId()))
                .findFirst()
                .orElseGet(() -> {
                    // 2) Si pas trouvé -> créer, rattacher, et laisser le save(tp) persister
                    TPStatus newStatus = new TPStatus(student, tp, null);
                    tp.getStatusStudents().add(newStatus);
                    return newStatus;
                });
    }
}
