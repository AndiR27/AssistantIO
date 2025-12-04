package heg.backendspring.exception;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.net.URI;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final String PROBLEM_BASE = "https://project-andi.example.com/problems/";


    /**
     * Handler global pour TOUTES les exceptions non gérées ailleurs.
     * Retourne un ProblemDetail RFC 7807 (application/problem+json).
     */
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleException(Exception ex, HttpServletRequest request) {
        ProblemDetail problem = mapException(ex, request); // ici mapException retourne directement ProblemDetail
        return problem;
    }

    /**
     * Equivalent de ta méthode mapException(...) en Quarkus.
     * On retourne ici un "wrapper" contenant le HttpStatus + le ProblemDetail.
     */
    private ProblemDetail mapException(Exception ex,
                                       jakarta.servlet.http.HttpServletRequest request) {

        URI instance = buildInstanceUri(request);

        // 1) Exceptions Spring "avec statut" (équivalent de WebApplicationException)
        if (ex instanceof ResponseStatusException rse) {
            HttpStatus status = HttpStatus.resolve(rse.getStatusCode().value());
            if (status == null) {
                status = HttpStatus.INTERNAL_SERVER_ERROR;
            }
            String title = defaultTitleFor(status);
            String detail = firstNonBlank(rse.getReason(), title);

            return baseProblem(status, title, detail, instance);
        }

//        // 2) Équivalent de WebApplicationException côté Jakarta/JAX-RS
//        if (ex instanceof WebApplicationException wae) {
//            int code = (wae.getResponse() != null) ? wae.getResponse().getStatus() : 500;
//            HttpStatus status = HttpStatus.resolve(code);
//            if (status == null) {
//                status = HttpStatus.INTERNAL_SERVER_ERROR;
//            }
//            String title = defaultTitleFor(status);
//            String detail = firstNonBlank(wae.getMessage(), title);
//
//            ProblemDetail problem = baseProblem(status, title, detail, instance);
//            return new ProblemWithStatus(status, problem);
//        }

        // 3) Requête invalide (400) : BadRequest, IllegalArgumentException, validation, JSON mal formé, etc.
        if (ex instanceof BadRequestException
                || ex instanceof IllegalArgumentException
                || ex instanceof HttpMessageNotReadableException
                || ex instanceof MethodArgumentNotValidException
                || ex instanceof ConstraintViolationException) {

            HttpStatus status = HttpStatus.BAD_REQUEST;
            return baseProblem(
                    status,
                    "Requête invalide",
                    firstNonBlank(ex.getMessage(), "La requête est mal formée."),
                    instance
            );

        }

        // 4) Ressource introuvable (404)
        if (ex instanceof EntityNotFoundException) {
            HttpStatus status = HttpStatus.NOT_FOUND;
            return baseProblem(
                    status,
                    "Ressource introuvable",
                    firstNonBlank(ex.getMessage(), "La ressource demandée est introuvable."),
                    instance
            );
        }

        // 5) Erreur I/O (500)
        if (ex instanceof IOException) {
            HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
            return baseProblem(
                    status,
                    "Erreur d'entrée/sortie",
                    firstNonBlank(ex.getMessage(), "Une erreur s'est produite lors du traitement des fichiers ou du flux."),
                    instance
            );

        }

        // 6) Erreur liée à la gestion des Courses : CourseException
        if (ex instanceof CourseException ce) {
            HttpStatus status;
            String detailMessage = firstNonBlank(ce.getMessage(), "Une erreur liée au cours est survenue.");
            switch (ce.getErrorCode()) {
                case COURSE_NOT_FOUND:
                    status = HttpStatus.NOT_FOUND;
                    break;
                case TP_ALREADY_EXISTS:
                case STUDENT_ALREADY_IN_COURSE:
                case INVALID_COURSE_CODE:
                case OPERATION_NOT_ALLOWED:
                    status = HttpStatus.BAD_REQUEST;
                    break;
                case GENERIC_COURSE_ERROR:
                default:
                    status = HttpStatus.INTERNAL_SERVER_ERROR;
                    break;
            }
            return baseProblem(
                    status,
                    defaultTitleFor(status),
                    detailMessage,
                    instance
            );
        }

        // 7) Par défaut → erreur interne (500)
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        return baseProblem(
                status,
                "Erreur interne",
                "Une erreur inattendue est survenue.",
                instance
        );

    }

    /**
     * Crée un ProblemDetail "de base" (équivalent de baseProblem(...) en Quarkus/HttpProblem).
     */
    private ProblemDetail baseProblem(HttpStatus status,
                                      String title,
                                      String detail,
                                      URI instance) {

        ProblemDetail problem = ProblemDetail.forStatus(status);
        problem.setTitle(title);
        problem.setDetail(detail);
        problem.setType(URI.create(PROBLEM_BASE + status.value()));
        problem.setInstance(instance);

        return problem;
    }

    private URI buildInstanceUri(jakarta.servlet.http.HttpServletRequest request) {
        if (request == null) {
            return URI.create("about:blank");
        }
        String url = request.getRequestURL().toString();
        String query = request.getQueryString();
        if (query != null && !query.isBlank()) {
            url = url + "?" + query;
        }
        return URI.create(url);
    }

    private String defaultTitleFor(HttpStatus status) {
        return switch (status) {
            case BAD_REQUEST -> "Requête invalide";
            case UNAUTHORIZED -> "Non authentifié";
            case FORBIDDEN -> "Accès interdit";
            case NOT_FOUND -> "Ressource introuvable";
            case METHOD_NOT_ALLOWED -> "Méthode HTTP non autorisée";
            case NOT_ACCEPTABLE -> "Format non acceptable";
            case CONFLICT -> "Conflit";
            case UNSUPPORTED_MEDIA_TYPE -> "Type de contenu non supporté";
            default -> "Erreur";
        };
    }

    private String firstNonBlank(String s, String fallback) {
        return (s == null || s.isBlank()) ? fallback : s;
    }


}


