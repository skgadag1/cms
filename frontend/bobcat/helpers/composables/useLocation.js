import {onMounted, ref, useContext} from '@nuxtjs/composition-api'
import {getWithExpiry, setWithExpiry} from '../functional/AppHelpers'
import {LOCATION_KEYS} from '~/constants'

const zipCode = ref('')
const address = ref('')
const countryCode = ref('')
const range = ref('')
const locationChanged = ref(0)

export default function useLocation() {
  const {$regions} = useContext()
  const getLocationFromStore = () => {
    if (!process.client) { return {} }
    return {
      zipCode: getWithExpiry(LOCATION_KEYS.ZIP_CODE),
      address: getWithExpiry(LOCATION_KEYS.ADDRESS),
      countryCode: getWithExpiry(LOCATION_KEYS.COUNTRY_CODE),
      range: getWithExpiry(LOCATION_KEYS.RANGE)
    }
  }

  onMounted(() => {
    if ($regions?.current?.name !== 'na') { return } // prevent loading if region is not NA
    const storedData = getLocationFromStore()
    if (storedData.zipCode) { zipCode.value = storedData.zipCode }
    if (storedData.address) { address.value = storedData.address }
    if (storedData.countryCode) { countryCode.value = storedData.countryCode }
    if (storedData.range) { range.value = storedData.range }
  })

  function saveLocation(key, value) {
    if ($regions?.current?.name !== 'na') { return } // prevent saving if region is not NA
    if (!Object.values(LOCATION_KEYS).includes(key) || !value) { return }
    setWithExpiry(key, value)
    const location = {address, countryCode, zipCode, range}
    location[key].value = value
  }

  function notify() {
    setTimeout(() => {
      locationChanged.value += 1
    }, 0)
  }

  return {
    notify,
    saveLocation,
    getLocationFromStore,
    zipCode,
    address,
    countryCode,
    range,
    locationChanged
  }
}
