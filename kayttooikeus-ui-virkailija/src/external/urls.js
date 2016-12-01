import Bacon from 'baconjs'

import frontUrls from './kayttooikeus-ui-virkailija-oph'
window.urls.addProperties(frontUrls)

export const urlsP = Bacon.fromCallback(callback =>
  window.CONFIG_URL ?
    window.urls.loadFromUrls("/kayttooikeus-service/config/frontProperties.json").success(() => callback(window)) :
    callback(window)
).toProperty();
