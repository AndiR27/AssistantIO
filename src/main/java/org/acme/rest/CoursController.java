package org.acme.rest;

import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.annotation.Resource;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import org.acme.models.CoursDTO;
import org.acme.models.EtudiantDTO;
import org.acme.models.TypeCoursDTO;
import org.acme.models.TypeSemestreDTO;
import org.acme.service.ServiceCours;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

import java.util.ArrayList;
import java.util.List;

@Path("/Cours")
public class CoursController {

    @CheckedTemplate
    private static class Templates {

        public static native TemplateInstance cours_detail(CoursDTO coursDTO);

        public static native TemplateInstance etudiants(List<EtudiantDTO> etudiants);
    }

    @Inject
    ServiceCours coursService;

    @GET
    @Path("/{id}")
    public TemplateInstance findCours(@PathParam("id") Long id) {
        return Templates.cours_detail(coursService.findCours(id));
    }

    //Ajouter un étudiant à un cours
    @POST
    @Path("/{id}/addEtudiant")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Transactional
    @APIResponse(
            responseCode = "200",
            description = "Add Etudiant")
    public TemplateInstance addEtudiant() {
        return null;
    }

    /**
     * Liste tous les étudiants
     */
    @GET
    @Path("/{id}/etudiants")
    public TemplateInstance etudiants(@PathParam("id") Long id) {
        List<EtudiantDTO> etudiantsInscrits = coursService.getEtudiantsInscrits(id);
        return Templates.etudiants(etudiantsInscrits);
    }
}
