import Bacon from 'baconjs'
// remove mocking when ready get real data from backend
import fetchMock from 'fetch-mock'

const L10N_URL = 'http://l10n'
const MOCK_DELAY = 800

fetchMock.get(L10N_URL, 
  new Promise(res => setTimeout(res, MOCK_DELAY)).then(() => ({
    "fi": {
        'HYVAKSYMATTOMAT_KAYTTOOIKEUSANOMUKSET_OTSIKKO': 'Hyväksymättömät käyttöoikeusanomukset',
        'KUTSUTUT_VIRKAILIJAT_OTSIKKO': 'Kutsutut virkailijat',
        'NAYTA_KUTSUTUT_LINKKI': 'Näytä kutsutut',
        'TAKAISIN_LINKKI': 'Takaisin'
    }
  })
));

const fetchFromUrl = url => {
  return Bacon.fromPromise(
    fetch(url)
      .then(response => {
        return response.json()
      })
  )
};

const l10nRequestS = Bacon.later(0, L10N_URL);
const l10nResponseS = l10nRequestS.flatMap(fetchFromUrl);

export const l10nResponsePendingP = l10nRequestS.awaiting(l10nResponseS);
export default l10nResponseS
