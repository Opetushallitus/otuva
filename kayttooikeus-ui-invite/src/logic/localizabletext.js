import R from 'ramda'

const FORMATS = [
  {
    // used (at least) in henkilöpalvelu
    isValid: (localizableText) => Array.isArray(localizableText.texts),
    getValue: (localizableText, uiLang) => R.find(R.propEq('lang', uiLang.toUpperCase()))(localizableText.texts).text
  },
  {
    // used (at least) in organisaatiopalvelu
    isValid: (localizableText) => typeof localizableText === "object" && localizableText !== null,
    getValue: (localizableText, uiLang) => localizableText[uiLang.toLowerCase()]
  }
]

export function toLocalizedText(uiLang, localizableText, fallbackValue) {
  if (typeof localizableText === 'undefined') {
    return fallbackValue
  }
  let value = R.pipe(
    R.filter((format) => format.isValid(localizableText)),
    R.map((format) => format.getValue(localizableText, uiLang)),
    R.find((value) => typeof value !== 'undefined')
  )(FORMATS)
  if (typeof value === 'undefined') {
    return fallbackValue
  }
  return value
}
