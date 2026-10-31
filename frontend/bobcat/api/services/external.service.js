/* eslint-disable no-console */
const GOOGLE_API_URL = 'https://maps.googleapis.com/maps/api/geocode/json'

export default $axios => ({
  async getLocationCoordinates(address, googleApiKey) {
    try {
      const url = `${GOOGLE_API_URL}?address=${encodeURIComponent(address)}&key=${googleApiKey}`

      return (await $axios.get(url))?.data
    } catch (e) {
      console.debug('[Google API] Unable to fetch location details', e)
      if (!process.server) {
        throw new Error(e)
      }
    }
  },

  async getAddressFromCoordinates(lat, lng, googleApiKey) {
    try {
      const url = `${GOOGLE_API_URL}?latlng=${lat},${lng}&key=${googleApiKey}`

      return (await $axios.get(url))?.data
    } catch (e) {
      console.debug('[Google API] Unable to fetch address from coordinates', e)
      if (!process.server) {
        throw new Error(e)
      }
    }
  }
})
