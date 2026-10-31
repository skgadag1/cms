/* eslint-disable no-console */
import {NA_PRODUCT_API_LOCALES, ROW_PRODUCT_API_LOCALES, DEALER_INVENTORY_LOCALIZED_REGIONS, BUSINESS_REGIONS} from '~/constants'

export const shouldIncludeLocaleParams = ($regions) => {
  return DEALER_INVENTORY_LOCALIZED_REGIONS.includes($regions?.current?.name)
}

export const getDefaultLocaleForLang = ($regions, language) => {
  // Return explicit locale if set
  if ($regions?.current?.locale) {
    return $regions.current.locale
  }

  // Otherwise, use locale map as fallback only if mapping exists
  const isNARegion = BUSINESS_REGIONS.na.includes($regions?.current?.name)
  const localeMap = isNARegion ? NA_PRODUCT_API_LOCALES : ROW_PRODUCT_API_LOCALES

  const result = localeMap[language]

  return typeof result !== 'undefined' ? result : null
}

export default ($axios, $regions, $config) => ({
  async getProductTypes(isDealerInventory = true, params = {}) {
    try {
      const url = `${$config.BOBCAT_API_SERVER}${$config.PRODUCTS_API}/product-types`
      const options = {
        params: {
          isDealerInventory,
          ...params
        }
      }

      return (await $axios.get(url, options))?.data
    } catch (e) {
      console.debug(`${isDealerInventory ? '[dealer-inventory]' : '[products]'} Unable to fetch product types`, e)
      if (!process.server) { throw new Error(e) }
    }
  },

  async getModelProductType(isDealerInventory = true, modelId, params = {}) {
    try {
      const url = `${$config.BOBCAT_API_SERVER}models/${modelId}/product-type`
      const options = {
        params: {
          isDealerInventory,
          ...params
        }
      }

      return (await $axios.get(url, options))?.data
    } catch (e) {
      console.debug(`${isDealerInventory ? '[dealer-inventory]' : '[models]'} Unable to fetch model's product type`, e)
      if (!process.server) { throw new Error(e) }
    }
  },

  async getModels(productType, level, isDealerInventory = true, params = {}) {
    try {
      const url = `${$config.BOBCAT_API_SERVER}${$config.PRODUCTS_API}/product-types/${productType}/models`
      const options = {
        params: {
          level,
          isDealerInventory,
          ...params
        }
      }
      return (await $axios.get(url, options))?.data
    } catch (e) {
      console.debug('[dealer-inventory] Unable to fetch product models', e)
      if (!process.server) { throw new Error(e) }
    }
  },

  async searchResults(address, distance, unitOfLength, productType, models = [], limit = 200, filters) {
    let params = {
      zipCode: address,
      distance,
      unitOfLength,
      ...(!!productType?.facetCategory && {facetCategory: productType?.facetCategory}),
      ...(!!filters?.length > 0 && {filters: filters.join('|')}),
      productType: productType?.irwName || productType,
      size: limit,
      felBannerEnabled: true,
      from: 0
    }
    if (models?.length > 0) {
      params = {...params, models}
    }
    try {
      const url = `${$config.BOBCAT_API_SERVER}${$config.DEALER_INVENTORY_API}/inventories`
      return (await $axios.get(url, {params}))?.data
    } catch (e) {
      console.debug('[dealer-inventory] Unable to search product results', e)
      if (!process.server) {
        throw new Error(e)
      }
    }
  },

  async searchResultsV2(address, country, distance, unitOfLength, category, models = [], limit = 200, filters) {
    const shouldLocalize = shouldIncludeLocaleParams($regions)
    const lang = $regions?.current?.language?.name || process.env.NUXT_DEFAULT_LANGUAGE
    let params = {
      address,
      country,
      distance,
      unitOfLength,
      ...(!!filters?.length > 0 && {filters: filters.join('|')}),
      ...(shouldLocalize && {lang}),
      productType: category.irwName,
      productCode: category.contractCode,
      size: limit,
      felBannerEnabled: true,
      from: 0
    }
    if (models?.length > 0) {
      params = {...params, models}
    }
    try {
      const url = `${$config.BOBCAT_API_SERVER}${$config.DEALER_INVENTORY_API_V2}/inventories`
      return (await $axios.get(url, {params}))?.data
    } catch (e) {
      console.debug('[dealer-inventory] Unable to search product results', e)
      if (!process.server) {
        throw new Error(e)
      }
    }
  },

  async searchDealerInventory(mdmId, productType, filters = []) {
    const shouldLocalize = shouldIncludeLocaleParams($regions)
    const lang = $regions?.current?.language?.name || process.env.NUXT_DEFAULT_LANGUAGE
    const params = {
      ...(!!productType?.irwName && {productType: productType.irwName}),
      ...(!!productType?.facetCategory && {facetCategory: productType.facetCategory}),
      ...(filters?.length > 0 && {filters: filters.join('|')}),
      ...(shouldLocalize && {lang}),
      felBannerEnabled: true
    }
    try {
      const url = `${$config.BOBCAT_API_SERVER}${$config.DEALER_INVENTORY_API_V2}/dealers/${mdmId}/inventories`
      return (await $axios.get(url, {params}))?.data
    } catch (e) {
      console.debug('[dealer-inventory] Unable to fetch dealer inventory', e)
      if (!process.server) {
        throw new Error(e)
      }
    }
  },

  async getModelDetails(dealerID, modelID, pricingEnabled = true, felBannerEnabled = true, queryParams) {
    try {
      const productType = queryParams?.['product-type']
      const level = queryParams?.level
      const url = `${$config.BOBCAT_API_SERVER}${$config.DEALER_INVENTORY_API}/inventories/${dealerID}/models/${modelID}`
      const shouldLocalize = shouldIncludeLocaleParams($regions)
      const lang = $regions?.current?.language?.name || process.env.NUXT_DEFAULT_LANGUAGE
      const locale = getDefaultLocaleForLang($regions, lang)
      const options = {
        params: {
          productType,
          level,
          ...(pricingEnabled && {pricingEnabled: true}),
          ...(felBannerEnabled && {felBannerEnabled: true}),
          noerror: true,
          ...(shouldLocalize && {lang}),
          ...(shouldLocalize && locale && {locale})
        }
      }
      return (await $axios.get(url, options))?.data
    } catch (e) {
      console.debug('[dealer-inventory] Unable to fetch model details', e)
      if (!process.server) { throw new Error(e) }
    }
  }
})
