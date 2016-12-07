import Bacon from 'baconjs'

import frontUrls from './kayttooikeus-ui-virkailija-oph'
window.urls.addProperties(frontUrls)

export const urlsP = Bacon.fromCallback(callback =>
  window.CONFIG_URL ?
    window.urls.load({overrides: "/kayttooikeus-service/config/frontProperties"}).then(() => callback(window)) :
    callback(window)
).toProperty();
