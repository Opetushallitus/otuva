import Bacon from 'baconjs'

import dispatcher from './dispatcher'

const d = dispatcher();

const basicInfo = {
  toProperty: (initialBasicInfo={email: null, language: null, etunimi: null, sukunimi: null}) => {
    const assignLanguage = (basicInfo, languageCode) => {
      return {...basicInfo, languageCode}
    };
    const assignEmail = (basicInfo, email) => {
      return {...basicInfo, email}
    };
    return Bacon.update(initialBasicInfo,
      [d.stream('language')], assignLanguage,
      [d.stream('email')], assignEmail,
      [d.stream('etunimi')], (info, etunimi) => ({...info, etunimi}),
      [d.stream('sukunimi')], (info, sukunimi) => ({...info, sukunimi})
    )
  },
  setLanguage: code => d.push('language', code),
  setEmail: address => d.push('email', address),
  setEtunimi: etunimi => d.push('etunimi', etunimi),
  setSukunimi: sukuinimi => d.push('sukunimi', sukuinimi)
};

export default basicInfo
export const basicInfoP = basicInfo.toProperty();
