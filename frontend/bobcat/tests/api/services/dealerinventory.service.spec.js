// Setup environment variables for testing
process.env.NUXT_DEFAULT_LANGUAGE = process.env.NUXT_DEFAULT_LANGUAGE || 'en'
const mockConfig = {
  BOBCAT_API_SERVER: process.env.BOBCAT_API_SERVER || 'https://test.api.com/',
  PRODUCTS_API: process.env.PRODUCTS_API || '/v1/products',
  DEALER_INVENTORY_API: process.env.DEALER_INVENTORY_API || '/dealer-inventory/v1',
  DEALER_INVENTORY_API_V2: process.env.DEALER_INVENTORY_API_V2 || '/dealer-inventory/v2'
}

import dealerInventoryService, {shouldIncludeLocaleParams, getDefaultLocaleForLang} from '~/api/services/dealerinventory.service'
import {DEALER_INVENTORY_LOCALIZED_REGIONS, BUSINESS_REGIONS, NA_PRODUCT_API_LOCALES, ROW_PRODUCT_API_LOCALES} from '~/constants'

describe('[dealerinventory.service] Helper Functions', () => {
  describe('shouldIncludeLocaleParams()', () => {
    it('should return true for NA region', () => {
      const mockRegions = {
        current: {
          name: 'na'
        }
      }

      const result = shouldIncludeLocaleParams(mockRegions)

      expect(result).toBe(true)
    })

    it('should return false for EMEA region (eu)', () => {
      const mockRegions = {
        current: {
          name: 'eu'
        }
      }

      const result = shouldIncludeLocaleParams(mockRegions)

      expect(result).toBe(false)
    })

    it('should return false for EMEA region (mea)', () => {
      const mockRegions = {
        current: {
          name: 'mea'
        }
      }

      const result = shouldIncludeLocaleParams(mockRegions)

      expect(result).toBe(false)
    })

    it('should return false for ALAO region (ap)', () => {
      const mockRegions = {
        current: {
          name: 'ap'
        }
      }

      const result = shouldIncludeLocaleParams(mockRegions)

      expect(result).toBe(false)
    })

    it('should return false for ALAO region (la)', () => {
      const mockRegions = {
        current: {
          name: 'la'
        }
      }

      const result = shouldIncludeLocaleParams(mockRegions)

      expect(result).toBe(false)
    })

    it('should return false when regions is null', () => {
      const result = shouldIncludeLocaleParams(null)

      expect(result).toBe(false)
    })

    it('should return false when regions.current is undefined', () => {
      const mockRegions = {}

      const result = shouldIncludeLocaleParams(mockRegions)

      expect(result).toBe(false)
    })

    it('should return false when regions.current.name is undefined', () => {
      const mockRegions = {
        current: {}
      }

      const result = shouldIncludeLocaleParams(mockRegions)

      expect(result).toBe(false)
    })

    it('should use DEALER_INVENTORY_LOCALIZED_REGIONS constant', () => {
      // Verify the constant is being used correctly
      expect(DEALER_INVENTORY_LOCALIZED_REGIONS).toContain('na')
      expect(DEALER_INVENTORY_LOCALIZED_REGIONS).toEqual(['na'])
    })
  })

  describe('getDefaultLocaleForLang()', () => {
    describe('NA region', () => {
      it('should return en-US for English language', () => {
        const mockRegions = {
          current: {
            name: 'na',
            locale: null
          }
        }

        const result = getDefaultLocaleForLang(mockRegions, 'en')

        expect(result).toBe('en-US')
      })

      it('should return es-US for Spanish language', () => {
        const mockRegions = {
          current: {
            name: 'na',
            locale: null
          }
        }

        const result = getDefaultLocaleForLang(mockRegions, 'es')

        expect(result).toBe('es-US')
      })

      it('should return explicit locale when set, ignoring language', () => {
        const mockRegions = {
          current: {
            name: 'na',
            locale: 'en-CA'
          }
        }

        const result = getDefaultLocaleForLang(mockRegions, 'en')

        expect(result).toBe('en-CA')
      })

      it('should return null for unsupported language', () => {
        const mockRegions = {
          current: {
            name: 'na',
            locale: null
          }
        }

        const result = getDefaultLocaleForLang(mockRegions, 'fr')

        expect(result).toBeNull()
      })
    })

    describe('EMEA region', () => {
      it('should return de-DE for German language in EU region', () => {
        const mockRegions = {
          current: {
            name: 'eu',
            locale: null
          }
        }

        const result = getDefaultLocaleForLang(mockRegions, 'de')

        expect(result).toBe('de-DE')
      })

      it('should return fr-FR for French language in EU region', () => {
        const mockRegions = {
          current: {
            name: 'eu',
            locale: null
          }
        }

        const result = getDefaultLocaleForLang(mockRegions, 'fr')

        expect(result).toBe('fr-FR')
      })

      it('should return en-GB for English language in EU region', () => {
        const mockRegions = {
          current: {
            name: 'eu',
            locale: null
          }
        }

        const result = getDefaultLocaleForLang(mockRegions, 'en')

        expect(result).toBe('en-GB')
      })

      it('should return es-ES for Spanish language in EU region (not es-US)', () => {
        const mockRegions = {
          current: {
            name: 'eu',
            locale: null
          }
        }

        const result = getDefaultLocaleForLang(mockRegions, 'es')

        expect(result).toBe('es-ES')
      })

      it('should return ru-RU for Russian language in MEA region', () => {
        const mockRegions = {
          current: {
            name: 'mea',
            locale: null
          }
        }

        const result = getDefaultLocaleForLang(mockRegions, 'ru')

        expect(result).toBe('ru-RU')
      })

      it('should return explicit locale when set in EMEA region', () => {
        const mockRegions = {
          current: {
            name: 'eu',
            locale: 'de-AT'
          }
        }

        const result = getDefaultLocaleForLang(mockRegions, 'de')

        expect(result).toBe('de-AT')
      })
    })

    describe('ALAO region', () => {
      it('should use ROW locale map for ALAO regions', () => {
        const mockRegions = {
          current: {
            name: 'ap',
            locale: null
          }
        }

        const result = getDefaultLocaleForLang(mockRegions, 'en')

        expect(result).toBe('en-GB')
      })

      it('should use ROW locale map for LA region', () => {
        const mockRegions = {
          current: {
            name: 'la',
            locale: null
          }
        }

        const result = getDefaultLocaleForLang(mockRegions, 'es')

        expect(result).toBe('es-ES')
      })
    })

    describe('Edge cases', () => {
      it('should handle null regions gracefully', () => {
        const result = getDefaultLocaleForLang(null, 'en')

        // Should try to access localeMap with 'en' but regions is null
        expect(result).toBeDefined()
      })

      it('should handle undefined current region', () => {
        const mockRegions = {}

        const result = getDefaultLocaleForLang(mockRegions, 'en')

        expect(result).toBeDefined()
      })

      it('should handle undefined region name', () => {
        const mockRegions = {
          current: {
            locale: null
          }
        }

        const result = getDefaultLocaleForLang(mockRegions, 'en')

        // Should default to ROW since region name is undefined
        expect(result).toBe('en-GB')
      })
    })

    describe('Undefined protection logic', () => {
      it('should return null when language is undefined', () => {
        const mockRegions = {
          current: {
            name: 'na',
            locale: null
          }
        }

        const result = getDefaultLocaleForLang(mockRegions, undefined)

        expect(result).toBeNull()
      })

      it('should return null when language is not in locale map', () => {
        const mockRegions = {
          current: {
            name: 'na',
            locale: null
          }
        }

        const result = getDefaultLocaleForLang(mockRegions, 'unsupported-lang')

        expect(result).toBeNull()
      })

      it('should return null when language is null', () => {
        const mockRegions = {
          current: {
            name: 'na',
            locale: null
          }
        }

        const result = getDefaultLocaleForLang(mockRegions, null)

        expect(result).toBeNull()
      })

      it('should return null for empty string language', () => {
        const mockRegions = {
          current: {
            name: 'na',
            locale: null
          }
        }

        const result = getDefaultLocaleForLang(mockRegions, '')

        expect(result).toBeNull()
      })

      it('should handle undefined $regions parameter', () => {
        const result = getDefaultLocaleForLang(undefined, 'en')

        expect(result).toBe('en-GB')
      })

      it('should handle $regions with undefined current property', () => {
        const mockRegions = {
          current: undefined
        }

        const result = getDefaultLocaleForLang(mockRegions, 'en')

        expect(result).toBe('en-GB')
      })

      it('should return explicit locale even when language is undefined', () => {
        const mockRegions = {
          current: {
            name: 'na',
            locale: 'fr-CA'
          }
        }

        const result = getDefaultLocaleForLang(mockRegions, undefined)

        expect(result).toBe('fr-CA')
      })

      it('should prioritize explicit locale over language mapping', () => {
        const mockRegions = {
          current: {
            name: 'na',
            locale: 'custom-locale'
          }
        }

        const result = getDefaultLocaleForLang(mockRegions, 'en')

        expect(result).toBe('custom-locale')
      })
    })

    describe('Business unit mapping', () => {
      it('should correctly identify NA business unit', () => {
        // NA business unit only contains 'na'
        expect(BUSINESS_REGIONS.na).toEqual(['na'])
      })

      it('should correctly identify EMEA business unit', () => {
        // EMEA business unit contains eu, mea, cis
        expect(BUSINESS_REGIONS.emea).toContain('eu')
        expect(BUSINESS_REGIONS.emea).toContain('mea')
        expect(BUSINESS_REGIONS.emea).toContain('cis')
      })

      it('should correctly identify ALAO business unit', () => {
        // ALAO business unit contains ap, oc, la, kr, in, sea, cn, br
        expect(BUSINESS_REGIONS.alao).toContain('ap')
        expect(BUSINESS_REGIONS.alao).toContain('la')
        expect(BUSINESS_REGIONS.alao).toContain('br')
      })
    })

    describe('Locale map integrity', () => {
      it('should have NA locale mappings for en and es', () => {
        expect(NA_PRODUCT_API_LOCALES.en).toBe('en-US')
        expect(NA_PRODUCT_API_LOCALES.es).toBe('es-US')
      })

      it('should have ROW locale mappings for multiple languages', () => {
        expect(ROW_PRODUCT_API_LOCALES.en).toBe('en-GB')
        expect(ROW_PRODUCT_API_LOCALES.es).toBe('es-ES')
        expect(ROW_PRODUCT_API_LOCALES.de).toBe('de-DE')
        expect(ROW_PRODUCT_API_LOCALES.fr).toBe('fr-FR')
      })

      it('should have different Spanish locale for NA vs ROW', () => {
        expect(NA_PRODUCT_API_LOCALES.es).toBe('es-US')
        expect(ROW_PRODUCT_API_LOCALES.es).toBe('es-ES')
        expect(NA_PRODUCT_API_LOCALES.es).not.toBe(ROW_PRODUCT_API_LOCALES.es)
      })
    })
  })
})

