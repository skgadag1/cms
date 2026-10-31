import unitedStates from '~/constants/states/unitedStates.json'
import canada from '~/constants/states/canada.json'
import india from '~/constants/states/india.json'
import sriLanka from '~/constants/states/sriLanka.json'
import bangladesh from '~/constants/states/bangladesh.json'
import nepal from '~/constants/states/nepal.json'
import bhutan from '~/constants/states/bhutan.json'
import maldives from '~/constants/states/maldives.json'

export default function useCountries() {
  const countryLabel = {
    UNITED_STATES: 'United States',
    CANADA: 'Canada',
    INDIA: 'India',
    NEPAL: 'Nepal',
    SRI_LANKA: 'Sri Lanka',
    BANGLADESH: 'Bangladesh',
    BHUTAN: 'Bhutan',
    MALDIVES: 'Maldives'
  }
  const getCountries = (type) => {
    if (type === 'nao') {
      return [
        {label: countryLabel.UNITED_STATES, value: countryLabel.UNITED_STATES, states: unitedStates},
        {label: countryLabel.CANADA, value: countryLabel.CANADA, states: canada}
      ]
    } else {
      return [
        {label: countryLabel.INDIA, value: countryLabel.INDIA, states: india},
        {label: countryLabel.NEPAL, value: countryLabel.NEPAL, states: nepal},
        {label: countryLabel.SRI_LANKA, value: countryLabel.SRI_LANKA, states: sriLanka},
        {label: countryLabel.BANGLADESH, value: countryLabel.BANGLADESH, states: bangladesh},
        {label: countryLabel.BHUTAN, value: countryLabel.BHUTAN, states: bhutan},
        {label: countryLabel.MALDIVES, value: countryLabel.MALDIVES, states: maldives}
      ]
    }
  }

  return {
    getCountries,
    countryLabel
  }
}
