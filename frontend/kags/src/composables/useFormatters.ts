import qs from 'qs'
import langs from 'langs'

export type LabelMap = Record<string, unknown>

export const getNavigatorLanguage = (): string => (navigator.languages && navigator.languages.length)
  ? navigator.languages[0]
  : (navigator as Navigator & {userLanguage?: string; browserLanguage?: string}).userLanguage
    || navigator.language
    || (navigator as Navigator & {browserLanguage?: string}).browserLanguage
    || 'en-US'

export const getUserTimeZone = (): string => Intl.DateTimeFormat().resolvedOptions().timeZone

export const translate = (value: string, labels: LabelMap | null | undefined, key: string | null = null): string => {
  if (!labels || Object.keys(labels).length === 0) return ''
  if (!key) {
    if (typeof labels[value] === 'string') return labels[value] as string
    if (labels.config && typeof (labels.config as LabelMap)[value] === 'string') return (labels.config as LabelMap)[value] as string
    return `{ config.${value} }`
  }
  const contextual = labels[key] as LabelMap | undefined
  if (contextual && typeof contextual[value] === 'string') return contextual[value] as string
  if (typeof labels[value] === 'string') return labels[value] as string
  if (labels.config && typeof (labels.config as LabelMap)[value] === 'string') return (labels.config as LabelMap)[value] as string
  return `{ ${key}.${value} }`
}

export const trunc = (str: string | null | undefined, len = 55, clamp = '...'): string | null | undefined =>
  str && str.length ? str.slice(0, len) + (len < str.length ? clamp : '') : str

export const formatDate = (val: string | number | Date): string => {
  const options: Intl.DateTimeFormatOptions = {day: 'numeric', month: 'short', year: 'numeric'}
  return new Date(val).toLocaleDateString(getNavigatorLanguage(), options)
}

export const formatTime = (val: string | number | Date): string => {
  const options: Intl.DateTimeFormatOptions = {hour: 'numeric', minute: 'numeric'}
  return `${new Date(val).toLocaleTimeString(getNavigatorLanguage(), options)} ${getUserTimeZone()}`
}

export const replace = (value: string, keyword: string | RegExp, replacement: string): string => value.replace(keyword, replacement)

export const capitalize = (value: unknown): string => {
  if (!value) return ''
  const text = String(value)
  return text.charAt(0).toUpperCase() + text.slice(1)
}

export const translateLocale = (facet: string | null | undefined): string => {
  if (!facet) return ''
  const language = langs.where('1', facet.substr(0, 2))
  return language ? `${language.local} (${facet.substr(2, 2)})` : facet
}

export const updateUrlParam = (keyword: string, value: string | number | null | undefined): void => {
  if (!window.history || !history.replaceState) return
  let newAddress = location.origin + location.pathname
  if (location.href.indexOf('?') > 0) {
    const items = qs.parse(location.search.substr(1))
    if (value) items[keyword] = value
    else delete items[keyword]
    if (Object.entries(items).length > 0) newAddress += '?' + qs.stringify(items)
  } else if (value !== '') newAddress += '?' + keyword + '=' + value
  history.replaceState('', '', newAddress)
}

// Expose the formatter helpers to templates and Options API methods through setup().
export function useFormatters() {
  return {
    getNavigatorLanguage,
    getUserTimeZone,
    trunc,
    formatDate,
    formatTime,
    replace,
    capitalize,
    translateLocale,
    updateUrlParam
  }
}

export default useFormatters
