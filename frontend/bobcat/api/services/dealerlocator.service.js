/* eslint-disable no-console */

export default ($axios, $regions, $config) => ({
  async getDealerDetails(mdmId) {
    try {
      const url = `${$config.BOBCAT_API_SERVER}${$config.DEALER_LOCATOR_API}/${mdmId}/details`
      const options = {
        params: {
          region: $regions.current.name,
          lang: $regions.current.language.name,
          noerror: true
        }
      }

      return (await $axios.get(url, options))?.data
    } catch (e) {
      console.debug('[dealer locator] Unable to fetch dealer details', e)
      if (!process.server) {
        throw new Error(e)
      }
    }
  },

  async calculateDealerDistanceByCar(userAddress, dealerAddress) {
    try {
      const url = `${$config.BOBCAT_API_SERVER}${$config.BOBCAT_API_LOCATION}/distance`

      const options = {
        params: {
          userLocation: userAddress,
          destinationLocation: dealerAddress
        }
      }
      return (await $axios.get(url, options))?.data
    } catch (e) {
      console.debug('[dealer locator] Unable to fetch distance by car', e)
      if (!process.server) {
        throw new Error(e)
      }
    }
  },

  async getAllContractCodes(grouping = true) {
    try {
      const url = `${$config.BOBCAT_API_SERVER}/dealer-locator/contract-codes`
      const options = {
        params: {
          grouping,
          region: $regions.current.name,
          instance: 'public',
          lang: $regions.current.language.name
        },
        headers: {
          'x-api-key': $config.BOBCAT_API_KEY ?? ''
        }
      }
      return (await $axios.get(url, options))?.data
    } catch (e) {
      console.debug('[dealer locator] Unable to fetch all contract codes', e)
      return []
    }
  },

  async getDealerContractCodes(mdmId, grouping = true) {
    try {
      const url = `${$config.BOBCAT_API_SERVER}${$config.DEALER_LOCATOR_API}/${mdmId}/contract-codes`
      const options = {
        params: {
          grouping,
          region: $regions.current.name,
          instance: 'public',
          lang: $regions.current.language.name
        },
        headers: {
          'x-api-key': $config.BOBCAT_API_KEY ?? ''
        }
      }
      return (await $axios.get(url, options))?.data
    } catch (e) {
      console.debug('[dealer locator] Unable to fetch dealer contract codes', e)
      if (!process.server) {
        throw new Error(e)
      }
    }
  },

  async getDealerIndustriesAndServices(mdmId) {
    try {
      const url = `${$config.BOBCAT_API_SERVER}${$config.DEALER_LOCATOR_API}/${mdmId}/labels`
      const options = {
        params: {
          region: $regions.current.name,
          instance: 'public',
          lang: $regions.current.language.name
        },
        headers: {
          'x-api-key': $config.BOBCAT_API_KEY ?? ''
        }
      }
      return (await $axios.get(url, options))?.data
    } catch (e) {
      console.debug('[dealer locator] Unable to fetch dealer industries & services', e)
      if (!process.server) {
        throw new Error(e)
      }
    }
  }
})
