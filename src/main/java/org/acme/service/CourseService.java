package org.acme.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import org.acme.mapping.CourseMapper;
import org.acme.mapping.StudentMapper;
import org.acme.mapping.EvaluationMapper;
import org.acme.mapping.TPMapper;
import org.acme.models.*;
import org.acme.repository.*;
import org.acme.entity.*;
import org.acme.enums.*;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.jboss.logging.Logger;

@ApplicationScoped
public class CourseService {
    @Inject
    CourseRepository courseRepository;

    @Inject
    StudentRepository studentRepository;

    @Inject
    TPRepository travailPratiqueRepository;

    @Inject
    TPService TPService;

    @Inject
    SubmissionService submissionService;

    @Inject
    @ConfigProperty(name = "zip-storage.path")
    String zipStoragePath;

    @Inject
    CourseMapper courseMapper;
    @Inject
    StudentMapper studentMapper;
    @Inject
    TPMapper tpMapper;
    @Inject
    EvaluationMapper evaluationMapper;
    @Inject
    StudentService studentService;

    private static final Logger LOG = Logger.getLogger(CourseService.class);

    /**
     * Creation d'un cours
     * Le chemin vers un nouveau dépot pour les zips doit être créé
     */
    public CourseDTO creerCours(CourseDTO courseDTO) {
        Course course = courseMapper.toEntity(courseDTO);
        //Cours cours = new Cours(nom, code, TypeSemestre.valueOf(semestre), annee, TypeCours.valueOf(typeCours));
        courseRepository.persist(course);

        LOG.info("Course " + courseDTO.getNom() + " created");

        //Creation du repertoire servant de depot pour les zips grâce au code
        //On est dans un cours, donc le path est celui de base : "DocumentsZip/"
        creerDossierZip(course.code, zipStoragePath);
        return courseMapper.toDto(course);
    }

    /**
     * Methode permettant de retrouver un cours selon l'id
     **/
    public CourseDTO findCours(Long id) {
        Course course = courseRepository.findById(id);
        return courseMapper.toDto(course);
    }

    /**
     * Méthode permettant d'ajouter un étudiant spécifique existant à un cours existant
     */
    public StudentDTO ajouterEtudiant(CourseDTO courseDTO, StudentDTO studentDTO) {

        try {
            // Récupérer le cours, ou lever une NotFoundException si non présent
            Course course = courseRepository.findByIdOptional(courseDTO.getId())
                    .orElseThrow(() -> new NotFoundException("Cours non trouvé (ID=" + courseDTO.getId() + ")"));

            Student student;
            if (studentDTO.getId() != null) {
                // Cas : l'étudiant existe déjà
                student = studentRepository.findByIdOptional(studentDTO.getId())
                        .orElseThrow(() -> new NotFoundException("Étudiant non trouvé (ID=" + studentDTO.getId() + ")"));
            } else {
                // Cas : l'étudiant n'existe pas => on le crée
                StudentDTO studentDTO1 = studentService.addEtudiant(studentDTO);
                student = studentMapper.toEntity(studentDTO1);
            }
            // Établir la relation bidirectionnelle
            course.addEtudiant(student);
            student.addCours(course);

            // Persister en base
            courseRepository.persist(course);

            //etudiantRepository.persistAndFlush(etudiant);
            LOG.info("Etudiant " + studentDTO.getNom() + " added to course " + courseDTO.getNom());

            return studentMapper.toDto(student);

        } catch (NotFoundException e) {
            System.out.println("Erreur : " + e.getMessage());
        } catch (Exception e) {
            // Pour toute autre erreur imprévue
            System.out.print("Erreur inattendue lors de l'ajout de l'étudiant au cours" + e.getMessage());
        }

        return null;
    }

    /**
     * Methode permettant d'ajouter une liste d'étudiants à un cours
     * à partir d'un fichier TXT
     */
    @Transactional
    public void addAllStudentsFromFile(CourseDTO courseDTO, String[] data) {
        LOG.debugf("Starting addAllStudentsFromFile for course=%s, incomingRows=%d",
                courseDTO.getNom(), data.length);
        try {
            Course course = courseMapper.toEntity(courseDTO);
            //Cours cours = coursRepository.findById(idCours);
            List<Student> nouveauxStudents = new ArrayList<>();
            for (String row : data) {
                String[] etudiantData = row.split(";");
                //Etudiant etudiant = new Etudiant(etudiantData[0], etudiantData[1], TypeEtude.valueOf(etudiantData[2]));
                StudentDTO studentDTO = new StudentDTO(null, etudiantData[0], etudiantData[1], StudyType.valueOf(etudiantData[2]), new ArrayList<>());
                StudentDTO etuAdded = studentService.addEtudiant(studentDTO);
                ajouterEtudiant(courseDTO, etuAdded);

            }
            LOG.info("All students added to course " + courseDTO.getNom());
        } catch (RuntimeException e) {
            LOG.error("Failed to add students to course " + courseDTO.getNom() + "\n--> " + e.getMessage());
        }
    }

