import { config } from '../../Constants'

export const getAvatarUrl = (text) => {
  return `${config.url.AVATARS_DICEBEAR_URL}/avataaars/svg?seed=${text}`
}

export const isAdmin = (keycloak) => {
  return keycloak?.tokenParsed?.resource_access?.['moviebets-app']?.roles?.includes('MOVIEBETS_MANAGER') ?? false
}

export const getUsername = (keycloak) => {
  return keycloak.authenticated && keycloak.tokenParsed?.preferred_username
}

export const handleLogError = (error) => {
  if (error.response) {
    console.log(error.response.data)
  } else if (error.request) {
    console.log(error.request)
  } else {
    console.log(error.message)
  }
}