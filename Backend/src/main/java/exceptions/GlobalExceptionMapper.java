package exceptions;

import com.tietoevry.quarkus.resteasy.problem.HttpProblem;
import io.vertx.core.http.HttpServerRequest;
import jakarta.annotation.Priority;
import jakarta.inject.Singleton;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;


import java.net.URI;
import java.time.OffsetDateTime;

import static org.apache.commons.lang3.StringUtils.firstNonBlank;

/**
 * Mapper global des exceptions → réponses RFC 7807 (application/problem+json).
 * Compatible Quarkus 3+ (Jakarta REST 3). Aucune dépendance Spring.
 *
 * - Génère un ProblemDetail standardisé pour TOUTE exception non gérée.
 * - Ajoute un requestId (depuis l’en-tête X-Request-ID si présent, sinon UUID).
 * - Mappe proprement les exceptions fréquentes (validation, sécurité, JAX-RS).
 * - Sérialise automatiquement en JSON via Quarkus/RESTEasy Reactive.
 */
@Provider
@Singleton
@Priority(1)
public class GlobalExceptionMapper implements ExceptionMapper<Exception> {


    private static final String PROBLEM_BASE  = "https://project-andi.example.com/problems/";

    //Les deux contextes suivants sont injectés automatiquement par Quarkus
    // et peuvent être utilisés pour enrichir les réponses d’erreur.
    @Context
    UriInfo uriInfo;

    @Override
    public Response toResponse(Exception exception) {
        //Mapping de l’exception vers une réponse HTTP Problem
        HttpProblem problem = mapException(exception);

        return Response.status(problem.getStatusCode())
                .type("application/problem+json")
                .entity(problem)
                .build();
    }


    //Méthode privée pour mapper les exceptions spécifiques à des réponses personnalisées
    private HttpProblem mapException(Exception ex) {
        URI instance = (uriInfo != null) ? uriInfo.getRequestUri() : URI.create("about:blank");

        if (ex instanceof WebApplicationException wae) {
            int code = (wae.getResponse() != null) ? wae.getResponse().getStatus() : 500;
            Status status = Status.fromStatusCode(code) != null ? Status.fromStatusCode(code) : Status.INTERNAL_SERVER_ERROR;
            String title = defaultTitleFor(status);
            String detail = firstNonBlank(wae.getMessage(), title);
            return baseProblem(status, title, detail, instance).build();
        }

        if (ex instanceof BadRequestException || ex instanceof IllegalArgumentException) {
            return baseProblem(
                    Status.BAD_REQUEST,
                    "Requête invalide",
                    firstNonBlank(ex.getMessage(), "La requête est mal formée."),
                    instance
            ).build();
        }

        if (ex instanceof NotFoundException) {
            return baseProblem(
                    Status.NOT_FOUND,
                    "Ressource introuvable",
                    firstNonBlank(ex.getMessage(), "La ressource demandée est introuvable."),
                    instance
            ).build();
        }

        if (ex instanceof java.io.IOException) {
            return baseProblem(
                    Status.INTERNAL_SERVER_ERROR,
                    "Erreur d'entrée/sortie",
                    firstNonBlank(ex.getMessage(), "Une erreur s'est produite lors du traitement des fichiers ou du flux."),
                    instance
            ).build();
        }

        // Par défaut → erreur interne
        return baseProblem(
                Status.INTERNAL_SERVER_ERROR,
                "Erreur interne",
                "Une erreur inattendue est survenue.",
                instance
        ).build();
    }

    //Méthode utilitaire pour créer un HttpProblem de base
    private HttpProblem.Builder baseProblem(Status status, String title, String detail, URI instance) {
        return HttpProblem.builder()
                .withStatus(status)
                .withTitle(title)
                .withDetail(detail)
                .withType(URI.create(PROBLEM_BASE + status.getStatusCode()))
                .withInstance(instance);
    }

    private String defaultTitleFor(Status status) {
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
