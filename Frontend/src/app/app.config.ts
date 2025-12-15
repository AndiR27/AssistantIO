import { ApplicationConfig, APP_INITIALIZER, provideBrowserGlobalErrorListeners, provideZoneChangeDetection } from '@angular/core';
import { provideRouter } from '@angular/router';

import { routes } from './app.routes';
import { provideClientHydration, withEventReplay } from '@angular/platform-browser';
import { provideHttpClient, withFetch, withInterceptors } from '@angular/common/http';
import { provideOAuthClient } from 'angular-oauth2-oidc';
import { authInterceptor } from './shared/interceptors/auth.interceptor';
import { AuthService } from './shared/services/auth.service';

/**
 * Factory pour initialiser OAuth avant le démarrage de l'app
 * Cela garantit que le token est traité avant que les guards ne s'exécutent
 */
function initializeOAuth(authService: AuthService): () => Promise<boolean> {
  return () => authService.runInitialAuth();
}

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideHttpClient(
      withFetch(),
      withInterceptors([authInterceptor])
    ),
    provideOAuthClient(),
    // APP_INITIALIZER s'exécute AVANT que l'app ne démarre
    {
      provide: APP_INITIALIZER,
      useFactory: initializeOAuth,
      deps: [AuthService],
      multi: true
    },
    // Router MUST come after APP_INITIALIZER
    provideRouter(routes),
    provideClientHydration(withEventReplay()),
  ]
};
