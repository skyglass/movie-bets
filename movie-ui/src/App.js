import React from 'react'
import { ReactKeycloakProvider } from '@react-keycloak/web'
import Keycloak from 'keycloak-js'
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom'
import Home from './components/home/Home'
import { moviesApi } from './components/misc/MoviesApi'
import Navbar from './components/misc/Navbar'
import PrivateRoute from './components/misc/PrivateRoute'
import MoviesPage from './components/movies/MoviesPage'
import UserSettings from './components/settings/UserSettings'
import MovieWizard from './components/wizard/MovieWizard'
import MovieDetail from './components/movie/MovieDetail'
import { Dimmer, Header, Icon } from 'semantic-ui-react'
import { config } from './Constants'

function App() {
  const keycloak = new Keycloak({
    url: `${config.keycloak.BASE_URL}`,
    realm: `${config.keycloak.REALM}`,
    clientId: `${config.keycloak.CLIENT_ID}`
  })
  const initOptions = { pkceMethod: 'S256' }

  const handleOnEvent = async (event, error) => {
    if (event === 'onAuthSuccess') {
      if (keycloak.authenticated) {
        let response = await moviesApi.getUserExtrasMe(keycloak.token)
        if (response.status === 404) {
          const username = keycloak.tokenParsed.preferred_username
          const userExtra = { avatar: username }
          response = await moviesApi.saveUserExtrasMe(keycloak.token, userExtra)
          console.log('UserExtra created for ' + username)
        }
        keycloak['avatar'] = response.data.avatar
      }
    }
  }

  const loadingComponent = (
    <Dimmer inverted active={true} page>
      <Header style={{ color: '#4d4d4d' }} as='h2' icon inverted>
        <Icon loading name='cog' />
        <Header.Content>Keycloak is loading
          <Header.Subheader style={{ color: '#4d4d4d' }}>or running authorization code flow with PKCE</Header.Subheader>
        </Header.Content>
      </Header>
    </Dimmer>
  )

  return (
    <ReactKeycloakProvider
      authClient={keycloak}
      initOptions={initOptions}
      LoadingComponent={loadingComponent}
      onEvent={(event, error) => handleOnEvent(event, error)}
    >
      <Router>
        <Navbar />
        <Routes>
          <Route path='/' element={<Home />} />
          <Route path='/home' element={<Home />} />
          <Route path='/movies/:id/:canVote' element={<MovieDetail />} />
          <Route path="/movies" element={<PrivateRoute><MoviesPage /></PrivateRoute>} />
          <Route path="/wizard" element={<PrivateRoute><MovieWizard /></PrivateRoute>} />
          <Route path="/settings" element={<PrivateRoute><UserSettings /></PrivateRoute>} />
          <Route path="*" element={<Navigate to="/" />} />
        </Routes>
      </Router>
    </ReactKeycloakProvider>
  )
}

export default App