    /**
     * Methode permettant de récupérer tous les étudiants inscrits à un cours
     */
    public List<StudentDTO> getEtudiantsInscrits(Long idCours) {
        //Récupérer la liste des étudiants inscrits à un cours avec le repo et convertir en liste de DTO
        List<Student> students = courseRepository.findById(idCours).studentList;
        return students.stream()
                .map(studentMapper::toDto)
                .toList();
        //return coursRepository.findEtudiantsInscrits(idCours);
    }

    /**
     * Methode permettant d'ajouter un TP à un cours en le créant
     */
    public TP_DTO ajouterTP(CourseDTO courseDTO, int no) {
        TP_DTO tpDTO = null;
        try {
            Course course = courseRepository.findById(courseDTO.getId());
            TP tp = new TP(no, course, null);
            course.tpsList.add(tp);
            tp.course = course;
            //travailPratiqueRepository.persist(tp);
            courseRepository.persist(course);

            courseRepository.getEntityManager().flush();

            //Si un TP est créé, il faudra aussi y ajouter un dossier pour les rendus
            creerDossierZip("TP" + no, zipStoragePath + "/" + course.code);

            tpDTO = tpMapper.toDto(tp);
            LOG.info("TP " + no + " added to course " + courseDTO.getNom());

        } catch (NotFoundException e) {
            LOG.error("Failed to add TP " + no + " to course " + courseDTO.getNom() + "\n--> " + e.getMessage());
        }
        return tpDTO;
    }

    /**
     * Methode permettant de récupérer un TP selon son no (on suppose qu'un cours
     * a plusieurs TP, il sera plus facile de les retrouver par leur no sans chercher
     * dans toute la Database par les id)
     */
    public TP_DTO findTPByNumero(CourseDTO courseDTO, int no) {
        TP tp = courseRepository.findTpByNo(courseDTO.getId(), no);
        return tpMapper.toDto(tp);
    }


    /**
     * Methode permettant d'ajouter une évaluation à un cours en la créant : Examen
     */
    public ExamDTO ajouterExamen(CourseDTO courseDTO, ExamDTO examenDTO) {
        ExamDTO examDTO = null;
        try {
            Course course = courseRepository.findById(courseDTO.getId());
            Exam exam = evaluationMapper.toEntityExamen(examenDTO);
            exam.course = course;
            //Evaluation examen = new Examen(nom, date, cours, null, TypeSemestre.valueOf(semestre));
            course.evaluations.add(exam);
            courseRepository.persist(course);
            courseRepository.getEntityManager().flush();

            //Creation du repertoire servant de depot pour les zips grâce au nom
            creerDossierZip(exam.name, zipStoragePath + "/" + course.code);
            examDTO = evaluationMapper.toDtoExamen(exam);
            LOG.info("Examen " + examenDTO.getNom() + " added to course " + courseDTO.getNom());
        } catch (NotFoundException e) {
            LOG.error("Failed to add examen " + examenDTO.getNom() + "\n--> " + e.getMessage());
        }
        return examDTO;
    }

    /**
     * Methode permettant d'ajouter une évaluation à un cours en la créant : Controle
     * Continu
     */
    public ContinuousAssessmentDTO addCC(CourseDTO courseDTO, ContinuousAssessmentDTO ccDTO) {

        try {
            Course course = courseRepository.findById(courseDTO.getId());
            ContinuousAssessment cc = evaluationMapper.toEntityControleContinu(ccDTO);

            cc.course = course;
            course.evaluations.add(cc);
            courseRepository.persist(course);
            courseRepository.getEntityManager().flush();

            //Creation du repertoire servant de depot pour les zips grâce au nom : /CC1
            creerDossierZip(cc.name, zipStoragePath + "/" + course.code);

            LOG.info("CC " + ccDTO.getNom() + " added to course " + courseDTO.getNom());
            return evaluationMapper.toDtoControleContinu(cc);
        } catch (NotFoundException e) {
            LOG.error("Failed to add CC " + ccDTO.getNom() + "\n--> " + e.getMessage());
        }
        return null;
    }

    /**
     * Methode permettant de lancer le traitement du rendu pour un TP
     */
    public void lancerTraitementRenduZip(Long idCours, Long idTp) throws IOException {
        Course course = courseRepository.findById(idCours);
        TP tp = travailPratiqueRepository.findById(idTp);
        LOG.debug("Starting traitementRenduZip for course=" + course.name + ", TP no=" + tp.no);
        submissionService.traitementRenduZip(course, tp);
    }


    public List<CourseDTO> listCours() {
        //Récupérer la liste des cours avec le repo et convertir en liste de DTO avec stream
        return courseRepository.listAll().stream()
                .map(courseMapper::toDto)
                .toList();
    }

    //PRIVATE METHODS

    /**
     * Methode permettant de créer un dossier pour stocker les zips
     */
    private void creerDossierZip(String nomDossier, String path) {
        Path parentPath = Paths.get(path);

        Path newDirectory = parentPath.resolve(nomDossier);
        if (Files.notExists(newDirectory)) {
            try {
                Files.createDirectory(newDirectory);
            } catch (Exception e) {
                LOG.error("Failed to create directory " + newDirectory + "\n--> " + e.getMessage());
            }
        } else {
            LOG.info("Folder " + nomDossier + " already exists");
        }
    }


}
