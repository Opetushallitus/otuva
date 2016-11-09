import Bacon from 'baconjs'

const L10N_URL = '/kayttooikeus-service/l10n';

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
