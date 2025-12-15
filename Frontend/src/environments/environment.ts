export const environment = {
  production: false,
  apiUrl: 'http://localhost:8088',
  keycloak: {
    issuer: 'https://keycloak.andi27.synology.me/auth/realms/assistantio',
    clientId: 'assistantio-frontend',
    redirectUri: 'http://localhost:4200/home',
    scope: 'openid profile email'
  }
};
