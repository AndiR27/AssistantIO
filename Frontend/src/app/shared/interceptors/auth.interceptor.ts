import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';
import { MatSnackBar } from '@angular/material/snack-bar';
import { environment } from '../../../environments/environment';
import { catchError, throwError } from 'rxjs';

/** Prevents multiple "session expired" snackbars when several requests fail at once */
let isHandlingExpiry = false;

/**
 * Intercepteur HTTP fonctionnel pour ajouter le token Bearer
 * aux requêtes vers l'API backend, et gérer l'expiration de session (401)
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
    const authService = inject(AuthService);
    const snackBar = inject(MatSnackBar);

    // Ne pas ajouter le token pour les requêtes vers Keycloak
    if (req.url.includes(environment.keycloak.issuer)) {
        return next(req);
    }

    // Ajouter le token seulement si authentifié et si c'est une requête API
    const token = authService.getAccessToken();

    let request = req;
    if (token && (req.url.startsWith(environment.apiUrl) || req.url.startsWith('/api'))) {
        request = req.clone({
            setHeaders: {
                Authorization: `Bearer ${token}`
            }
        });
    }

    return next(request).pipe(
        catchError((error: HttpErrorResponse) => {
            if (error.status === 401 && !isHandlingExpiry) {
                isHandlingExpiry = true;

                snackBar.open('Session expirée — reconnexion nécessaire', 'Se connecter', {
                    duration: 6000,
                }).onAction().subscribe(() => {
                    authService.login();
                });

                // Auto-redirect after snackbar timeout if user doesn't click
                setTimeout(() => {
                    if (isHandlingExpiry) {
                        isHandlingExpiry = false;
                        authService.login();
                    }
                }, 6500);
            }

            return throwError(() => error);
        })
    );
};
