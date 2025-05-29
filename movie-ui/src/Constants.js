export const config = {
  url: {
    API_BASE_URL: process.env.API_BASE_URL,
    OMDB_BASE_URL: 'https://www.omdbapi.com',
    AVATARS_DICEBEAR_URL: 'https://api.dicebear.com/6.x'
  },
  keycloak: {
    BASE_URL: process.env.KEYCLOAK_BASE_URL,
    REALM: "moviebets-realm",
    CLIENT_ID: "moviebets-app"
  }
}