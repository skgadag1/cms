// Setup environment variables for testing
import productsServiceFactory from '~/api/services/products.service'
import {NA_PRODUCT_API_LOCALES, ROW_PRODUCT_API_LOCALES, MODEL_TYPE} from '~/constants'

process.env.VUE_APP_SITE_BASENAME = process.env.VUE_APP_SITE_BASENAME || '/bobcat'
const mockConfig = {
  BOBCAT_API_SERVER: process.env.BOBCAT_API_SERVER || 'https://test.api.com/',
  BOBCAT_API_PRODUCT: process.env.BOBCAT_API_PRODUCT || 'products/%BRANDID%/%MODELID%/specs?uom=%UOM%&locale=%LOCALE%&businessUnit=%BUSINESSUNIT%&region=%REGION%&specLevel=%SPECLEVEL%&modelType=%MODELTYPE%&instance=%INSTANCE%',
  BOBCAT_API_PRODUCT_PDF: process.env.BOBCAT_API_PRODUCT_PDF || 'products/%BRANDID%/%MODELID%/specs/pdf?uom=%UOM%&locale=%LOCALE%&businessUnit=%BUSINESSUNIT%&modelType=%MODELTYPE%',
  INSTANCE_NAME: process.env.INSTANCE_NAME || 'test'
}

