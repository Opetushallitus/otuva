import Bacon from 'baconjs'

import frontUrls from './kayttooikeus-ui-virkailija-oph'

window.urls.addProperties(frontUrls);

export const urlsP = Bacon.fromCallback(callback => window.CONFIG_URL ?
    window.urls().load({overrides: window.CONFIG_URL}).then(() => callback(window),
        e => {
            console.error(e);
            alert(`Loading backend URLs failed. Backend server might not be responding...

URL-osoitteiden lataus epäonnistui. Taustapalvelu ei mahdollisesti vastaa...`);
        }) :
    callback(window)
).toProperty();
