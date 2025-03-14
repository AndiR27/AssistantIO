package org.acme.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import org.acme.models.CoursDTO;
import org.acme.service.ServiceCours;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

import java.util.List;

@Path("/cours")
public class CoursController {

    @Inject
    ServiceCours coursService;

    @POST
    @Path("/add")
    @APIResponse(
            responseCode = "200",
            description = "Add Cours")
    public CoursDTO addCours(CoursDTO cours) {
        return coursService.creerCours(cours);
    }

    @GET
    @Path("/list")
    public List<CoursDTO> listCours() {
        return coursService.listCours();
    }

}
