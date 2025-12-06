import { Component } from '@angular/core';
import { MatIcon } from '@angular/material/icon';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [MatIcon],
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.css'],
})

// Header est un composant Angular qui représente l'en-tête de l'application.
export class HeaderComponent {
  // Le titre de l'en-tête
  title = 'Assistant IO';

  // Constructeur du composant
  constructor() {

  }


}
