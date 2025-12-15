import { inject } from '@angular/core';
import { CanActivateFn } from '@angular/router';
import { AuthService } from '../services/auth.service';

/**
 * Guard fonctionnel pour protéger les routes
 * OAuth est déjà initialisé via APP_INITIALIZER avant que ce guard s'exécute
 */
export const authGuard: CanActivateFn = () => {
    const authService = inject(AuthService);

    // OAuth est déjà initialisé, on vérifie juste l'authentification
    if (authService.isAuthenticated()) {
        return true;
    }

    // Non authentifié - déclencher le flux de connexion Keycloak
    authService.login();
    return false;
};
