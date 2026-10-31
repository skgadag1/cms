import canadaStates from './states/canada.json'
import unitedStates from './states/unitedStates.json'

export const US_REGION_CODES = [
  'AL', 'AK', 'AZ', 'AR', 'CA', 'CO', 'CT', 'DE', 'FL', 'GA',
  'HI', 'ID', 'IL', 'IN', 'IA', 'KS', 'KY', 'LA', 'ME', 'MD',
  'MA', 'MI', 'MN', 'MS', 'MO', 'MT', 'NE', 'NV', 'NH', 'NJ',
  'NM', 'NY', 'NC', 'ND', 'OH', 'OK', 'OR', 'PA', 'RI', 'SC',
  'SD', 'TN', 'TX', 'UT', 'VT', 'VA', 'WA', 'WV', 'WI', 'WY',
  'DC', 'PR', 'VI', 'GU', 'AS', 'MP'
]

export const CA_REGION_CODES = [
  'ON', 'QC', 'BC', 'AB', 'MB', 'SK', 'NS', 'NB', 'NL', 'PE',
  'YT', 'NT', 'NU'
]

// Full-name -> 2-letter code lookup, sourced from the existing state JSON files
// so that regions supplied as full names (e.g. "North Dakota") can be normalized.
const buildNameToCode = list => list.reduce((acc, {label, value}) => {
  if (label && value) {
    acc[label.trim().toLowerCase()] = value.toUpperCase()
  }
  return acc
}, {})

const US_NAME_TO_CODE = buildNameToCode(unitedStates)
const CA_NAME_TO_CODE = buildNameToCode(canadaStates)

// Normalizes a raw region value to a 2-letter code when possible.
// Accepts either an already-valid 2-letter code or a full state/province name.
const normalizeRegionCode = (rawRegion) => {
  if (!rawRegion) {
    return ''
  }
  const trimmed = String(rawRegion).trim()
  if (!trimmed) {
    return ''
  }
  if (trimmed.length === 2) {
    return trimmed.toUpperCase()
  }
  const key = trimmed.toLowerCase()
  return US_NAME_TO_CODE[key] || CA_NAME_TO_CODE[key] || ''
}

export const deriveAddressCountry = (explicitCountry, rawRegion) => {
  if (explicitCountry && String(explicitCountry).trim()) {
    return String(explicitCountry).trim()
  }
  const code = normalizeRegionCode(rawRegion)
  if (!code) {
    return ''
  }
  if (US_REGION_CODES.includes(code)) {
    return 'US'
  }
  if (CA_REGION_CODES.includes(code)) {
    return 'CA'
  }
  return ''
}
