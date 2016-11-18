import Bacon from 'baconjs'

export const urlsP = Bacon.fromCallback(callback => 
        window.CONFIG_URL ?  (window.CONFIG_URL_OVERRIDE ? window.urls.loadFromUrls(window.CONFIG_URL, window.CONFIG_URL_OVERRIDE)
            : window.urls.loadFromUrls(window.CONFIG_URL)).success(() => callback(window))
        : callback(window));