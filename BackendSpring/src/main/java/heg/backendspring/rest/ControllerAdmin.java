package heg.backendspring.rest;

import heg.backendspring.api.AdminCourseApi;
import heg.backendspring.service.ServiceCourse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ControllerAdmin implements AdminCourseApi {

    private final ServiceCourse serviceCourse;


}
