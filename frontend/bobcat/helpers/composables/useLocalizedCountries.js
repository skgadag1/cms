import {COUNTRY_CODES, LANGUAGE_MAPPING} from '~/constants/localizedCountries'

export default function useLocalizedCountries() {
  const mapLanguageToLocale = (languageCode) => {
    return LANGUAGE_MAPPING[languageCode?.toLowerCase()] || 'en-US'
  }

  const getLocalizedCountryName = (countryCode, language = 'en', useNative = false) => {
    if (!countryCode) {
      return null
    }

    try {
      const locale = useNative ? 'en-US' : mapLanguageToLocale(language)
      const displayNames = new Intl.DisplayNames([locale], {type: 'region'})
      return displayNames.of(countryCode.toUpperCase())
    } catch (error) {
      try {
        const displayNames = new Intl.DisplayNames(['en-US'], {type: 'region'})
        return displayNames.of(countryCode.toUpperCase())
      } catch (fallbackError) {
        return countryCode.toUpperCase()
      }
    }
  }

  const getLocalizedCountryOptions = (language = 'en', useNative = false) => {
    const locale = useNative ? 'en-US' : mapLanguageToLocale(language)

    try {
      const displayNames = new Intl.DisplayNames([locale], {type: 'region'})

      return COUNTRY_CODES
        .map(code => ({
          label: displayNames.of(code),
          value: code,
          selected: false
        }))
        .filter(country => country.label)
        .sort((a, b) => a.label.localeCompare(b.label, locale))
    } catch (error) {
      try {
        const displayNames = new Intl.DisplayNames(['en-US'], {type: 'region'})

        return COUNTRY_CODES
          .map(code => ({
            label: displayNames.of(code),
            value: code,
            selected: false
          }))
          .filter(country => country.label)
          .sort((a, b) => a.label.localeCompare(b.label, 'en-US'))
      } catch (fallbackError) {
        return COUNTRY_CODES.map(code => ({
          label: code,
          value: code,
          selected: false
        }))
      }
    }
  }

  const getCountryByLocalizedName = (name, language = 'en') => {
    const locale = mapLanguageToLocale(language)

    try {
      const displayNames = new Intl.DisplayNames([locale], {type: 'region'})

      return COUNTRY_CODES.find((code) => {
        const localizedName = displayNames.of(code)
        return localizedName?.toLowerCase() === name.toLowerCase()
      })
    } catch (error) {
      try {
        const displayNames = new Intl.DisplayNames(['en-US'], {type: 'region'})

        return COUNTRY_CODES.find((code) => {
          const localizedName = displayNames.of(code)
          return localizedName?.toLowerCase() === name.toLowerCase()
        })
      } catch (fallbackError) {
        return COUNTRY_CODES.find(code =>
          code.toLowerCase() === name.toLowerCase()
        )
      }
    }
  }

  const createLocalizedCountriesData = () => {
    return COUNTRY_CODES.map(code => ({
      code,
      native: getLocalizedCountryName(code, 'en', true),
      en: getLocalizedCountryName(code, 'en'),
      fr: getLocalizedCountryName(code, 'fr'),
      cn: getLocalizedCountryName(code, 'cn'),
      zh: getLocalizedCountryName(code, 'zh'),
      it: getLocalizedCountryName(code, 'it'),
      es: getLocalizedCountryName(code, 'es'),
      de: getLocalizedCountryName(code, 'de'),
      nl: getLocalizedCountryName(code, 'nl'),
      ko: getLocalizedCountryName(code, 'ko'),
      pt: getLocalizedCountryName(code, 'pt'),
      ru: getLocalizedCountryName(code, 'ru')
    }))
  }

  return {
    getLocalizedCountryName,
    getLocalizedCountryOptions,
    getCountryByLocalizedName,
    createLocalizedCountriesData,
    mapLanguageToLocale
  }
}
