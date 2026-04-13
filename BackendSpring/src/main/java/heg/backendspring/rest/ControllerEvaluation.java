package heg.backendspring.rest;

import heg.backendspring.api.EvaluationApi;
import heg.backendspring.models.EvaluationDto;
import heg.backendspring.models.TPDto;
import heg.backendspring.service.ServiceEvaluation;
import heg.backendspring.utils.FileUploadForm;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

@RestController
@Slf4j
@RequiredArgsConstructor
public class ControllerEvaluation implements EvaluationApi {

    private final ServiceEvaluation serviceEvaluation;

    @Override
    public Optional<NativeWebRequest> getRequest() {
        return EvaluationApi.super.getRequest();
    }

    @Override
    public ResponseEntity<EvaluationDto> createEvaluation(EvaluationDto evaluationDto) {
        return ResponseEntity.status(201).body(serviceEvaluation.createEvaluation(evaluationDto));
    }

    @Override
    public ResponseEntity<Void> deleteEvaluation(Long evaluationId) {
        serviceEvaluation.deleteEvaluation(evaluationId);
        return ResponseEntity.status(204).build();
    }

    @Override
    public ResponseEntity<Resource> downloadRestructuredSubmissionZipForEvaluation(Long evaluationId) {
        // Récupère le fichier zip restructuré
        File file = serviceEvaluation.getSubmissionFileRestructurated(evaluationId);

        if (file == null || !file.exists()) {
            throw new EntityNotFoundException(
                    "Aucun fichier restructuré trouvé pour l'evaluation " + evaluationId);
        }
        Resource resource = new FileSystemResource(file);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"")
                .contentLength(file.length())
                .contentType(MediaType.APPLICATION_OCTET_STREAM) // ou MediaType.valueOf("application/zip")
                .body(resource);
    }

    @Override
    public ResponseEntity<EvaluationDto> getEvaluationById(Long evaluationId) {
        Optional<EvaluationDto> evalDto = serviceEvaluation.getEvaluationById(evaluationId);
        return evalDto.map(ResponseEntity::ok).orElseThrow(() -> new EntityNotFoundException("Evaluation not found"));
    }

    @Override
    public ResponseEntity<Void> startEvaluationSubmissionZipProcess(Long evaluationId) {
        try {
            serviceEvaluation.startZipProcess(evaluationId);
            return ResponseEntity.accepted().build();
        } catch (IOException e) {
            return ResponseEntity.status(500).build();
        }
    }

    @Override
    public ResponseEntity<EvaluationDto> updateEvaluation(Long evaluationId, EvaluationDto evaluationDto) {
        return ResponseEntity.ok(serviceEvaluation.updateEvaluation(evaluationDto));
    }

    @Override
    public ResponseEntity<EvaluationDto> uploadSubmissionZipForEvaluation(Long evaluationId, MultipartFile file) {
        log.info("Received file: {}", file.getOriginalFilename());
        try {
            return ResponseEntity.status(201).body(serviceEvaluation.addSubmissionToEvaluation(evaluationId, FileUploadForm.getFileInputStream(file)));
        } catch (IOException e) {
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Upload d'un fichier mapping.txt pour une Evaluation en mode LOCAL_WS.
     * Le mapping est stocké à côté du zip de rendu et la Submission passe en mode LOCAL_WS.
     * Format attendu par ligne : "StudentName : HEG-WS-XXXX".
     */
    @PostMapping(
            value = "/evaluations/{evaluationId}/submission/mapping",
            consumes = {"multipart/form-data"},
            produces = {"application/json"}
    )
    public ResponseEntity<EvaluationDto> uploadMappingFileForEvaluation(
            @PathVariable("evaluationId") Long evaluationId,
            @RequestParam("file") MultipartFile file) {
        log.info("Received mapping file: {} for evaluation {}", file.getOriginalFilename(), evaluationId);
        try {
            EvaluationDto dto = serviceEvaluation.addMappingFileToEvaluation(
                    evaluationId, FileUploadForm.getFileInputStream(file));
            return ResponseEntity.status(201).body(dto);
        } catch (IOException e) {
            log.error("Erreur lors du traitement du mapping pour l'évaluation {}", evaluationId, e);
            return ResponseEntity.status(500).build();
        }
    }
}
