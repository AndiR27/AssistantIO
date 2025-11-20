package heg.backendspring.models;

public record TPStatusDto(
        Long id,
        Long studentId,
        Long tpId,
        boolean studentSubmission
) {
}