describe('[dealerinventory.service] API Methods', () => {
  describe('getModelDetails()', () => {
    let mockAxios
    let mockRegions
    let service

    beforeEach(() => {
      mockAxios = {
        get: jest.fn().mockResolvedValue({data: {modelId: '123', name: 'Test Model'}})
      }
      mockRegions = {
        current: {
          name: 'na',
          locale: null,
          language: {
            name: 'en'
          }
        }
      }
      service = dealerInventoryService(mockAxios, mockRegions, mockConfig)
    })

    it('should call axios with correct URL and basic params', async () => {
      const dealerID = 'dealer123'
      const modelID = 'model456'
      const queryParams = {
        'product-type': 'skid-steer-loaders',
        level: 'model'
      }

      await service.getModelDetails(dealerID, modelID, true, true, queryParams)

      expect(mockAxios.get).toHaveBeenCalledWith(
        'https://test.api.com//dealer-inventory/v1/inventories/dealer123/models/model456',
        expect.objectContaining({
          params: expect.objectContaining({
            productType: 'skid-steer-loaders',
            level: 'model',
            pricingEnabled: true,
            felBannerEnabled: true,
            noerror: true,
            lang: 'en',
            locale: 'en-US'
          })
        })
      )
    })

    it('should include locale params for NA region', async () => {
      await service.getModelDetails('dealer1', 'model1', true, true, {})

      const callArgs = mockAxios.get.mock.calls[0][1]

      expect(callArgs.params).toHaveProperty('lang', 'en')
      expect(callArgs.params).toHaveProperty('locale', 'en-US')
    })

    it('should not include locale params for non-NA region', async () => {
      mockRegions.current.name = 'eu'
      service = dealerInventoryService(mockAxios, mockRegions, mockConfig)

      await service.getModelDetails('dealer1', 'model1', true, true, {})

      const callArgs = mockAxios.get.mock.calls[0][1]

      expect(callArgs.params).not.toHaveProperty('lang')
      expect(callArgs.params).not.toHaveProperty('locale')
    })

    it('should omit pricingEnabled when false', async () => {
      await service.getModelDetails('dealer1', 'model1', false, true, {})

      const callArgs = mockAxios.get.mock.calls[0][1]

      expect(callArgs.params).not.toHaveProperty('pricingEnabled')
    })

    it('should omit felBannerEnabled when false', async () => {
      await service.getModelDetails('dealer1', 'model1', true, false, {})

      const callArgs = mockAxios.get.mock.calls[0][1]

      expect(callArgs.params).not.toHaveProperty('felBannerEnabled')
    })

    it('should use default language when language is not set', async () => {
      mockRegions.current.language = undefined
      service = dealerInventoryService(mockAxios, mockRegions, mockConfig)

      await service.getModelDetails('dealer1', 'model1', true, true, {})

      const callArgs = mockAxios.get.mock.calls[0][1]

      expect(callArgs.params).toHaveProperty('lang', 'en')
    })

    it('should use explicit locale when set', async () => {
      mockRegions.current.locale = 'en-CA'
      service = dealerInventoryService(mockAxios, mockRegions, mockConfig)

      await service.getModelDetails('dealer1', 'model1', true, true, {})

      const callArgs = mockAxios.get.mock.calls[0][1]

      expect(callArgs.params).toHaveProperty('locale', 'en-CA')
    })

    it('should not include locale param when getDefaultLocaleForLang returns null', async () => {
      mockRegions.current.language = {name: 'unsupported-language'}
      service = dealerInventoryService(mockAxios, mockRegions, mockConfig)

      await service.getModelDetails('dealer1', 'model1', true, true, {})

      const callArgs = mockAxios.get.mock.calls[0][1]

      expect(callArgs.params).toHaveProperty('lang', 'unsupported-language')
      expect(callArgs.params).not.toHaveProperty('locale')
    })

    it('should always include noerror param', async () => {
      await service.getModelDetails('dealer1', 'model1', false, false, {})

      const callArgs = mockAxios.get.mock.calls[0][1]

      expect(callArgs.params).toHaveProperty('noerror', true)
    })

    it('should handle queryParams with undefined values', async () => {
      const queryParams = {
        'product-type': undefined,
        level: 'model'
      }

      await service.getModelDetails('dealer1', 'model1', true, true, queryParams)

      const callArgs = mockAxios.get.mock.calls[0][1]

      expect(callArgs.params).toHaveProperty('productType', undefined)
      expect(callArgs.params).toHaveProperty('level', 'model')
    })

    it('should handle missing queryParams', async () => {
      await service.getModelDetails('dealer1', 'model1', true, true)

      const callArgs = mockAxios.get.mock.calls[0][1]

      expect(callArgs.params).toHaveProperty('productType', undefined)
      expect(callArgs.params).toHaveProperty('level', undefined)
    })

    it('should handle Spanish language in NA region', async () => {
      mockRegions.current.language = {name: 'es'}
      service = dealerInventoryService(mockAxios, mockRegions, mockConfig)

      await service.getModelDetails('dealer1', 'model1', true, true, {})

      const callArgs = mockAxios.get.mock.calls[0][1]

      expect(callArgs.params).toHaveProperty('lang', 'es')
      expect(callArgs.params).toHaveProperty('locale', 'es-US')
    })

    it('should handle ROW region with German language', async () => {
      mockRegions.current.name = 'eu'
      mockRegions.current.language = {name: 'de'}
      service = dealerInventoryService(mockAxios, mockRegions, mockConfig)

      await service.getModelDetails('dealer1', 'model1', true, true, {})

      const callArgs = mockAxios.get.mock.calls[0][1]

      expect(callArgs.params).not.toHaveProperty('lang')
      expect(callArgs.params).not.toHaveProperty('locale')
    })

    it('should handle axios errors gracefully', async () => {
      const originalProcessServer = process.server
      process.server = true

      mockAxios.get.mockRejectedValue(new Error('Network error'))

      const result = await service.getModelDetails('dealer1', 'model1', true, true, {})

      expect(result).toBeUndefined()

      process.server = originalProcessServer
    })

    it('should return data from axios response', async () => {
      const mockData = {modelId: 'test', name: 'Test Model', price: 50000}
      mockAxios.get.mockResolvedValue({data: mockData})

      const result = await service.getModelDetails('dealer1', 'model1', true, true, {})

      expect(result).toEqual(mockData)
    })

    it('should construct correct URL with dealer and model IDs', async () => {
      await service.getModelDetails('dealer-abc-123', 'model-xyz-789', true, true, {})

      expect(mockAxios.get).toHaveBeenCalledWith(
        'https://test.api.com//dealer-inventory/v1/inventories/dealer-abc-123/models/model-xyz-789',
        expect.any(Object)
      )
    })

    it('should validate complete axios options structure', async () => {
      const queryParams = {
        'product-type': 'compact-track-loaders',
        level: 'series'
      }

      await service.getModelDetails('dealer1', 'model1', true, true, queryParams)

      expect(mockAxios.get).toHaveBeenCalledTimes(1)
      expect(mockAxios.get).toHaveBeenCalledWith(
        expect.stringContaining('/inventories/dealer1/models/model1'),
        {
          params: {
            productType: 'compact-track-loaders',
            level: 'series',
            pricingEnabled: true,
            felBannerEnabled: true,
            noerror: true,
            lang: 'en',
            locale: 'en-US'
          }
        }
      )
    })
  })
})
