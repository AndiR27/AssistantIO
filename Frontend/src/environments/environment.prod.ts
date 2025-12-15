export const environment = {
  production: true,
  apiUrl: '/api',
  keycloak: {
    issuer: 'https://keycloak.andi27.synology.me/auth/realms/assistantio',
    clientId: 'assistantio-frontend',
    redirectUri: '', // Will use window.location.origin at runtime
    scope: 'openid profile email'
  }
};
