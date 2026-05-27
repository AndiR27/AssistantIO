import { Injectable, PLATFORM_ID, inject } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { OAuthService, AuthConfig } from 'angular-oauth2-oidc';
import { environment } from '../../../environments/environment';
import { BehaviorSubject } from 'rxjs';

@Injectable({
    providedIn: 'root'
})
export class AuthService {
    private initialized = false;
    private initPromise: Promise<boolean> | null = null;
    private isBrowser: boolean;

    private _isAuthenticated$ = new BehaviorSubject<boolean>(false);
    isAuthenticated$ = this._isAuthenticated$.asObservable();

    constructor(private oauthService: OAuthService) {
        this.isBrowser = isPlatformBrowser(inject(PLATFORM_ID));
        if (this.isBrowser) {
            this.configure();

            this.oauthService.events.subscribe(() => {
                this._isAuthenticated$.next(this.oauthService.hasValidAccessToken());
            });
        }
    }

    private configure(): void {
        const authConfig: AuthConfig = {
            issuer: environment.keycloak.issuer,
            clientId: environment.keycloak.clientId,
            redirectUri: environment.keycloak.redirectUri || window.location.origin,
            responseType: 'code',
            scope: environment.keycloak.scope,
            showDebugInformation: !environment.production,
            requireHttps: false,
            useSilentRefresh: false,
            sessionChecksEnabled: false,
        };

        this.oauthService.configure(authConfig);
    }

    runInitialAuth(): Promise<boolean> {
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

                if (this.oauthService.hasValidAccessToken()) {
                    this.oauthService.setupAutomaticSilentRefresh();
                }

                this._isAuthenticated$.next(this.oauthService.hasValidAccessToken());
                return this.isAuthenticated();
            })
            .catch((error) => {
                console.error('Erreur lors de l\'initialisation OAuth:', error);
                this.initialized = true;
                return false;
            });

        return this.initPromise;
    }

    isInitialized(): boolean {
        return this.initialized;
    }

    login(): void {
        this.oauthService.initCodeFlow();
    }

    logout(): void {
        this.oauthService.logOut();
    }

    isAuthenticated(): boolean {
        return this.oauthService.hasValidAccessToken();
    }

    getAccessToken(): string {
        return this.oauthService.getAccessToken();
    }

    getIdentityClaims(): Record<string, unknown> {
        return this.oauthService.getIdentityClaims() as Record<string, unknown>;
    }

    isGlobalAdmin(): boolean {
        const token = this.oauthService.getAccessToken();
        if (!token) return false;
        try {
            const base64Url = token.split('.')[1];
            const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
            const payload = JSON.parse(atob(base64));
            const roles: string[] = payload?.realm_access?.roles ?? [];
            return roles.includes('ADMIN');
        } catch {
            return false;
        }
    }

    getUserName(): string {
        const claims = this.getIdentityClaims();
        return (claims?.['preferred_username'] as string)
            || (claims?.['name'] as string)
            || 'Utilisateur';
    }

}
