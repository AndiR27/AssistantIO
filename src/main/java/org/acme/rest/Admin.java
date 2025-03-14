package org.acme.rest;

import io.quarkus.qute.*;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.acme.models.CoursDTO;
import org.acme.service.ServiceCours;

import java.util.List;

@Path("/admin")
public class Admin {

    @CheckedTemplate
    private static class Templates{
        public static native TemplateInstance home(List<CoursDTO> courses);

    }

    @Inject
    ServiceCours coursService;

    @GET
    @Path("/home")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance home() {
        return Templates.home(coursService.listCours());
    }
}
