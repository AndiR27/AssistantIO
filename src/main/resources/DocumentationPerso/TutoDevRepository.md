# Quarkus - Gestion des Repositories

## 1️⃣ Qu'est-ce qu'un Repository ?
Un **Repository** permet d'abstraire l'accès aux données en gérant les entités via des opérations **CRUD** (Create, Read, Update, Delete). Il facilite l'organisation du code en séparant la logique métier de la persistance.

## 2️⃣ Utilisation d'un Repository avec EntityManager

### **Définition de l'entité**
```java
import jakarta.persistence.*;

@Entity
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    
    // Getters et Setters
}
```

### **Création d'un Repository personnalisé**
```java
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;

@ApplicationScoped
public class StudentRepository {
    
    @PersistenceContext
    EntityManager em;

    public void save(Student student) {
        em.persist(student);
    }

    public Student findById(Long id) {
        return em.find(Student.class, id);
    }

    public List<Student> findAll() {
        return em.createQuery("SELECT s FROM Student s", Student.class).getResultList();
    }

    public void delete(Student student) {
        em.remove(em.contains(student) ? student : em.merge(student));
    }
}
```

### **Utilisation dans un Service**
```java
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;

@ApplicationScoped
public class StudentService {
    
    @Inject
    StudentRepository repository;

    public void createStudent(String name) {
        Student student = new Student();
        student.setName(name);
        repository.save(student);
    }

    public List<Student> getAllStudents() {
        return repository.findAll();
    }
}
```

---

## 3️⃣ Utilisation d'un Repository avec PanacheEntityBase
Quarkus propose **PanacheEntityBase** pour simplifier la gestion des entités.

### **Définition de l'entité avec PanacheEntityBase**
```java
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

@Entity
public class Student extends PanacheEntityBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    public String name;
}
```

### **Création d'un Repository avec PanacheRepository**
```java
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class StudentRepository implements PanacheRepository<Student> {
}
```

### **Utilisation dans un Service**
```java
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;

@ApplicationScoped
public class StudentService {
    
    @Inject
    StudentRepository repository;

    public void createStudent(String name) {
        Student student = new Student();
        student.name = name;
        repository.persist(student);
    }

    public List<Student> getAllStudents() {
        return repository.listAll();
    }
}
```

---

## 4️⃣ Différences entre les deux approches
| Approche | Avantages | Inconvénients |
|----------|------------|----------------|
| **EntityManager** | Contrôle total sur les requêtes et transactions | Code plus verbeux |
| **PanacheRepository** | Code plus simple et intuitif | Moins de flexibilité sur les requêtes complexes |

---

## 5️⃣ Conclusion
- **EntityManager** convient pour un contrôle avancé sur la persistance.
- **PanacheRepository** est idéal pour une approche rapide et simple.
- **Quarkus** permet d'utiliser les deux méthodes selon les besoins du projet.

