export const config = {
  url: {
    BASE_URL: 'http://localhost',
    OMDB_BASE_URL: 'https://www.omdbapi.com',
    AVATARS_DICEBEAR_URL: 'https://api.dicebear.com/6.x'
  },
  keycloak: {
    BASE_URL: process.env.REACT_APP_KEYCLOAK_BASE_URL,
    REALM: 'moviebets-realm',
    CLIENT_ID: 'moviebets-app'
  }
}