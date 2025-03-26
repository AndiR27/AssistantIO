package org.acme.rest;

import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import io.smallrye.config.ConfigMapping;
import io.vertx.ext.web.FileUpload;
import jakarta.annotation.Resource;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import org.acme.entity.Cours;
import org.acme.models.*;
import org.acme.rest.form.FileUploadForm;
import org.acme.service.ServiceCours;
import org.acme.service.ServiceRendu;
import org.acme.service.ServiceTravailPratique;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.jboss.logging.annotations.Param;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import org.jboss.resteasy.annotations.providers.multipart.PartType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Path("/cours")
public class CoursController {

    @CheckedTemplate
    private static class Templates {

        public static native TemplateInstance cours_detail(CoursDTO coursDTO);

        public static native TemplateInstance etudiants(List<EtudiantDTO> etudiants);

        public static native TemplateInstance rendu_form(Long coursId, TravailPratiqueDTO tp, boolean renduExiste);

        public static native TemplateInstance tpStatusList(Map<Long, TPStatusDTO> statusEtudiants);

    }

    @Inject
    ServiceCours coursService;

    @Inject
    ServiceTravailPratique tpService;

    @Inject
    ServiceRendu serviceRendu;

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
            @FormParam("email") String email,
            @FormParam("typeEtude") String typeEtude) {
        //Récupérer le cours et ajouter l’étudiant via le service
        CoursDTO coursDto = coursService.findCours(id);
        // 1. Construire l’objet EtudiantDTO (sans ID => nouvel étudiant)
        EtudiantDTO newEtd = new EtudiantDTO(null, nom, email, TypeEtudeDTO.valueOf(typeEtude),new ArrayList<>());

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

    @POST
    @Path("/{id}/ajoutEtudiantsTxt")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.TEXT_HTML)
    @Transactional
    @APIResponse(
            responseCode = "200",
            description = "Ajout d'étudiants à un cours à partir d'un fichier TXT"
    )
    public TemplateInstance ajoutEtudiantsTxt(
            @PathParam("id") Long id,
            @MultipartForm FileUploadForm form) {
        // 1. Récupérer le cours
        CoursDTO coursDto = coursService.findCours(id);
        if (form == null) {
            System.out.println("Erreur fichier manquant");
            //return (TemplateInstance) Response.status(Response.Status.BAD_REQUEST).entity("Fichier manquant").build();
            return Templates.cours_detail(coursDto).data("error", "Fichier manquant");

        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(form.getFile()))) {
            // Transformer en tableau de String
            String[] data = reader.lines().toArray(String[]::new);
            // 3. Ajouter les étudiants
            coursService.addAllStudentsFromFile(coursDto, data);

            // 4. Rafraîchir le cours en base pour avoir la liste d'étudiants à jour
            CoursDTO updatedCours = coursService.findCours(id);

            // 5. Renvoyer la page détaillée du cours mise à jour
            return Templates.cours_detail(updatedCours);
        } catch (Exception e) {
            return (TemplateInstance) Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Erreur lors de la lecture du fichier").build();
        }
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

    // Autres méthodes spécifiques au traitement des TPs
    /**
     * Créer un rendu pour un TP donné à travers ServiceTravailPratique
     * Note : Eventuellement par la suite, cette méthode pourrait être déplacée dans un autre contrôleur
     */
    @POST
    @Path("/{id_cours}/addRendu/{no_tp}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.TEXT_HTML)
    @Transactional
    @APIResponse(
            responseCode = "200",
            description = "Ajout d'un rendu pour un TP"
    )
    public TemplateInstance addRendu(@PathParam("id_cours") Long idCours,
                                     @PathParam("no_tp") int noTP,
                                     @MultipartForm FileUploadForm form) {
        // 1. Récupérer le cours et le TP
        CoursDTO coursDto = coursService.findCours(idCours);
        TravailPratiqueDTO tpDto = coursService.findTPByNumero(coursDto, noTP );
        if (tpDto == null) {
            throw new NotFoundException("Cours ou TP non trouvé (ID=" + idCours + ", " + noTP + ")");
        }

        // 2. Appeler le service pour ajouter le rendu
        tpService.creerRenduTP(tpDto, form.getFile());

        // 3. Rafraîchir le cours en base pour avoir la liste de rendus à jour
        CoursDTO updatedCours = coursService.findCours(idCours);

        // 4. Renvoyer la page détaillée du cours mise à jour
        return Templates.cours_detail(updatedCours);
    }

    /**
     * Ajouter un rendu zip à un TP
     */
    @POST
    @Path("/{id_cours}/TP/{no_tp}/addRenduZip")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.TEXT_HTML)
    @Transactional
    @APIResponse(
            responseCode = "200",
            description = "Ajout d'un rendu zip pour un TP"
    )
    public TemplateInstance addRenduZip(@PathParam("id_cours") Long idCours,
                            @PathParam("no_tp") int noTP,
                            @MultipartForm FileUploadForm form) {
        // 1. Récupérer le cours et le TP
        CoursDTO coursDto = coursService.findCours(idCours);
        TravailPratiqueDTO tpDto = coursService.findTPByNumero(coursDto, noTP );
        if (tpDto == null) {
            throw new NotFoundException("Cours ou TP non trouvé (ID=" + idCours + ", " + noTP + ")");
        }

        // 2. Appeler le service pour ajouter le rendu
        TravailPratiqueDTO tpDTO = tpService.creerRenduTP(tpDto, form.getFile());
        CoursDTO coursDTO = coursService.findCours(idCours);
        return Templates.rendu_form(coursDTO.getId(), tpDTO, true);

    }

    /**
     * Lancer le processus de traitement du ZIP rendu pour le formater correctement
     * /cours/{coursId}/TP/{tp.no}/traitementZip/{tp.rendu.id}
     */
    @PUT
    @Path("/{id_cours}/TP/{no_Tp}/traitementZip")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance traitementZip(@PathParam("id_cours") Long idCours,
                                          @PathParam("no_Tp") int noTP) throws IOException {
        //Trouver le cours et le TP
        CoursDTO coursDTO = coursService.findCours(idCours);
        TravailPratiqueDTO tpDTO = coursService.findTPByNumero(coursDTO, noTP);

        // Appeler le service pour traiter le rendu
        RenduDTO renduDTO = coursService.lancerTraitementRenduZip(idCours, tpDTO.getId());

        // Rafraîchir le cours en base pour avoir la liste de rendus à jour
        CoursDTO updatedCours = coursService.findCours(idCours);
        TravailPratiqueDTO updatedTpDTO = tpService.findTravailPratique(tpDTO.getId());

        //Quand le traitement est terminé, on voudrait pouvoir lancer la gestion des rendus



        // Renvoyer la page détaillée du cours mise à jour
        return Templates.rendu_form(updatedCours.getId(), updatedTpDTO, true);
    }

    @GET
    @Path("/{id_cours}/TP/{tp_no}/downloadZip/{id_rendu}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response downloadZipTraite(@PathParam("id_cours") Long idCours,
                                              @PathParam("tp_no") int tpNo,
                                              @PathParam("id_rendu") Long idRendu) throws IOException {
        // 1. Récupérer le cours et le TP
        CoursDTO coursDto = coursService.findCours(idCours);
        TravailPratiqueDTO tpDto = coursService.findTPByNumero(coursDto, tpNo );
        if (tpDto == null) {
            throw new NotFoundException("Cours ou TP non trouvé (ID=" + idCours + ", " + tpNo + ")");
        }

        // 2. Récupérer le rendu
        RenduDTO renduDTO = tpDto.getRendu();
        if (renduDTO == null || renduDTO.getCheminFichierStructure() == null) {
            throw new NotFoundException("Aucun rendu traité n'est disponible pour ce TP.");
        }

        // Construire la réponse de téléchargement
        // On ouvre un InputStream depuis le Path
        InputStream inputStream = Files.newInputStream(Paths.get(renduDTO.getCheminFichierStructure()));
        String nomFichier = Paths.get(renduDTO.getCheminFichierStructure()).getFileName().toString(); // ex: "TP2_RenduRestructuration.zip"

        // On précise le content type (zip) et on inclut le header Content-Disposition pour le "download"
        return Response.ok(inputStream, MediaType.APPLICATION_OCTET_STREAM)
                .header("Content-Disposition", "attachment; filename=\"" + nomFichier + "\"")
                .build();
    }


}


