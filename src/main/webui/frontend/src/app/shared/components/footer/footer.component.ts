import {Component} from '@angular/core';

@Component({
  selector: 'app-footer',
  standalone: true,
  templateUrl: './footer.component.html',
  styleUrls: ['./footer.component.css'],
})

// Footer est un composant Angular qui représente le pied de page de l'application.
export class FooterComponent {
  // Le titre du pied de page
  gitHubLink : string = "https://github.com/AndiR27"

  // Constructeur du composant
  constructor() {
    // Initialisation ou logique spécifique au pied de page peut être ajoutée ici
  }
}
