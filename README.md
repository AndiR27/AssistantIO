# AssistantIO — Fullstack Application (Spring Boot + Angular)

<p align="center">
   <img src="https://img.shields.io/github/v/tag/AndiR27/AssistantIO?label=version&style=for-the-badge" />
  <img src="https://img.shields.io/badge/Spring%20Boot-3.3+-6DB33F?style=for-the-badge&logo=springboot&logoColor=white"/>
  <img src="https://img.shields.io/badge/Angular-17+-DD0031?style=for-the-badge&logo=angular&logoColor=white"/>
  <img src="https://img.shields.io/badge/PostgreSQL-15+-4169E1?style=for-the-badge&logo=postgresql&logoColor=white"/>
  <img src="https://img.shields.io/badge/Docker-Ready-2496ED?style=for-the-badge&logo=docker&logoColor=white"/>
  <img src="https://img.shields.io/badge/GitHub_Actions-CI/CD-2088FF?style=for-the-badge&logo=githubactions&logoColor=white"/>
  <img src="https://img.shields.io/badge/Architecture-Fullstack-9C27B0?style=for-the-badge&logo=stackshare&logoColor=white"/>
</p>

---

## 📝 Description

**AssistantIO** est une plateforme complète de gestion académique développée en **Spring Boot**, **Angular** et **PostgreSQL**.  
Elle répond à un besoin concret : remplacer les suivis pédagogiques traditionnels basés sur des fichiers Excel par une solution centralisée, automatisée et fiable.

Conçue pour les établissements d'enseignement supérieur, l'application permet :

- de gérer les étudiants, les cours et les travaux pratiques (TP) de manière structurée ;
- d'importer facilement des listes d'étudiants ;
- de traiter automatiquement les rendus d’étudiants issus de plateformes externes (ex. Cyberlearn) ;
- de restructurer des archives ZIP / 7z et de générer des projets de correction unifiés ;
- de suivre l’avancement et la complétude des rendus par étudiant.

AssistantIO a été pensé comme un outil **moderne, maintenable et extensible**, respectant les standards d’architecture logicielle (layers, DTOs, mappers, services) et intégrant des tests automatisés.  
Il constitue une base robuste pour toute institution souhaitant moderniser ses flux pédagogiques.

---

## 🛠️ Technologies

### Backend — Spring Boot
- Java 21+
- Spring Boot 3.x
- Spring Web (REST)
- Spring Data JPA / Hibernate
- PostgreSQL
- MapStruct
- Jakarta Validation
- Swagger / OpenAPI
- JUnit 5 / Mockito

### Frontend — Angular
- Angular 17+
- Angular Material
- RxJS
- SCSS
- Lazy loading
- HttpClient

---

## 🚀 Lancement du projet

### Backend Spring Boot

```bash
cd BackendSpring
mvn clean install
mvn spring-boot:run
```

API disponible sur :  
http://localhost:8088  
Swagger UI : http://localhost:8088/swagger-ui/index.html

---

### Frontend Angular

```bash
cd FrontendAngular
npm install
ng serve
```

UI disponible sur :  
http://localhost:4200/

---

## 🧱 Architecture du Projet

```text
AssistantIO/
│
├── BackendSpring/       → API REST (Spring Boot)
│   ├── controllers/
│   ├── services/
│   ├── repositories/
│   ├── entities/
│   ├── dto/
│   ├── mappers/
│   └── utils/
│
├── FrontendAngular/     → Interface Web (Angular)
│   ├── components/
│   ├── services/
│   ├── models/
│   └── pages/
│
└── README.md
```

---

## 🎯 Fonctionnalités clés 

### Gestion académique centralisée
- Cours, étudiants et travaux pratiques structurés.
- Importation massive via fichiers TXT/CSV.
- Export consolidé pour suivi administratif.

### Automatisation des rendus étudiants
- Upload des archives Cyberlearn (ZIP + 7z embarqués).
- Extraction intelligente par étudiant.
- Reconstruction de projets Java/Python uniformisés.
- Génération automatique d’un ZIP de correction.

### Supervision pédagogique
- Statut des rendus par étudiant (avec code couleur).
- Statistiques et contrôle qualité intégrés.
- Interface moderne et rapide (Angular Material).

### Fiabilité & Maintenabilité
- Architecture en couches.
- Mapping propre via MapStruct.
- Validation et gestion d’erreurs standardisées.
- Tests unitaires avancés (services + ZipUtils).

---

## 🧪 Tests

### Backend
```bash
mvn test
```

### Frontend
```bash
ng test
```

---

## 👤 Auteur

**Andi R.**  
Développeur Fullstack Java · Angular  
Passionné par l’architecture logicielle, l’automatisation et les solutions pédagogiques modernes.

---

## 🤝 Contribution

1. Fork du repo
2. Crée une branche :
   ```bash
   git checkout -b feature/ma-feature
   ```
3. Commit :
   ```bash
   git commit -m "Nouvelle fonctionnalité"
   ```
4. Push :
   ```bash
   git push origin feature/ma-feature
   ```
5. Ouvre une Pull Request 🚀

---

## 📄 Licence

Projet sous licence **MIT**.

