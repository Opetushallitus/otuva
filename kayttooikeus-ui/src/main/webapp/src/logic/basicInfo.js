import Bacon from 'baconjs'

import Dispatcher from './dispatcher'

const dispatcher = new Dispatcher()

const basicInfo = {
  toProperty: initialBasicInfo => {
    const assignLanguage = (basicInfo, languageCode) => {
      return {...basicInfo, languageCode}
    }

    const assignEmail = (basicInfo, email) => {
      return {...basicInfo, email}
    }
    
    return Bacon.update(initialBasicInfo,
      [dispatcher.stream('language')], assignLanguage,
      [dispatcher.stream('email')], assignEmail,
    )
  },
  setLanguage: code => dispatcher.push('language', code),
  setEmail: address => dispatcher.push('email', address),
}

export default basicInfo
