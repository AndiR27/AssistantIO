# Guide Complet : Annotations JPA et Relations

## Introduction
Ce fichier sert de **référence rapide** pour l'annotation des entités JPA avec Hibernate. Il couvre **les différentes annotations, les relations entre entités et leurs variantes**.

---

## 1️⃣ Définition d'une Entité
Chaque entité JPA est une classe mappée à une table en base de données.

### **Déclaration de base**
```java
import jakarta.persistence.*;

@Entity // Indique que cette classe est une entité JPA
@Table(name = "students") // Permet de personnaliser le nom de la table
public class Student {
```
📌 **Variantes** :
- `@Table(name = "nom_table")` : Définit un nom spécifique pour la table.
- `@Entity(name = "StudentEntity")` : Change l’identifiant utilisé par Hibernate.

---

## 2️⃣ Clé Primaire (`@Id` et `@GeneratedValue`)
Chaque entité doit avoir une clé primaire unique.

```java
    @Id // Définit la clé primaire
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
```
📌 **Stratégies possibles pour `@GeneratedValue`** :
- `GenerationType.IDENTITY` → Auto-incrémenté par la base (PostgreSQL, MySQL).
- `GenerationType.SEQUENCE` → Utilise une séquence SQL dédiée (Oracle, PostgreSQL).
- `GenerationType.AUTO` → Hibernate choisit la meilleure stratégie.
- `GenerationType.TABLE` → Stocke l’ID dans une table spéciale.

---

## 3️⃣ Colonnes (`@Column`)
Permet de définir les contraintes et propriétés d’un champ.

```java
    @Column(nullable = false, length = 100, unique = true)
    private String email;
```
📌 **Options disponibles** :
- `nullable = false` → Empêche les valeurs NULL.
- `length = 255` → Définit la taille maximale de la colonne.
- `unique = true` → Garantit l’unicité des valeurs.

---

## 4️⃣ Enums (`@Enumerated`)
Pour stocker des énumérations en base de données.

```java
    @Enumerated(EnumType.STRING) // Stocke "TEMPS_PLEIN", "TEMPS_PARTIEL"
    private TypeEtude studyType;
```
📌 **Deux modes possibles** :
- `EnumType.STRING` → Stocke les valeurs sous forme de texte (**recommandé**).
- `EnumType.ORDINAL` → Stocke la position de l’élément dans l’énum (**⚠️ Risqué en cas de modification**).

---

## 5️⃣ Relations entre Entités
### **OneToMany (`@OneToMany`)**
Relation **1 → N** : Un étudiant peut avoir plusieurs rendus.

```java
    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Submission> submissions;
```
📌 **Options** :
- `mappedBy = "student"` → Indique que l’autre entité gère la relation.
- `cascade = CascadeType.ALL` → Supprime les rendus si l’étudiant est supprimé.
- `fetch = FetchType.LAZY` → Charge les rendus **seulement quand nécessaire** (**recommandé**).

---
### **ManyToOne (`@ManyToOne`)**
Relation **N → 1** : Chaque submission appartient à un étudiant.

```java
    @ManyToOne
    @JoinColumn(name = "etudiant_id")
    private Student student;
```
📌 **Options** :
- `@JoinColumn(name = "colonne_foreign_key")` → Définit la colonne qui stocke la clé étrangère.

---
### **ManyToMany (`@ManyToMany`)**
Relation **N ↔ N** : Un étudiant peut être inscrit à plusieurs course.

```java
    @ManyToMany
    @JoinTable(
        name = "student_course",
        joinColumns = @JoinColumn(name = "student_id"),
        inverseJoinColumns = @JoinColumn(name = "course_id")
    )
    private List<Course> courses;
```
📌 **Options** :
- `@JoinTable` → Crée une table intermédiaire pour stocker les associations.
- `joinColumns = @JoinColumn(name = "student_id")` → Colonne qui stocke l’ID de l’étudiant.
- `inverseJoinColumns = @JoinColumn(name = "course_id")` → Colonne qui stocke l’ID du course.

---

## 6️⃣ Gestion des Relations (`cascade` et `fetch`)
### **CascadeType** (Propagation des opérations CRUD)
- `ALL` → Applique toutes les opérations CRUD.
- `PERSIST` → Sauvegarde en cascade.
- `REMOVE` → Supprime en cascade.
- `MERGE` → Met à jour en cascade.

### **FetchType** (Mode de chargement des relations)
- `EAGER` → Charge immédiatement la relation (⚠️ Peut être coûteux en mémoire).
- `LAZY` → Charge la relation uniquement quand elle est utilisée (**recommandé** pour `OneToMany`).

---

## 7️⃣ Constructeur, Getters et Setters
Un constructeur vide **est obligatoire** pour que JPA puisse instancier l'entité.

```java
    public Student() {}
```
Tous les champs doivent être accessibles via des **getters & setters**.

```java
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
```

---

## 8️⃣ Exemple d’Entité Complète (`Student.java`)
```java
@Entity
@Table(name = "students")
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100, unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    private TypeEtude studyType;

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Submission> submissions;

    @ManyToMany
    @JoinTable(
        name = "student_course",
        joinColumns = @JoinColumn(name = "student_id"),
        inverseJoinColumns = @JoinColumn(name = "course_id")
    )
    private List<Course> courses;

    public Student() {}
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
```
## 6️⃣ Différence entre `mappedBy` et `@JoinColumn`
La confusion entre `mappedBy` et `@JoinColumn` vient du fait qu’ils sont utilisés pour définir des relations entre entités, mais ils ont des rôles différents.

