export const config = {
  url: {   
    BASE_URL: process.env.BASE_URL
  },
  keycloak: {
    BASE_URL: process.env.KEYCLOAK_BASE_URL,
    REALM: "moviebets-realm", 
    CLIENT_ID: "moviebets-app"
  } 
}