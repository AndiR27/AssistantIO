package org.acme.rest;

import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.annotation.Resource;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import org.acme.models.*;
import org.acme.service.ServiceCours;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

import java.util.ArrayList;
import java.util.List;

@Path("/cours")
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
    @Produces(MediaType.TEXT_HTML)
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
    public TemplateInstance addEtudiant(
            @PathParam("id") Long id,
            @FormParam("nom") String nom,
            @FormParam("email") String email) {
        // 1. Construire l’objet EtudiantDTO (sans ID => nouvel étudiant)
        EtudiantDTO newEtd = new EtudiantDTO();
        newEtd.setNom(nom);
        newEtd.setEmail(email);
        // Optionnel : newEtd.setTypeEtude(TypeEtudeDTO.TEMPS_PLEIN); etc.

        // 2. Récupérer le cours et ajouter l’étudiant via le service
        CoursDTO coursDto = coursService.findCours(id);
        if (coursDto == null) {
            throw new NotFoundException("Cours non trouvé (ID=" + id + ")");
        }
        coursService.ajouterEtudiant(coursDto, newEtd);

        // 3. Renvoyer un fragment HTML qui affiche la liste mise à jour
        List<EtudiantDTO> etudiantsInscrits = coursService.getEtudiantsInscrits(id);
        return Templates.etudiants(etudiantsInscrits);
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

    // ------------------------------------
    // 4. Ajouter un TP à un cours
    // ------------------------------------
    @POST
    @Path("/{id}/addTP")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_HTML)
    @Transactional
    @APIResponse(
            responseCode = "200",
            description = "Ajout d'un TP à un cours"
    )
    public TemplateInstance addTP(
            @PathParam("id") Long id,
            @FormParam("numero") int numero) {
        // 1. Récupérer le cours
        CoursDTO coursDto = coursService.findCours(id);
        if (coursDto == null) {
            throw new NotFoundException("Cours non trouvé (ID=" + id + ")");
        }

        // 2. Ajouter le TP
        TravailPratiqueDTO newTp = coursService.ajouterTP(coursDto, numero);

        // 3. Rafraîchir le cours en base pour avoir la liste TPs à jour
        CoursDTO updatedCours = coursService.findCours(id);

        // 4. Renvoyer la page détaillée du cours mise à jour
        return Templates.cours_detail(updatedCours);
    }


    // -----------------------------------------------------
    // 4) Ajouter un Examen à un cours
    // -----------------------------------------------------
    @POST
    @Path("/{id}/addExamen")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_HTML)
    @Transactional
    @APIResponse(
            responseCode = "200",
            description = "Ajout d'un examen à un cours"
    )
    public TemplateInstance addExamen(
            @PathParam("id") Long id,
            @FormParam("nom") String nomExamen,
            @FormParam("date") String dateExamen, // ex: 2025-05-12
            @FormParam("semestre") String semestre
    ) {
        // 1) Récupérer le cours
        CoursDTO coursDto = coursService.findCours(id);
        if (coursDto == null) {
            throw new NotFoundException("Cours non trouvé (ID=" + id + ")");
        }

        // 2) Construire un ExamenDTO
        ExamenDTO examDTO = new ExamenDTO();
        examDTO.setNom(nomExamen);
        examDTO.setDate(dateExamen);           // ex: "2025-05-12"
        examDTO.setSemestre(TypeSemestreDTO.valueOf(semestre)); // PRINTEMPS / AUTOMNE ?
        // etc. selon tes champs

        // 3) Appeler le service
        coursService.ajouterExamen(coursDto, examDTO);

        // 4) Réafficher la page cours_detail
        CoursDTO updated = coursService.findCours(id);
        return Templates.cours_detail(updated);
    }

    // -----------------------------------------------------
    // 5) Ajouter un Contrôle Continu à un cours
    // -----------------------------------------------------
    @POST
    @Path("/{id}/addCC")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_HTML)
    @Transactional
    @APIResponse(
            responseCode = "200",
            description = "Ajout d'un Contrôle Continu à un cours"
    )
    public TemplateInstance addCC(
            @PathParam("id") Long id,
            @FormParam("nom") String nomCC,
            @FormParam("date") String dateCC,
            @FormParam("semestre") String semestre
    ) {
        // 1) Récupérer le cours
        CoursDTO coursDto = coursService.findCours(id);
        if (coursDto == null) {
            throw new NotFoundException("Cours non trouvé (ID=" + id + ")");
        }

        // 2) Construire un ControleContinuDTO
        ControleContinuDTO ccDTO = new ControleContinuDTO();
        ccDTO.setNom(nomCC);
        ccDTO.setDate(dateCC);
        //ccDTO.setSemestre(TypeSemestreDTO.valueOf(semestre)); // Ex: PRINTEMPS / AUTOMNE ?

        // 3) Appeler le service
        coursService.addCC(coursDto, ccDTO);

        // 4) Réafficher la page cours_detail
        CoursDTO updated = coursService.findCours(id);
        return Templates.cours_detail(updated);
    }
}


