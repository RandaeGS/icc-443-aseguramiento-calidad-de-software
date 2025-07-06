import Keycloak from 'keycloak-js';

const keycloak = new Keycloak({
    url: 'http://localhost:7080',
    realm: 'project',
    clientId: 'vue'
});

export const initKeycloak = async () => {
    try {
        const authenticated = await keycloak.init({
            onLoad: 'login-required',
            checkLoginIframe: true
        });
        if (authenticated) {
            console.log('User is authenticated');
            localStorage.setItem('token', keycloak.token);
            setInterval(() => {
                keycloak
                    .updateToken(70)
                    .then((refreshed) => {
                        if (refreshed) {
                            console.log('Token refreshed');
                            localStorage.setItem('token', keycloak.token);
                        }
                    })
                    .catch(() => {
                        console.error('Failed to refresh token');
                    });
            }, 60000);
        } else {
            console.log('User is not authenticated');
        }
    } catch (error) {
        console.error('Failed to initialize Keycloak:', error);
    }
};

export default keycloak;
