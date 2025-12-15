import { Injectable, PLATFORM_ID, inject } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { OAuthService, AuthConfig } from 'angular-oauth2-oidc';
import { environment } from '../../../environments/environment';

/**
 * Service d'authentification OAuth2/OIDC avec Keycloak
 * Utilise le flux Authorization Code avec PKCE
 */
@Injectable({
    providedIn: 'root'
})
export class AuthService {
    private initialized = false;
    private initPromise: Promise<boolean> | null = null;
    private isBrowser: boolean;

    constructor(private oauthService: OAuthService) {
        this.isBrowser = isPlatformBrowser(inject(PLATFORM_ID));
        if (this.isBrowser) {
            this.configure();
        }
    }

    /**
     * Configure le client OAuth2 pour Keycloak avec PKCE
     */
    private configure(): void {
        const authConfig: AuthConfig = {
            issuer: environment.keycloak.issuer,
            clientId: environment.keycloak.clientId,
            redirectUri: environment.keycloak.redirectUri || window.location.origin,
            responseType: 'code',
            scope: environment.keycloak.scope,
            showDebugInformation: !environment.production,
            requireHttps: false, // Set to true in production with HTTPS
            // PKCE configuration
            useSilentRefresh: false,
            sessionChecksEnabled: false,
        };

        this.oauthService.configure(authConfig);
    }

    /**
     * Initialise le service OAuth - à appeler une seule fois au démarrage
     * Retourne une promesse qui est réutilisée si déjà en cours
     */
    runInitialAuth(): Promise<boolean> {
        // Skip OAuth initialization on server (SSR)
        if (!this.isBrowser) {
            this.initialized = true;
            return Promise.resolve(false);
        }

        if (this.initPromise) {
            return this.initPromise;
        }

        this.initPromise = this.oauthService.loadDiscoveryDocumentAndTryLogin()
            .then(() => {
                this.initialized = true;
                const isAuth = this.isAuthenticated();
                console.log('OAuth initialized, authenticated:', isAuth);
                return isAuth;
            })
            .catch((error) => {
                console.error('Erreur lors de l\'initialisation OAuth:', error);
                this.initialized = true; // Mark as done even on error
                return false;
            });

        return this.initPromise;
    }

    /**
     * Vérifie si le service a été initialisé
     */
    isInitialized(): boolean {
        return this.initialized;
    }

    /**
     * Déclenche le flux de connexion Keycloak
     */
    login(): void {
        this.oauthService.initCodeFlow();
    }

    /**
     * Déconnecte l'utilisateur
     */
    logout(): void {
        this.oauthService.logOut();
    }

    /**
     * Vérifie si l'utilisateur est authentifié
     */
    isAuthenticated(): boolean {
        return this.oauthService.hasValidAccessToken();
    }

    /**
     * Retourne le token d'accès JWT
     */
    getAccessToken(): string {
        return this.oauthService.getAccessToken();
    }

    /**
     * Retourne les claims de l'identité de l'utilisateur
     */
    getIdentityClaims(): Record<string, unknown> {
        return this.oauthService.getIdentityClaims() as Record<string, unknown>;
    }

    /**
     * Retourne le nom d'affichage de l'utilisateur
     */
    getUserName(): string {
        const claims = this.getIdentityClaims();
        return (claims?.['preferred_username'] as string)
            || (claims?.['name'] as string)
            || 'Utilisateur';
    }
}
