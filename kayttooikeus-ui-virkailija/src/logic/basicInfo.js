import Bacon from 'baconjs'

import dispatcher from './dispatcher'

const d = dispatcher()

const basicInfo = {
  toProperty: (initialBasicInfo={}) => {
    const assignLanguage = (basicInfo, languageCode) => {
      return {...basicInfo, languageCode}
    }
    const assignEmail = (basicInfo, email) => {
      return {...basicInfo, email}
    }
    
    return Bacon.update(initialBasicInfo,
      [d.stream('language')], assignLanguage,
      [d.stream('email')], assignEmail,
    )
  },
  setLanguage: code => d.push('language', code),
  setEmail: address => d.push('email', address)
};

export default basicInfo
export const basicInfoP = basicInfo.toProperty();
