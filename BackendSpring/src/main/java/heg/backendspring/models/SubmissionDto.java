package heg.backendspring.models;

public record SubmissionDto(
        Long id,
        String fileName,
        String pathStorage,
        String pathFileStructured
) {
}
