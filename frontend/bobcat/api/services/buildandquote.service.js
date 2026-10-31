export default ($axios, $config) => ({
  async getQuote(categoryId = '') {
    if (!categoryId?.length) { return }
    try {
      const url = process.env.BUILDQUOTE_API_SERVER + $config.PRODUCTS_API
      return await $axios.get(url, {params: {categoryId}})
    } catch (e) {
      // eslint-disable-next-line no-console
      console.debug('[buildandquote.service] Unable to fetch quote', e)
      if (!process.server) { throw new Error(e) }
    }
  },

  async getOptionChoices(itemId = '') {
    try {
      const url = process.env.BUILDQUOTE_API_SERVER + $config.BUILD_QUOTE_API_SEARCH_PRODUCT_OPTIONS.replace('%itemId%', itemId)
      return await $axios.get(url)
    } catch (e) {
      // eslint-disable-next-line no-console
      console.debug('[buildandquote.service] Unable to fetch option choices', e)
      throw new Error(e)
    }
  }
})
