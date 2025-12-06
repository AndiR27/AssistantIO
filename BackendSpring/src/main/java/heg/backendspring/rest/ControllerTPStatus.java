package heg.backendspring.rest;

import heg.backendspring.api.TpStatusApi;
import heg.backendspring.enums.StudentSubmissionType;
import heg.backendspring.models.TPStatusDto;
import heg.backendspring.service.ServiceTPStatus;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.NativeWebRequest;

import java.util.Optional;

@RestController
@Slf4j
@RequiredArgsConstructor
public class ControllerTPStatus implements TpStatusApi {

    private final ServiceTPStatus serviceTPStatus;


    @Override
    public Optional<NativeWebRequest> getRequest() {
        return TpStatusApi.super.getRequest();
    }

    @Override
    public ResponseEntity<Void> deleteTPStatus(Long statusId) {
        serviceTPStatus.deleteTPStatus(statusId);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<TPStatusDto> getTPStatusById(Long statusId) {
        Optional<TPStatusDto> tpStatusDtoOpt = serviceTPStatus.findTPStatus(statusId);
        return tpStatusDtoOpt.map(ResponseEntity::ok).orElseThrow(() -> new EntityNotFoundException("TPStatus not found"));
    }

    @Override
    public ResponseEntity<TPStatusDto> updateTPStatus(Long statusId, String submissionType) {
        TPStatusDto tpStatusDto = serviceTPStatus.updateTPStatus(statusId, StudentSubmissionType.valueOf(submissionType));
        return ResponseEntity.ok(tpStatusDto);
    }

}
