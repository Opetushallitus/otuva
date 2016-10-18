import Bacon from 'baconjs'
// remove mocking when ready get real data from backend
import fetchMock from 'fetch-mock'

const L10N_URL = 'http://l10n'
const MOCK_DELAY = 800

fetchMock.get(L10N_URL, 
  new Promise(res => setTimeout(res, MOCK_DELAY)).then(() => ({
    "fi": {
      'VIRKAILIJAN_LISAYS_OTSIKKO': 'Virkalijan lisäys',
      'VIRKAILIJAN_TIEDOT_OTSIKKO': 'Virkalijan tiedot',
      'VIRKAILIJAN_TIEDOT_SPOSTI': 'Sähköposti',
      'VIRKAILIJAN_TIEDOT_KIELI': 'Kieli',
      'VIRKAILIJAN_LISAYS_ORGANISAATIOON_OTSIKKO': 'Virkalijan lisäys organisaatioon',
      'VIRKAILIJAN_LISAYS_ORGANISAATIOON_ORGANISAATIO': 'Organisaatio',
      'VIRKAILIJAN_LISAYS_ORGANISAATIOON_MYONNA_KAYTTOOIKEUKSIA': 'Myönnä käyttöoikeuksia',
      'VIRKAILIJAN_LISAYS_ORGANISAATIOON_LISAA': 'Lisää organisaatio...',
      'VIRKAILIJAN_LISAYS_TALLENNA': 'Tallenna ja lähetä itserekisteröitymissähköposti',
      'VIRKAILIJAN_LISAYS_LAHETETTY': 'Lähetetty'
    }
  })
))

const fetchFromUrl = url => {
  return Bacon.fromPromise(
    fetch(url)
      .then(response => {
        return response.json()
      })
  )
}

const l10nRequestS = Bacon.later(0, L10N_URL)
const l10nResponseS = l10nRequestS.flatMap(fetchFromUrl)

export const l10nResponsePendingP = l10nRequestS.awaiting(l10nResponseS)
export default l10nResponseS