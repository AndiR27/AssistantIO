package rest;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import models.*;
import rest.form.FileUploadForm;
import service.CourseService;
import service.TPService;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Path("/course/{courseId}")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CourseController {

    @PathParam("courseId")
    Long courseId;

    @Inject
    CourseService courseService;

    @Inject
    TPService tpService;

    private CourseDTO course() {
        // throws 404 if not found
        return courseService.findCourse(courseId);
    }

    @GET
    public CourseDTO getCourse() {
        return course();
    }

    /**
     * All methods that a Course can manage
     */

    //---------------------------
    // 1) Add Student to Course
    //---------------------------
    @Path("/addStudent")
    @POST
    @APIResponse(
            responseCode = "200",
            description = "Add Student to Course")
    public Response addStudent(@Valid StudentDTO studentDto) {
        //Récupérer le cours et ajouter l’étudiant via le service
        StudentDTO result = courseService.addStudentToCourse(course(), studentDto);

        return Response.ok()
                .entity(result)
                .build();
    }

    //---------------------------
    // 2) Get All Students in Course
    //---------------------------

    @Path("/students")
    @GET
    @APIResponse(
            responseCode = "200",
            description = "Get All Students in Course")
    public List<StudentDTO> getAllStudents() {
        //Récupérer le cours et retourner la liste des étudiants
        return courseService.getStudentsCourse(courseId);

    }

    //---------------------------
    // 3) Remove Student from Course
    //---------------------------
    @Path("/removeStudent/{studentId}")
    @DELETE
    @APIResponse(
            responseCode = "200",
            description = "Remove Student from Course")
    public Response removeStudent(@PathParam("studentId") Long studentId) {
        //Récupérer le cours et supprimer l’étudiant via le service
        courseService.deleteStudent(course(), studentId);
        return Response.noContent().build();
    }

    //---------------------------
    // 4) add Students with TXT file in Course
    //---------------------------
    @Path("/addStudentsFromFile")
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @APIResponse(
            responseCode = "200",
            description = "Add Students from File to Course")
    public Response addStudentsFromFile(@MultipartForm FileUploadForm form) {
        //Vérifier que le fichier est bien un fichier texte
        if (form == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("File is required")
                    .build();
        }
        //Récupérer le cours et ajouter les étudiants via le service
        try {
            courseService.addAllStudentsFromFile(course(), form.transformData());
            return Response.ok()
                    .entity(courseService.getStudentsCourse(courseId))
                    .build();
        } catch (IOException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("File could not be processed: " + e.getMessage())
                    .build();
        }

    }

    //---------------------------
    // 5) Add TP to Course (only param is the number of the TP)
    //---------------------------
    @Path("/addTP/{tpNumber}")
    @POST
    @APIResponse(
            responseCode = "200",
            description = "Add TP to Course")
    public Response addTP(@PathParam("tpNumber") int numTp) {
        TP_DTO newTp = courseService.addTP(course(), numTp);
        return Response.ok()
                .entity(newTp)
                .build();
    }

    //---------------------------
    // 7) Get All TPs in Course
    //---------------------------
    @Path("/tps")
    @GET
    @APIResponse(
            responseCode = "200",
            description = "Get All TPs in Course")
    public List<TP_DTO> getAllTPs() {
        return courseService.listTPs(course());
    }

    //---------------------------
    // 8) Add Exam to Course
    //---------------------------

    @Path("/addExam")
    @POST
    @APIResponse(
            responseCode = "200",
            description = "Add Exam to Course")
    public Response addExam(@Valid ExamDTO examDto) {
        ExamDTO newExam = courseService.addExam(course(), examDto);
        return Response.ok()
                .entity(newExam)
                .build();
    }

    //---------------------------
    // 9) Add CC to Course
    //---------------------------
    @Path("/addCC")
    @POST
    @APIResponse(
            responseCode = "200",
            description = "Add CC to Course")
    public Response addCC(@Valid ContinuousAssessmentDTO ccDto) {
        ContinuousAssessmentDTO newCC = courseService.addCC(course(), ccDto);
        return Response.ok()
                .entity(newCC)
                .build();
    }

    //---------------------------
    // 9) Get All Evaluations in Course
    //---------------------------
//    @Path("/evaluations")
//    @GET
//    @APIResponse(
//            responseCode = "200",
//            description = "Get All Evaluations in Course")
//    public List<EvaluationDTO> getAllEvaluations() {
//        return courseService.getAllEvaluations(course());
//    }

    //----------------------------
    // 10) Add a submission to a TP in the Course
    //----------------------------
    @Path("/addRendu/{tpNo}")
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @APIResponse(
            responseCode = "200",
            description = "Add a submission to a TP in the Course")
    public Response addSubmission(@PathParam("tpNo") int tpNo, @MultipartForm FileUploadForm form) {
        TP_DTO tp = courseService.findTPByNumero(course(), tpNo);
        TP_DTO tp_rendu = tpService.addSubmissionToTP(tp, form.getFile());
        return Response.ok()
                .entity(tp_rendu)
                .build();
    }

    //----------------------------
    // 11) Start process submission for a TP in the Course
    //----------------------------
    @Path("/startProcessSubmission/{tpNo}")
    @POST
    @APIResponse(
            responseCode = "200",
            description = "Start process submission for a TP in the Course")
    public Response startProcessSubmission(@PathParam("tpNo") int tpNo) {
        try {
            courseService.startZipProcess(courseId, tpNo);
            return Response.ok()
                    .entity("Submission processing started for TP number: " + tpNo)
                    .build();
        } catch (IOException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error processing submission: " + e.getMessage())
                    .build();
        }
    }

    //----------------------------
    // 12) Manage submissions for a TP in the Course
    //----------------------------
    @Path("/manageTP/{tpNo}")
    @POST
    @APIResponse(
            responseCode = "200",
            description = "Manage submissions for a TP in the Course")
    public Response manageSubmissions(@PathParam("tpNo") int tpNo) {
        TP_DTO tp_updated = tpService.manageSubmissionsTP(course(), tpNo);
        //manage errors :
        if (tp_updated == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("TP not found or no submissions to manage")
                    .build();
        }

        return Response.ok()
                .entity(tp_updated)
                .build();
    }

    //----------------------------
    // 13) Download restructured ZIP for a TP in the Course
    //----------------------------
    @Path("/downloadRestructuredZip/{tpNo}")
    @GET
    @Produces("application/zip")
    @APIResponse(
            responseCode = "200",
            description = "Download restructured ZIP for a TP in the Course")
    public Response downloadRestructuredZip(@PathParam("tpNo") int tpNo) {
        try {
            TP_DTO tp = courseService.findTPByNumero(course(), tpNo);
            if (tp == null || tp.getSubmission() == null || tp.getSubmission().getPathFileStructured() == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("TP or restructured ZIP not found")
                        .build();
            }
            File zipFile = tpService.getSubmissionFileRestructurated(tp);
            return Response.ok(zipFile)
                    .type("application/zip")
                    .header("Content-Disposition", "attachment; filename=\"" + zipFile.getName() + "\"")
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error downloading restructured ZIP: " + e.getMessage())
                    .build();
        }
    }



}
