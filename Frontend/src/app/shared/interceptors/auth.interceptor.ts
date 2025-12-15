import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';
import { environment } from '../../../environments/environment';

/**
 * Intercepteur HTTP fonctionnel pour ajouter le token Bearer
 * aux requêtes vers l'API backend
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
    const authService = inject(AuthService);

    // Ne pas ajouter le token pour les requêtes vers Keycloak
    if (req.url.includes(environment.keycloak.issuer)) {
        return next(req);
    }

    // Ajouter le token seulement si authentifié et si c'est une requête API
    const token = authService.getAccessToken();

    if (token && (req.url.startsWith(environment.apiUrl) || req.url.startsWith('/api'))) {
        const authReq = req.clone({
            setHeaders: {
                Authorization: `Bearer ${token}`
            }
        });
        return next(authReq);
    }

    return next(req);
};
