import {Component, inject, Input} from '@angular/core';
import {CoursePreview} from '../../models/coursePreview.model';
import {HomeService} from '../../services/home.service';
import {CommonModule} from '@angular/common';
import {RouterLink} from '@angular/router';
import {take} from 'rxjs';
import {CoursFormComponent} from '../cours-form/cours-form.component';
import {HeaderComponent} from '../../../shared/components/header/header.component';
import {FooterComponent} from '../../../shared/components/footer/footer.component';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, CoursFormComponent, RouterLink, HeaderComponent, FooterComponent],
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css'],
})
//Pour les attributs
/**
 * On les définit par des propriétés de la classe.
 * @Input : Permet de recevoir des données depuis un composant parent (par exemple,
 * dans home.component.html, on peut utiliser <app-root [title]="TitreExemple"></app-root>).
 * @Output : Permet d'émettre des événements vers un composant parent.
 */
export class HomeComponent {
  // Propriété pour stocker le titre de la page d'accueil
  title = 'Page d\'accueil';
  // Propriété pour stocker une liste de course
  courseList : CoursePreview[] = []
  homeService = inject(HomeService); // Injection du service HomeService

  @Input() course!: CoursePreview;
  // Constructeur pour initialiser le composant
  constructor() {
    console.log('HomeComponent initialized');
    // Récupération des cours dès l'initialisation du composant
  }

  //Récupérer la liste des cours avec ngOnInit : cela permet de charger les données dès que le composant est initialisé.
  ngOnInit() {
    this.homeService.getCoursePreviews().subscribe((courseList) =>{this.courseList = courseList;});
  }



  // Méthode pour afficher un message de bienvenue
  getWelcomeMessage(): string {
    return 'test';
  }
}
