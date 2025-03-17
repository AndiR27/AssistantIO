package org.acme.rest;

import io.quarkus.qute.*;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.acme.models.CoursDTO;
import org.acme.models.TypeCoursDTO;
import org.acme.models.TypeSemestreDTO;
import org.acme.service.ServiceCours;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

import java.util.ArrayList;
import java.util.List;

@Path("/admin")
public class Admin {

    @CheckedTemplate
    private static class Templates{
        public static native TemplateInstance home(List<CoursDTO> courses);
        public static native TemplateInstance cours_form(CoursDTO coursDTO);

    }

    @Inject
    ServiceCours coursService;

    @GET
    @Path("/home")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance home() {
        return Templates.home(coursService.listCours());
    }

    @POST
    @Path("/addCours")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Transactional
    @APIResponse(
            responseCode = "200",
            description = "Add Cours")
    public TemplateInstance addCours(@FormParam("nom") String nom,
                                     @FormParam("code") String code,
                                     @FormParam("semestre") String semestre,
                                     @FormParam("annee") int annee,
                                     @FormParam("prof") String prof,
                                     @FormParam("typeCours") String typeCours) {

        CoursDTO cours = new CoursDTO(null, nom, code, TypeSemestreDTO.valueOf(semestre), annee, prof, TypeCoursDTO.valueOf(typeCours), new ArrayList<>(),
                new ArrayList<>(), new ArrayList<>());
        return Templates.cours_form(coursService.creerCours(cours));
    }
}
