package heg.backendspring.service;

import heg.backendspring.entity.Course;
import heg.backendspring.entity.Evaluation;
import heg.backendspring.entity.Submission;
import heg.backendspring.enums.SubmissionMode;
import heg.backendspring.mapping.MapperEvaluation;
import heg.backendspring.models.EvaluationDto;
import heg.backendspring.repository.RepositoryEvaluation;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;


@Slf4j
@Service
@RequiredArgsConstructor
public class ServiceEvaluation {

    @Value("${zip-storage.path}")
    private String zipStoragePath;

    private final RepositoryEvaluation repositoryEvaluation;
    private final MapperEvaluation mapperEvaluation;
    private final ServiceSubmission serviceSubmission;
    private final ServiceSubmissionLocalWS serviceSubmissionLocalWS;
    private final SubmissionHandler submissionHandler;

    //==============================
    //    CRUD EVALUATION METHODS
    //==============================

    public Optional<EvaluationDto> getEvaluationById(Long id){
        return repositoryEvaluation.findById(id).map(mapperEvaluation::toDto);
    }

    public List<EvaluationDto> getAllEvaluations(){
        return repositoryEvaluation.findAll().stream().map(mapperEvaluation::toDto).toList();
    }

    public List<EvaluationDto> getAllEvaluationsFromCourse(Long idCourse) {
        return repositoryEvaluation.findAllByCourseId(idCourse).stream().map(mapperEvaluation::toDto).toList();
    }

    @Transactional
    public EvaluationDto createEvaluation(EvaluationDto evaluationDto){
        Evaluation evaluation = repositoryEvaluation.save(mapperEvaluation.toEntity(evaluationDto));
        log.info("Created evaluation {} with id: {}", evaluationDto.name(), evaluation.getId());
        return mapperEvaluation.toDto(evaluation);
    }

    public EvaluationDto updateEvaluation(EvaluationDto evaluationDto) {
        Optional<Evaluation> existingEvaluationOpt = repositoryEvaluation.findById(evaluationDto.id());
        if (existingEvaluationOpt.isPresent()) {
            Evaluation existingEvaluation = existingEvaluationOpt.get();
            mapperEvaluation.updateEntityFromDto(evaluationDto, existingEvaluation);
            Evaluation updatedEvaluation = repositoryEvaluation.save(existingEvaluation);
            log.info("Updated evaluation {} with id: {}", evaluationDto.name(), updatedEvaluation.getId());
            return mapperEvaluation.toDto(updatedEvaluation);
        } else {
            log.error("Evaluation with id {} not found for update", evaluationDto.id());
            throw new EntityNotFoundException("Evaluation not found");
        }
    }

    public void deleteEvaluation(Long id){
        if(repositoryEvaluation.existsById(id)){
            repositoryEvaluation.deleteById(id);
            log.info("Deleted evaluation with id: {}", id);
        }
    }


    /**
     * Ajoute une Submission (fichier ZIP) à une Evaluation.
     * Utilise SubmissionHandler pour éviter la duplication de code.
     */
    @Transactional
    public EvaluationDto addSubmissionToEvaluation(Long evaluationId, InputStream zipFile) {
        Optional<Evaluation> evalOpt = repositoryEvaluation.findById(evaluationId);
        if (evalOpt.isPresent()) {
            Evaluation eval = evalOpt.get();
            // Déléguer la logique commune à SubmissionHandler
            submissionHandler.addSubmission(eval, zipFile);
            repositoryEvaluation.save(eval);
            return mapperEvaluation.toDto(eval);
        }
        return null;
    }

    /**
     * Méthode permettant de lancer le traitement du rendu pour une Evaluation.
     * Route vers le mode CLASSIC ou LOCAL_WS en fonction du mode défini sur l'Evaluation.
     */
    @Transactional
    public void startZipProcess(Long evaluationId) throws IOException {
        Optional<Evaluation> evalOpt = repositoryEvaluation.findById(evaluationId);
        if (evalOpt.isEmpty()) {
            log.error("Evaluation with id {} not found", evaluationId);
            throw new EntityNotFoundException("Evaluation not found");
        }
        Evaluation evaluation = evalOpt.get();
        Course course = evaluation.getCourse();

        SubmissionMode mode = (evaluation.getMode() != null)
                ? evaluation.getMode()
                : SubmissionMode.CLASSIC;

        log.info("Starting zip process (mode={}) for course code={} eval name={}",
                mode, course.getCode(), evaluation.getName());

        if (mode == SubmissionMode.LOCAL_WS) {
            serviceSubmissionLocalWS.processZipSubmissionForEvaluation(course, evaluation);
        } else {
            serviceSubmission.processZipSubmissionForEvaluation(course, evaluation);
        }
    }

    /**
     * Attache un fichier mapping.txt à la Submission de l'Evaluation.
     * Le mode LOCAL_WS doit déjà être configuré sur l'Evaluation.
     */
    @Transactional
    public EvaluationDto addMappingFileToEvaluation(Long evaluationId, InputStream mappingFile) {
        Evaluation eval = repositoryEvaluation.findById(evaluationId)
                .orElseThrow(() -> new EntityNotFoundException("Evaluation not found with id: " + evaluationId));
        if (eval.getSubmission() == null) {
            throw new EntityNotFoundException(
                    "Aucune soumission pour l'évaluation " + evaluationId + " — envoyer le zip d'abord.");
        }
        submissionHandler.addMappingFile(eval, mappingFile);
        repositoryEvaluation.save(eval);
        log.info("Mapping ajouté pour l'évaluation {} (mode {})", evaluationId, eval.getMode());
        return mapperEvaluation.toDto(eval);
    }
    /**
     * Récupère le fichier ZIP restructuré pour une Evaluation.
     * Utilise SubmissionHandler pour la logique commune.
     */
    public File getSubmissionFileRestructurated(Long evaluationId) {
        Evaluation eval = repositoryEvaluation.findById(evaluationId)
                .orElseThrow(() -> new EntityNotFoundException("Evaluation not found with id: " + evaluationId));
        return submissionHandler.getRestructuredFile(eval);
    }



}
