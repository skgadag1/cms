import unitedStates from './states/unitedStates.json'
import canada from './states/canada.json'
import india from './states/india.json'

const COUNTRIES = [
  {label: 'United States', value: 'United States', states: unitedStates},
  {label: 'Canada', value: 'Canada', states: canada},
  {label: 'India', value: 'India', states: india}
]

export const COUNTRY_CODES = {
  UnitedStates: 'US',
  Canada: 'CA'
}

export default COUNTRIES