### **`mappedBy` (Côté inverse de la relation)**
- Utilisé **dans l'entité qui ne possède pas la clé étrangère**.
- Indique que **la relation est gérée par l’autre entité**.
- Ne crée pas de colonne supplémentaire en base de données.

**Exemple** :
```java
@Entity
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL)
    private List<Submission> submissions;
}
```
Dans la base de données : Seule la table submission contiendra une colonne student_id. Aucune colonne ne sera ajoutée dans student.

### **`@JoinColumn` (Côté propriétaire de la relation)**
Utilisé pour spécifier explicitement la clé étrangère.
Définit dans l'entité qui possède la relation.
Crée une colonne en base de données.

```java
@Entity
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "profile_id")
    private Profile profile;
}

```
---

## 📌 Conclusion
Ce fichier est une **référence rapide** pour la définition des entités JPA avec Hibernate. Il contient les **principales annotations, relations et options disponibles**.


# Gestion des Relations avec Hibernate Panache dans Quarkus

## 📌 Introduction
Ce document décrit les bonnes pratiques pour gérer les relations entre entités avec **Hibernate Panache** dans **Quarkus**. Il couvre les concepts de **cascade (`CascadeType`)** et de **chargement (`FetchType`)**.

## 📌 1️⃣ `CascadeType` – Contrôle les actions sur les entités liées
Le `CascadeType` définit comment les actions **CRUD** sur une entité affectent ses relations.

### 🔹 Types de `CascadeType`
| CascadeType      | Description |
|-----------------|-------------|
| **ALL**         | Applique tous les types de cascade (`PERSIST`, `MERGE`, `REMOVE`, etc.). |
| **PERSIST**     | Sauvegarde l'entité liée **automatiquement** quand l'entité principale est sauvegardée. |
| **MERGE**       | Met à jour l'entité liée quand l'entité principale est mise à jour. |
| **REMOVE**      | Supprime l'entité liée **quand l'entité principale est supprimée**. |
| **REFRESH**     | Rafraîchit l'entité liée quand l'entité principale est rafraîchie. |
| **DETACH**      | Détache l'entité liée quand l'entité principale est détachée du contexte de persistance. |

### 🔹 Exemple avec `CascadeType.ALL`
```java
@OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
public List<Submission> submissions;
```
👉 **Les `submissions` sont supprimées si l’étudiant est supprimé.**
👉 **Avec `orphanRemoval = true`, les `submissions` sans `student` sont automatiquement supprimées.**

---

## 📌 2️⃣ `FetchType` – Contrôle le chargement des entités liées
Le `FetchType` définit comment Hibernate récupère les entités liées en mémoire.

### 🔹 Différence entre `EAGER` et `LAZY`
| FetchType  | Description |
|-----------|-------------|
| **EAGER** | Charge **immédiatement** les entités liées. Peut ralentir l’application si volumineux. |
| **LAZY**  | Charge les entités **uniquement quand nécessaire**. **Recommandé pour optimiser la mémoire**. |

### 🔹 Exemple avec `FetchType.LAZY` (Recommandé pour `OneToMany` et `ManyToMany`)
```java
@OneToMany(mappedBy = "student", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
public List<Submission> submissions;
```
👉 **Hibernate ne charge PAS les `submissions` immédiatement**, elles sont chargées uniquement à la demande.

---

## 📌 3️⃣ Exemples de relations entre entités
### 🔹 One-To-Many (Un étudiant a plusieurs rendus)
```java
@Entity
public class Etudiant extends PanacheEntityBase {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    public List<Submission> submissions;
}
```

### 🔹 Many-To-One (Un submission appartient à un course)
```java
@Entity
public class Submission extends PanacheEntityBase {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    public Course course;
}
```

### 🔹 One-To-One (Un étudiant a un seul profil)
```java
@Entity
public class Etudiant extends PanacheEntityBase {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @OneToOne(mappedBy = "student", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    public Profile profile;
}
```

### 🔹 Many-To-Many (Un étudiant suit plusieurs course)
```java
@Entity
public class Etudiant extends PanacheEntityBase {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @JoinTable(
        name = "etudiant_course",
        joinColumns = @JoinColumn(name = "etudiant_id"),
        inverseJoinColumns = @JoinColumn(name = "course_id")
    )
    public List<Course> courses;
}
```

---

## 📌 4️⃣ Bonnes pratiques pour les relations en Quarkus
| Relation       | CascadeType recommandé             | FetchType recommandé |
|--------------|----------------------------------|----------------------|
| **One-To-Many** | `CascadeType.ALL` + `orphanRemoval = true` | `LAZY` |
| **Many-To-One** | Aucun cascade (géré manuellement) | `EAGER` |
| **One-To-One** | `CascadeType.ALL` si logique métier | `EAGER` |
| **Many-To-Many** | `CascadeType.PERSIST` ou `MERGE` | `LAZY` |

---

## 📌 5️⃣ Tester avec `cURL`
### 🔹 Ajouter un étudiant
```bash
curl -X POST http://localhost:8080/students \
     -H "Content-Type: application/json" \
     -d '{"nom": "Alice", "studyType": "temps plein"}'
```

### 🔹 Récupérer tous les étudiants
```bash
curl -X GET http://localhost:8080/students
```

### 🔹 Ajouter un course
```bash
curl -X POST http://localhost:8080/courses \
     -H "Content-Type: application/json" \
     -d '{"nom": "Quarkus Basics", "semestre": "Printemps", "annee": 2025}'
```

### 🔹 Récupérer tous les course
```bash
curl -X GET http://localhost:8080/courses
```

---



