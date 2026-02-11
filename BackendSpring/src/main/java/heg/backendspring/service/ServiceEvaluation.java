package heg.backendspring.service;


import heg.backendspring.entity.Course;
import heg.backendspring.entity.Evaluation;
import heg.backendspring.entity.Submission;
import heg.backendspring.entity.TP;
import heg.backendspring.mapping.MapperEvaluation;
import heg.backendspring.models.EvaluationDto;
import heg.backendspring.models.TPDto;
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
import java.lang.annotation.Target;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
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
     * Ajouter une Submission à une Evaluation
     */
    @Transactional
    public EvaluationDto addSubmissionToEvaluation(Long evaluationId,  InputStream zipFile) {
        Optional<Evaluation> evalOpt = repositoryEvaluation.findById(evaluationId);
        if (evalOpt.isPresent()) {
            Evaluation eval = evalOpt.get();

            //nom du fichier
            String nomFichier = eval.getName() + "_RenduCyberlearn.zip";
            String codeCours = eval.getCourse().getCode();
            //chemin vers le fichier
            Path evalFolder = Paths.get(zipStoragePath, codeCours, eval.getName());

            //Chemin complet vers le fichier
            Path cheminVersZip = evalFolder.resolve(nomFichier);
            //Copier le stream
            try {
                if (zipFile == null) {
                    log.info("Zip file is null");
                }
                Files.copy(zipFile, cheminVersZip, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                log.error("Unable to copy zip file", e);
            }

            //Creer le rendu
            eval.setSubmission(new Submission(nomFichier, cheminVersZip.toString()
                    , null));
            repositoryEvaluation.save(eval);
            return mapperEvaluation.toDto(eval);
        }
        return null;
    }

    /**
     * Méthode permettant de lancer le traitement du rendu pour une Evaluation
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
        log.info("Starting zip process for course code={} eval name={}", course.getCode(), evaluation.getName());
        serviceSubmission.processZipSubmissionForEvaluation(course, evaluation);
    }
    /**
     * Méthode permettant de récupérer le fichier zip restructuré pour un TP donné
     */
    public File getSubmissionFileRestructurated(Long evaluationId) {
        Optional<Evaluation> evalOpt = repositoryEvaluation.findById(evaluationId);
        if (evalOpt.isEmpty()) {
            log.error("Evaluation with id {} not found for retrieving submission file", evaluationId);
            throw new EntityNotFoundException("Evaluation not found");
        }
        // Récupérer le rendu
        Submission submission = evalOpt.get().getSubmission();
        if (submission != null) {
            log.info("Path to restructurated file is not null : {}", submission.getPathFileStructured());
            Path pathFile = Paths.get(submission.getPathFileStructured());
            return pathFile.toFile();
        }
        return null;
    }



}
