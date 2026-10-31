import {reactive} from '@nuxtjs/composition-api'
import {HIDDEN_FIELDS_VALUES} from '../../constants/hiddenFields'

const dealerLocatorData = reactive({
  [HIDDEN_FIELDS_VALUES.DEALER_LOCATOR_DEALER_NAME]: '',
  [HIDDEN_FIELDS_VALUES.DEALER_LOCATOR_MDM_ID]: '',
  [HIDDEN_FIELDS_VALUES.DEALER_LOCATOR_OPT_IN]: '',
  [HIDDEN_FIELDS_VALUES.DEALER_LOCATOR_CUSTOMER_NUMBER]: ''
})

export default function useDealerLocator() {
  function saveData(key, value) {
    if (!Object.keys(dealerLocatorData).includes(key) || !value) { return }
    dealerLocatorData[key] = value
  }

  return {
    dealerLocatorData,
    saveData
  }
}
