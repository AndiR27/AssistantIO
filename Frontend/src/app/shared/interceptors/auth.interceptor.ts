import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';
import { MatSnackBar } from '@angular/material/snack-bar';
import { environment } from '../../../environments/environment';
import { catchError, throwError } from 'rxjs';

let isHandlingExpiry = false;
let expiryTimeout: ReturnType<typeof setTimeout> | null = null;

export const authInterceptor: HttpInterceptorFn = (req, next) => {
    const authService = inject(AuthService);
    const snackBar = inject(MatSnackBar);

    if (req.url.includes(environment.keycloak.issuer)) {
        return next(req);
    }

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
                    if (expiryTimeout) clearTimeout(expiryTimeout);
                    isHandlingExpiry = false;
                    expiryTimeout = null;
                    authService.login();
                });

                expiryTimeout = setTimeout(() => {
                    isHandlingExpiry = false;
                    expiryTimeout = null;
                    authService.login();
                }, 6500);
            }

            return throwError(() => error);
        })
    );
};
