import {Component} from '@angular/core';

@Component({
  selector: 'app-header',
  standalone: true,
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.css'],
})

// Header est un composant Angular qui représente l'en-tête de l'application.
export class HeaderComponent{
  // Le titre de l'en-tête
  title = 'Assistant IO';

  // Constructeur du composant
  constructor() {

  }


}
