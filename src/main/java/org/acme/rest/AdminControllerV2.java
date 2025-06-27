package org.acme.rest;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.enums.CourseType;
import org.acme.enums.SemesterType;
import org.acme.models.CourseDTO;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.acme.service.CourseService;

import java.util.ArrayList;
import java.util.List;

@Path("/admin/v2/courses")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AdminControllerV2 {

    @Inject
    CourseService courseService;

    //---------------------------
    // 1) Get Course by ID
    //---------------------------
    @Path("/{id}")
    @GET
    public CourseDTO getCourseById(@PathParam("id") Long id) {
        return courseService.findCours(id);
    }

    //---------------------------
    // 2) Get All Courses
    //---------------------------
    @Path("/all")
    @GET
    public List<CourseDTO> getAllCourses() {
        return courseService.listCours();
    }

    //-------------------
    // 3) Create a new course
    //-------------------
    @POST
    @Path("/addCourse")
    @Transactional
    @APIResponse(
            responseCode = "200",
            description = "Add Cours")
    public Response addCours(@Valid CourseDTO courseDTO) {

        CourseDTO course = courseService.creerCours(courseDTO);
        return Response.ok()
                .entity(course)
                .build();
    }

    //-------------------
    // 4) Update an existing course
    //-------------------
    @PUT
    @Path("/{id}")
    @APIResponse(
            responseCode = "200",
            description = "Update Course")
    public Response updateCourse(@PathParam("id") Long id, @Valid CourseDTO courseDTO) {
        CourseDTO updatedCourse = courseService.updateCourse(courseDTO);
        if (updatedCourse == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(updatedCourse).build();
    }

    //-------------------
    // 5) Delete a course
    //-------------------
    @DELETE
    @Path("/{id}")
    @APIResponse(
            responseCode = "204",
            description = "Delete Course")
    public Response deleteCourse(@PathParam("id") Long id) {
        boolean deleted = courseService.deleteCourse(id);
        if (!deleted) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.noContent().build();
    }



}
