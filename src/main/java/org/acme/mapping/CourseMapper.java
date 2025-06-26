package org.acme.mapping;


import org.acme.entity.*;
import org.acme.enums.CourseType;
import org.acme.enums.SemesterType;
import org.acme.models.*;
import org.mapstruct.*;

/**
 * @Mapper(componentModel = "cdi") :
 * indique à MapStruct et à Quarkus que cette interface doit être gérée par le conteneur CDI.
 */
@Mapper(config = CentralConfig.class,
        uses = {StudentMapper.class, TPMapper.class})
public interface CourseMapper {
    /**
     * Convertit un Cours (entité) en CoursDto (DTO).
     */
    CourseDTO toDto(Course course);

    /**
     * Convertit un CoursDto (DTO) en Cours (entité).
     */
    Course toEntity(CourseDTO dto);

    /**
     * Met à jour un Cours (entité) à partir d'un CoursDto (DTO).
     */
    @Mapping(target = "id", ignore = true)
    void updateEntity(CourseDTO dto, @MappingTarget Course entity);

    //--- Méthodes par défaut pour les enums ---

    /**
     * Conversion TypeSemestre -> SemesterType
     */
    default SemesterType toDto(SemesterType semestre) {
        if (semestre == null) {
            return null;
        }
        // Supposons que SemesterType est un enum "miroir" de TypeSemestre
        return SemesterType.valueOf(semestre.name());
    }

    /**
     * Conversion SemesterType -> TypeSemestre
     */
    default SemesterType toEntity(SemesterType semestreDTO) {
        if (semestreDTO == null) {
            return null;
        }
        return SemesterType.valueOf(semestreDTO.name());
    }

    /**
     * Conversion TypeCours -> CourseType
     */
    default CourseType toDto(CourseType courseType) {
        if (courseType == null) {
            return null;
        }
        return CourseType.valueOf(courseType.name());
    }

    /**
     * Conversion CourseType -> TypeCours
     */
    default CourseType toEntity(CourseType typeCoursDTO) {
        if (typeCoursDTO == null) {
            return null;
        }
        return CourseType.valueOf(typeCoursDTO.name());
    }
}