describe('[products.service] Locale Resolution', () => {
  let productsService
  let mockAxios
  let mockRegions
  let mockUtils

  beforeEach(() => {
    // Setup mock axios
    mockAxios = {
      $get: jest.fn()
    }

    // Setup mock utils
    mockUtils = {
      axiosWithTimeout: jest.fn().mockResolvedValue({data: {}}),
      products: {
        getBusinessUnit: jest.fn(),
        getProductRegion: jest.fn()
      }
    }

    // Setup default mock regions
    mockRegions = {
      current: {
        locale: null,
        language: {
          name: 'en'
        }
      }
    }

    // Create service instance
    productsService = productsServiceFactory(mockAxios, mockRegions, mockUtils, mockConfig)
  })

  describe('getSpecifications()', () => {
    describe('NA business unit', () => {
      it('should use en-US locale for English language when regions.current.locale is not set', async () => {
        mockRegions.current.language.name = 'en'
        mockRegions.current.locale = null

        await productsService.getSpecifications('M0001', 'na', 'na', 'us', 3, MODEL_TYPE.PRODUCT)

        expect(mockUtils.axiosWithTimeout).toHaveBeenCalled()
        const callArgs = mockUtils.axiosWithTimeout.mock.calls[0][0]
        expect(callArgs.url).toContain('locale=en-US')
      })

      it('should use es-US locale for Spanish language when regions.current.locale is not set', async () => {
        mockRegions.current.language.name = 'es'
        mockRegions.current.locale = null

        await productsService.getSpecifications('M0001', 'na', 'na', 'us', 3, MODEL_TYPE.PRODUCT)

        expect(mockUtils.axiosWithTimeout).toHaveBeenCalled()
        const callArgs = mockUtils.axiosWithTimeout.mock.calls[0][0]
        expect(callArgs.url).toContain('locale=es-US')
      })

      it('should use regions.current.locale when it is set (highest priority)', async () => {
        mockRegions.current.language.name = 'en'
        mockRegions.current.locale = 'fr-CA'

        await productsService.getSpecifications('M0001', 'na', 'na', 'us', 3, MODEL_TYPE.PRODUCT)

        expect(mockUtils.axiosWithTimeout).toHaveBeenCalled()
        const callArgs = mockUtils.axiosWithTimeout.mock.calls[0][0]
        expect(callArgs.url).toContain('locale=fr-CA')
      })

      it('should handle undefined language gracefully', async () => {
        mockRegions.current.language.name = undefined
        mockRegions.current.locale = null

        await productsService.getSpecifications('M0001', 'na', 'na', 'us', 3, MODEL_TYPE.PRODUCT)

        expect(mockUtils.axiosWithTimeout).toHaveBeenCalled()
        const callArgs = mockUtils.axiosWithTimeout.mock.calls[0][0]
        expect(callArgs.url).toContain('locale=undefined')
      })
    })

    describe('EMEA business unit', () => {
      it('should use de-DE locale for German language when regions.current.locale is not set', async () => {
        mockRegions.current.language.name = 'de'
        mockRegions.current.locale = null

        await productsService.getSpecifications('M0001', 'emea', 'eu', 'metric', 3, MODEL_TYPE.PRODUCT)

        expect(mockUtils.axiosWithTimeout).toHaveBeenCalled()
        const callArgs = mockUtils.axiosWithTimeout.mock.calls[0][0]
        expect(callArgs.url).toContain('locale=de-DE')
      })

      it('should use es-ES locale for Spanish language (not es-US)', async () => {
        mockRegions.current.language.name = 'es'
        mockRegions.current.locale = null

        await productsService.getSpecifications('M0001', 'emea', 'eu', 'metric', 3, MODEL_TYPE.PRODUCT)

        expect(mockUtils.axiosWithTimeout).toHaveBeenCalled()
        const callArgs = mockUtils.axiosWithTimeout.mock.calls[0][0]
        expect(callArgs.url).toContain('locale=es-ES')
        expect(callArgs.url).not.toContain('locale=es-US')
      })

      it('should use fr-FR locale for French language', async () => {
        mockRegions.current.language.name = 'fr'
        mockRegions.current.locale = null

        await productsService.getSpecifications('M0001', 'emea', 'eu', 'metric', 3, MODEL_TYPE.PRODUCT)

        expect(mockUtils.axiosWithTimeout).toHaveBeenCalled()
        const callArgs = mockUtils.axiosWithTimeout.mock.calls[0][0]
        expect(callArgs.url).toContain('locale=fr-FR')
      })

      it('should use regions.current.locale when it is set (highest priority)', async () => {
        mockRegions.current.language.name = 'de'
        mockRegions.current.locale = 'de-AT'

        await productsService.getSpecifications('M0001', 'emea', 'eu', 'metric', 3, MODEL_TYPE.PRODUCT)

        expect(mockUtils.axiosWithTimeout).toHaveBeenCalled()
        const callArgs = mockUtils.axiosWithTimeout.mock.calls[0][0]
        expect(callArgs.url).toContain('locale=de-AT')
      })
    })

    describe('ALAO business unit', () => {
      it('should use fr-FR locale for French language when regions.current.locale is not set', async () => {
        mockRegions.current.language.name = 'fr'
        mockRegions.current.locale = null

        await productsService.getSpecifications('M0001', 'alao', 'ap', 'metric', 3, MODEL_TYPE.PRODUCT)

        expect(mockUtils.axiosWithTimeout).toHaveBeenCalled()
        const callArgs = mockUtils.axiosWithTimeout.mock.calls[0][0]
        expect(callArgs.url).toContain('locale=fr-FR')
      })

      it('should use it-IT locale for Italian language', async () => {
        mockRegions.current.language.name = 'it'
        mockRegions.current.locale = null

        await productsService.getSpecifications('M0001', 'alao', 'ap', 'metric', 3, MODEL_TYPE.PRODUCT)

        expect(mockUtils.axiosWithTimeout).toHaveBeenCalled()
        const callArgs = mockUtils.axiosWithTimeout.mock.calls[0][0]
        expect(callArgs.url).toContain('locale=it-IT')
      })

      it('should use regions.current.locale when it is set (highest priority)', async () => {
        mockRegions.current.language.name = 'fr'
        mockRegions.current.locale = 'zh-CN'

        await productsService.getSpecifications('M0001', 'alao', 'cn', 'metric', 3, MODEL_TYPE.PRODUCT)

        expect(mockUtils.axiosWithTimeout).toHaveBeenCalled()
        const callArgs = mockUtils.axiosWithTimeout.mock.calls[0][0]
        expect(callArgs.url).toContain('locale=zh-CN')
      })
    })

    describe('Edge cases', () => {
      it('should handle null modelId by returning null', async () => {
        const result = await productsService.getSpecifications(null, 'na', 'na', 'us', 3, MODEL_TYPE.PRODUCT)

        expect(result).toBeNull()
        expect(mockUtils.axiosWithTimeout).not.toHaveBeenCalled()
      })

      it('should handle undefined modelId by returning null', async () => {
        const result = await productsService.getSpecifications(undefined, 'na', 'na', 'us', 3, MODEL_TYPE.PRODUCT)

        expect(result).toBeNull()
        expect(mockUtils.axiosWithTimeout).not.toHaveBeenCalled()
      })

      it('should handle empty string modelId by returning null', async () => {
        const result = await productsService.getSpecifications('', 'na', 'na', 'us', 3, MODEL_TYPE.PRODUCT)

        expect(result).toBeNull()
        expect(mockUtils.axiosWithTimeout).not.toHaveBeenCalled()
      })

      it('should handle missing language in NA map gracefully', async () => {
        mockRegions.current.language.name = 'unknown'
        mockRegions.current.locale = null

        await productsService.getSpecifications('M0001', 'na', 'na', 'us', 3, MODEL_TYPE.PRODUCT)

        expect(mockUtils.axiosWithTimeout).toHaveBeenCalled()
        const callArgs = mockUtils.axiosWithTimeout.mock.calls[0][0]
        expect(callArgs.url).toContain('locale=undefined')
      })

      it('should handle missing language in ROW map gracefully', async () => {
        mockRegions.current.language.name = 'unknown'
        mockRegions.current.locale = null

        await productsService.getSpecifications('M0001', 'emea', 'eu', 'metric', 3, MODEL_TYPE.PRODUCT)

        expect(mockUtils.axiosWithTimeout).toHaveBeenCalled()
        const callArgs = mockUtils.axiosWithTimeout.mock.calls[0][0]
        expect(callArgs.url).toContain('locale=undefined')
      })
    })
  })

  describe('downloadPdfSpecification()', () => {
    beforeEach(() => {
      mockAxios.$get.mockResolvedValue(new Blob())
    })

    describe('NA business unit', () => {
      it('should use en-US locale for English language when regions.current.locale is not set', async () => {
        mockRegions.current.language.name = 'en'
        mockRegions.current.locale = null

        await productsService.downloadPdfSpecification('M0001', 'na', 'us', MODEL_TYPE.PRODUCT)

        expect(mockAxios.$get).toHaveBeenCalled()
        const url = mockAxios.$get.mock.calls[0][0]
        expect(url).toContain('locale=en-US')
      })

      it('should use es-US locale for Spanish language when regions.current.locale is not set', async () => {
        mockRegions.current.language.name = 'es'
        mockRegions.current.locale = null

        await productsService.downloadPdfSpecification('M0001', 'na', 'us', MODEL_TYPE.PRODUCT)

        expect(mockAxios.$get).toHaveBeenCalled()
        const url = mockAxios.$get.mock.calls[0][0]
        expect(url).toContain('locale=es-US')
      })

      it('should use regions.current.locale when it is set (highest priority)', async () => {
        mockRegions.current.language.name = 'en'
        mockRegions.current.locale = 'fr-CA'

        await productsService.downloadPdfSpecification('M0001', 'na', 'us', MODEL_TYPE.PRODUCT)

        expect(mockAxios.$get).toHaveBeenCalled()
        const url = mockAxios.$get.mock.calls[0][0]
        expect(url).toContain('locale=fr-CA')
      })
    })

    describe('EMEA business unit', () => {
      it('should use de-DE locale for German language when regions.current.locale is not set', async () => {
        mockRegions.current.language.name = 'de'
        mockRegions.current.locale = null

        await productsService.downloadPdfSpecification('M0001', 'emea', 'metric', MODEL_TYPE.PRODUCT)

        expect(mockAxios.$get).toHaveBeenCalled()
        const url = mockAxios.$get.mock.calls[0][0]
        expect(url).toContain('locale=de-DE')
      })

      it('should use es-ES locale for Spanish language (not es-US)', async () => {
        mockRegions.current.language.name = 'es'
        mockRegions.current.locale = null

        await productsService.downloadPdfSpecification('M0001', 'emea', 'metric', MODEL_TYPE.PRODUCT)

        expect(mockAxios.$get).toHaveBeenCalled()
        const url = mockAxios.$get.mock.calls[0][0]
        expect(url).toContain('locale=es-ES')
        expect(url).not.toContain('locale=es-US')
      })

      it('should use regions.current.locale when it is set (highest priority)', async () => {
        mockRegions.current.language.name = 'de'
        mockRegions.current.locale = 'de-AT'

        await productsService.downloadPdfSpecification('M0001', 'emea', 'metric', MODEL_TYPE.PRODUCT)

        expect(mockAxios.$get).toHaveBeenCalled()
        const url = mockAxios.$get.mock.calls[0][0]
        expect(url).toContain('locale=de-AT')
      })
    })

    describe('ALAO business unit', () => {
      it('should use fr-FR locale for French language when regions.current.locale is not set', async () => {
        mockRegions.current.language.name = 'fr'
        mockRegions.current.locale = null

        await productsService.downloadPdfSpecification('M0001', 'alao', 'metric', MODEL_TYPE.PRODUCT)

        expect(mockAxios.$get).toHaveBeenCalled()
        const url = mockAxios.$get.mock.calls[0][0]
        expect(url).toContain('locale=fr-FR')
      })

      it('should use regions.current.locale when it is set (highest priority)', async () => {
        mockRegions.current.language.name = 'fr'
        mockRegions.current.locale = 'zh-CN'

        await productsService.downloadPdfSpecification('M0001', 'alao', 'metric', MODEL_TYPE.PRODUCT)

        expect(mockAxios.$get).toHaveBeenCalled()
        const url = mockAxios.$get.mock.calls[0][0]
        expect(url).toContain('locale=zh-CN')
      })
    })
  })

  describe('getCompareModels()', () => {
    beforeEach(() => {
      mockConfig.PRODUCTS_API = 'products'
      mockAxios.get = jest.fn().mockResolvedValue({data: {results: [{id: 'M0259'}], total: 1}})
    })

    it('requests current models by node id with the current language', async () => {
      mockRegions.current.language.name = 'fr'

      const result = await productsService.getCompareModels({id: 'uuid-subcategory'})

      expect(mockAxios.get).toHaveBeenCalledWith(`${mockConfig.BOBCAT_API_SERVER}products/compare/models`, {
        params: {id: 'uuid-subcategory', lang: 'fr'},
        useCache: true
      })
      expect(result).toEqual([{id: 'M0259'}])
    })

    it('requests by path when no id is given', async () => {
      await productsService.getCompareModels({path: '/bobcat/na/Loaders/Skid-Steer-Loaders'})

      expect(mockAxios.get.mock.calls[0][1].params).toEqual({path: '/bobcat/na/Loaders/Skid-Steer-Loaders', lang: 'en'})
    })

    it('returns an empty list without a request when neither id nor path is given', async () => {
      const result = await productsService.getCompareModels()

      expect(result).toEqual([])
      expect(mockAxios.get).not.toHaveBeenCalled()
    })
  })

  describe('Locale map constants verification', () => {
    it('NA_PRODUCT_API_LOCALES should contain en and es mappings', () => {
      expect(NA_PRODUCT_API_LOCALES.en).toBe('en-US')
      expect(NA_PRODUCT_API_LOCALES.es).toBe('es-US')
    })

    it('ROW_PRODUCT_API_LOCALES should contain European locales', () => {
      expect(ROW_PRODUCT_API_LOCALES.de).toBe('de-DE')
      expect(ROW_PRODUCT_API_LOCALES.fr).toBe('fr-FR')
      expect(ROW_PRODUCT_API_LOCALES.es).toBe('es-ES')
      expect(ROW_PRODUCT_API_LOCALES.it).toBe('it-IT')
    })

    it('Spanish locale should differ between NA and ROW', () => {
      expect(NA_PRODUCT_API_LOCALES.es).toBe('es-US')
      expect(ROW_PRODUCT_API_LOCALES.es).toBe('es-ES')
      expect(NA_PRODUCT_API_LOCALES.es).not.toBe(ROW_PRODUCT_API_LOCALES.es)
    })
  })
})
