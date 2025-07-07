# AssistantIO : Fullstack App 

[![contributions welcome](https://img.shields.io/badge/contributions-welcome-brightgreen.svg?style=flat)](https://github.com/ashusharmatech/todo-quarkus-angular/issues)  
![Java CI with Maven](https://github.com/ashusharmatech/todo-quarkus-angular/workflows/Java%20CI%20with%20Maven/badge.svg)  
![Node.js CI](https://github.com/ashusharmatech/todo-quarkus-angular/workflows/Node.js%20CI/badge.svg)

---

## Description

Cette application fullstack est conçue avec **Quarkus** pour le backend (REST API), **Angular** pour le frontend (interface utilisateur)
et PostgreSQL pour la base de données.
Elle permet de gérer d'automatiser la gestion des cours, de ses étudiants et des rendus de travaux pratiques 
avec des opérations CRUD et une interface utilisateur simple mais efficace.
L'idée principale est de se débarrasser de la gestion de ses tâches via des tableaux excels et
de les gérer de manière centralisée.

---

## Technologies

### Backend (Quarkus)

- Java 17+
- Quarkus
- RESTEasy Reactive
- Hibernate ORM avec Panache
- JPA / PostgreSQL
- MapStruct
- Validation Jakarta
- Swagger (OpenAPI)
- JUnit, REST Assured

### Frontend (Angular)

- Angular 17+
- Angular Material
- RxJS
- SCSS
- Formulaires réactifs
- HttpClient
- Lazy loading
- Environnement dev/prod

---

## Prérequis

- **Java 17+**
- **Maven 3.8+**
- **Node.js 18+**
- **Angular CLI**
- **PostgreSQL** (ou H2 pour test)
- Docker (optionnel)

---

## Installation

### 1. Backend

```bash
cd backend
./mvnw clean install
./mvnw quarkus:dev
```

- Accès Swagger : http://localhost:8080/q/swagger-ui

### 2. Frontend

```bash
cd frontend
npm install
ng serve
```

- Accès Angular : http://localhost:4200/

---

## Structure du Projet

```text
todo-quarkus-angular/
│
├── backend/           → Projet Quarkus (Java)
│   ├── src/main/java/
│   ├── src/test/java/
│   ├── application.properties
│   └── pom.xml
│
├── frontend/          → Projet Angular
│   ├── src/app/
│   ├── angular.json
│   └── package.json
│
└── README.md
```

---

## Fonctionnalités

### Backend

- Création, mise à jour, suppression, consultation de tâches
- Architecture RESTful
- DTO + Mapping via MapStruct
- Tests unitaires et d’intégration
- Validation d’entrée

### Frontend

- Interface Material Design
- Ajout / modification / suppression de tâches
- Notification utilisateur
- Formulaires dynamiques avec validation
- Communication avec l’API via HttpClient
- Routing et composants modulaires

---

## Lancement avec Docker

> À configurer selon vos images préférées de PostgreSQL et front-end.

```bash
docker-compose up --build
```

---

## Tests

### Backend

```bash
./mvnw test
```

### Frontend

```bash
ng test
```

---

## Auteurs

- Andi R. – [Développeur Fullstack Java & Angular]

---

## Contribuer

Les contributions sont les bienvenues !

1. Fork du repo
2. Crée une branche : `git checkout -b feature/ton-idee`
3. Commit : `git commit -m 'Ajoute une nouvelle fonctionnalité'`
4. Push : `git push origin feature/ton-idee`
5. Crée une pull request

---

## Licence

Ce projet est sous licence MIT – voir le fichier `LICENSE` pour plus de détails.
