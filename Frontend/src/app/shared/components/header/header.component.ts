import { Component } from '@angular/core';
import { MatIcon } from '@angular/material/icon';
import { AuthService } from '../../services/auth.service';
import { NgIf } from '@angular/common';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [MatIcon, NgIf],
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.css'],
})

// Header est un composant Angular qui représente l'en-tête de l'application.
export class HeaderComponent {
  // Le titre de l'en-tête
  title = 'Assistant IO';

  constructor(private authService: AuthService) { }

  /** Vérifie si l'utilisateur est authentifié */
  get isAuthenticated(): boolean {
    return this.authService.isAuthenticated();
  }

  /** Retourne le nom de l'utilisateur connecté */
  get userName(): string {
    return this.authService.getUserName();
  }

  /** Déclenche la connexion */
  login(): void {
    this.authService.login();
  }

  /** Déconnecte l'utilisateur */
  logout(): void {
    this.authService.logout();
  }
}
