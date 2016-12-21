import Bacon from 'baconjs'

import dispatcher from './dispatcher'

const d = dispatcher();

const basicInfo = {
  toProperty: (initialBasicInfo={email: null, language: null, etunimi: null, sukunimi: null}) => {
    return Bacon.update(initialBasicInfo,
      [d.stream('overwrite')], (info, over) => ({...info, ...over})
    )
  },
  setLanguage: code => d.push('overwrite', {languageCode: code}),
  setEmail: address => d.push('overwrite', {email: address}),
  setEtunimi: etunimi => d.push('overwrite', {etunimi: etunimi}),
  setSukunimi: sukuinimi => d.push('overwrite', {sukunimi: sukuinimi}),
  clear: () => d.push('overwrite', {email: null, etunimi: null, sukunimi: null})
};

export default basicInfo
export const basicInfoP = basicInfo.toProperty();
