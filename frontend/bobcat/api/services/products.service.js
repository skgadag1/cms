/* eslint-disable no-console */
import {NA_PRODUCT_API_LOCALES, ROW_PRODUCT_API_LOCALES, MODEL_TYPE} from '~/constants'

function getBrandId() {
  return process.env.VUE_APP_SITE_BASENAME.replaceAll('/', '')
}

export default ($axios, $regions, $utils, $config) => ({
  async getModelSpecifications(model, uom = 'us', specLevel = 3, modelType = MODEL_TYPE.PRODUCT) {
    return await this.getSpecifications(model?.id, $utils.products.getBusinessUnit(model), $utils.products.getProductRegion(model), uom, specLevel, modelType, model.productDetailsDataSource)
  },
  async getSpecifications(modelId, businessUnit = 'na', region = 'na', uom = 'us', specLevel = 3, modelType = MODEL_TYPE.PRODUCT, productDetailsDataSource = 'default') {
    if (!modelId) { return null }

    try {
      const url = `${$config.BOBCAT_API_SERVER}${$config.BOBCAT_API_PRODUCT}`
        .replace('%BRANDID%', getBrandId())
        .replace('%MODELID%', modelId)
        .replace('%LOCALE%', businessUnit === 'na'
          ? ($regions.current.locale || NA_PRODUCT_API_LOCALES[$regions.current.language.name])
          : ($regions.current.locale || ROW_PRODUCT_API_LOCALES[$regions.current.language.name]))
        .replace('%UOM%', uom)
        .replace('%BUSINESSUNIT%', businessUnit)
        .replace('%REGION%', region)
        .replace('%SPECLEVEL%', specLevel)
        .replace('%MODELTYPE%', modelType)
        .replace('%INSTANCE%', $config.INSTANCE_NAME)
      // onSsrOnly:false opts THIS call into a real client-side timeout instead of the
      // default "0" (no timeout) that onSsrOnly:true leaves on the browser. Scoped here so
      // compare spec fetches fail fast on the client without changing the shared
      // axiosWithTimeout behavior for every other caller (broad blast radius).
      return await $utils.axiosWithTimeout({
        url,
        method: 'GET',
        params: {noerror: true, lang: $regions.current.language.name, productDetailsDataSource}
      }, {onSsrOnly: false, timeout: 8000})
    } catch (e) {
      console.debug('[products.service] Unable to fetch model details', e)
      if (!process.server) { throw new Error(e) }
    }
  },

  async getCompareModels({id, path} = {}) {
    if (!id && !path) { return [] }
    const url = `${$config.BOBCAT_API_SERVER}${$config.PRODUCTS_API}/compare/models`
    const {data} = await $axios.get(url, {
      params: {
        ...(id ? {id} : {path}),
        lang: $regions.current.language.name
      },
      useCache: true
    })
    return data?.results || []
  },

  async downloadPdfSpecification(modelId, businessUnit = 'emea', uom = 'metric', modelType = MODEL_TYPE.PRODUCT) {
    const url = `${$config.BOBCAT_API_SERVER}${$config.BOBCAT_API_PRODUCT_PDF}`
      .replace('%BRANDID%', getBrandId())
      .replace('%MODELID%', modelId)
      .replace('%LOCALE%', businessUnit === 'na'
        ? ($regions.current.locale || NA_PRODUCT_API_LOCALES[$regions.current.language.name])
        : ($regions.current.locale || ROW_PRODUCT_API_LOCALES[$regions.current.language.name]))
      .replace('%UOM%', uom)
      .replace('%BUSINESSUNIT%', businessUnit)
      .replace('%MODELTYPE%', modelType)

    try {
      return await $axios.$get(url, {
        responseType: 'blob',
        headers: {
          Accept: 'application/pdf'
        }
      })
    } catch (e) {
      console.debug('[products.service] Unable to download pdf file', e)
      throw new Error(e)
    }
  }
})
