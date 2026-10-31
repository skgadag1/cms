/* eslint-disable no-console */
import {getURLwithQuery} from '~/utils/url/query'
const hasMocks = process.env.MOCK_MAGNOLIA > 0
const LABELS_BASE_PATH = process?.env?.VUE_APP_SITE_BASENAME

export default ($axios, $regions, $config) => {
  const isLocal = process.env.IS_LOCAL === true || process.env.IS_LOCAL === 'true'
  const useNative = $config.USE_NATIVE_MAGNOLIA
  const LABELS_PATH = $config.NUXT_LABELS_DEFAULT_PATH

  const CURRENT_LANGUAGE = $regions?.current?.language?.name || process.env.NUXT_DEFAULT_LANGUAGE

  return {
    async fetchSiteMap(_region = process.env.NUXT_DEFAULT_REGION) {
      let data = []

      try {
        if (!isLocal && !hasMocks && $config?.BOBCAT_API_SERVER) {
          const url = `${$config.BOBCAT_API_SERVER}${$config.BOBCAT_API_NAVIGATION}${process.env.VUE_APP_SITE_BASENAME}`
          const options = {
            params: {lang: CURRENT_LANGUAGE, region: _region, instance: $config.INSTANCE_NAME},
            useCache: true
          }

          const response = await $axios.get(url, options)
          data = response?.data
        } else {
          const bobcatMock = () => import('~/server/responses/content-api/navigationResponse' /* webpackChunkName: 'bobcat-pagenav-mock' */)
          data = (await bobcatMock()).default
        }
      } catch (e) {
        console.debug('[magnolia.service] Unable to fetch sitemap', e)
        if (!process.server) {
          throw new Error(e)
        }
      }

      return data
    },

    async fetchSingleConceptPath(_path = '/') {
      const url = useNative
        ? `${$config.MAGNOLIA_CONTENT_API_SERVER.replace('api/', '')}${$config.BOBCAT_API_CONCEPTS.replace('content/%INSTANCE%/', '').replace('%PATH%', _path)}`
        : `${$config.BOBCAT_API_SERVER}${$config.BOBCAT_API_CONCEPTS.replace('%INSTANCE%', $config.INSTANCE_NAME).replace('%PATH%', _path)}`
      const _labels = {}
      try {
        const {data} = await $axios.get(url, {params: {lang: CURRENT_LANGUAGE, noerror: true}, useCache: true})
        data.forEach((x) => {
          _labels[x['@name']] = x[`value${x.value}`]
        })
      } catch {
        console.debug(`[magnolia.service] Unable to fetch concepts ${_path}`)
      }
      return _labels
    },

    async getConcepts(_paths = [], region = $regions.current.name) {
      _paths.unshift('/%region%' + LABELS_PATH)
      _paths.unshift(LABELS_PATH)
      const requests = _paths.map(x => `${LABELS_BASE_PATH}${x}`.replace('%region%', region))
      const _labels = {}
      try {
        const translationCalls = requests.map(x => this.fetchSingleConceptPath(x))
        await Promise.allSettled(translationCalls).then(r => Object.assign(_labels, ...r.map(x => x.value)))
      } catch {
        console.debug(`[magnolia.service] Unable to fetch concepts ${_paths}`)
      }
      return _labels
    },

    async fetchNavigation(_region = $regions.current.name || process.env.NUXT_DEFAULT_REGION) {
      let _snavItems = []

      try {
        _snavItems = await this.fetchSiteMap(`${_region}`)
      } catch (e) {
        console.debug('[magnolia.service] Unable to parse sitemap object', e)
        if (!process.server) {
          throw new Error(e)
        }
      }

      return _snavItems
    },

    async getConfig(_path = '', throwError = true) {
      const _options = {params: {lang: CURRENT_LANGUAGE}}
      if (!throwError) {
        _options.params.noerror = true
      }

      try {
        const url = `${$config.BOBCAT_API_SERVER}${$config.BOBCAT_API_CONFIG.replace('%INSTANCE%', $config.INSTANCE_NAME).replace('%PATH%', _path)}`

        return await $axios.get(url, _options)
      } catch (e) {
        console.debug('[magnolia.service] Unable to fetch configuration', e)
        if (!process.server && throwError) {
          throw new Error(e)
        }
      }
    },

    async getProduct(_id, _path = null, _noerror = null) {
      if (!_id && !_path) {
        return
      }
      const baseUrl = useNative
        ? `${$config.MAGNOLIA_CONTENT_API_SERVER.replace('.rest/api/', '.rest/')}${$config.BOBCAT_API_PRODUCTS.replace('content/%INSTANCE%/', '').replace('%PATH%', _path || '')}`
        : `${$config.BOBCAT_API_SERVER}${$config.BOBCAT_API_PRODUCTS.replace('%INSTANCE%', $config.INSTANCE_NAME).replace('%PATH%', _path || '')}`

      const url = getURLwithQuery(baseUrl, {
        lang: CURRENT_LANGUAGE,
        ...(_id && {'@jcr:uuid': _id}),
        ...(_noerror && {noerror: true})
      })
      const opts = {useCache: true}

      const {data} = await $axios.get(url, opts)
      return data?.results?.[0] || data
    },

    async getAttachment(_id) {
      if (!_id) {
        return
      }
      const url = useNative
        ? `${$config.MAGNOLIA_CONTENT_API_SERVER.replace('.rest/api/', '.rest/')}${$config.BOBCAT_API_ATTACHMENTS.replace('content/%INSTANCE%/', '')}`
        : `${$config.BOBCAT_API_SERVER}${$config.BOBCAT_API_ATTACHMENTS.replace('%INSTANCE%', $config.INSTANCE_NAME)}`
      const opts = {
        params: {
          lang: CURRENT_LANGUAGE,
          '@jcr:uuid': _id
        },
        useCache: true
      }

      const {data} = await $axios.get(url, opts)
      return data?.results?.[0] || data
    },

    async fetchDxpTags(_ids = []) {
      if (!_ids.length) {
        return
      }
      const url = `${$config.BOBCAT_API_SERVER}${$config.BOBCAT_API_TAGS.replace('%INSTANCE%', $config.INSTANCE_NAME)}`
      const opts = {
        params: {
          lang: CURRENT_LANGUAGE,
          '@jcr:uuid': _ids.join('|'),
          limit: _ids.length
        },
        useCache: true
      }

      const {data} = await $axios.get(url, opts)
      return data?.results || data
    },

    async getCategoryModels(_path = '/') {
      try {
        const url = `${$config.BOBCAT_API_SERVER}${$config.BOBCAT_API_PRODUCTS.replace('%INSTANCE%', $config.INSTANCE_NAME).replace('%PATH%', _path)}@nodes`
        const opts = {
          params: {
            lang: CURRENT_LANGUAGE,
            noerror: true
          },
          headers: {
            'Cache-Control': 'max-age=360',
            Expires: '360'
          },
          useCache: true
        }

        return await $axios.get(url, opts)
      } catch (e) {
        console.debug('[magnolia.service] Unable to fetch category models', e)
        if (!process.server) {
          // Rethrow the original error so callers keep response.status (e.g. fetchWithRetry
          // can tell a non-retryable 4xx from a transient 5xx/network error). Wrapping it in
          // new Error(e) here dropped response and made every failure look like a network error.
          throw e
        }
      }
    },

    async getCategories(_ids = [], _path = '') {
      try {
        const url = useNative
          ? `${$config.MAGNOLIA_CONTENT_API_SERVER.replace('.rest/api/', '.rest/')}${$config.BOBCAT_API_CATEGORIES.replace('content/%INSTANCE%/', '').replace('%PATH%', _path)}`
          : `${$config.BOBCAT_API_SERVER}${$config.BOBCAT_API_CATEGORIES.replace('%INSTANCE%', $config.INSTANCE_NAME).replace('%PATH%', _path)}`
        const opts = {
          params: {
            ...(_path && {noerror: true}),
            lang: CURRENT_LANGUAGE,
            limit: _ids?.length || 200,
            ...(_ids?.length && {'@jcr:uuid': _ids.join('|')})
          }
        }
        if (_ids?.length) {
          const _allCats = (await $axios.get(url, opts))?.data?.results
          return _ids?.map(id => _allCats.find(c => c['@id'] === id))?.filter(x => x) || []
        } else if (_path) {
          return [(await $axios.get(url, opts))?.data]
        }
      } catch (e) {
        console.debug('[magnolia.service] Unable to fetch categories', e)
        if (!process.server) {
          throw new Error(e)
        }
      }
    },

    async getFacets(path) {
      try {
        const url = useNative
          ? `${$config.MAGNOLIA_CONTENT_API_SERVER.replace('.rest/api/', '.rest/')}${$config.BOBCAT_API_FACETS.replace('content/%INSTANCE%/', '').replace('%PATH%', path).replace('%REGION%', $regions.current.name)}`
          : `${$config.BOBCAT_API_SERVER}${$config.BOBCAT_API_FACETS.replace('%INSTANCE%', $config.INSTANCE_NAME).replace('%PATH%', path).replace('%REGION%', $regions.current.name)}`
        const {data} = await $axios.get(url, {params: {lang: CURRENT_LANGUAGE, noerror: true}})
        return ({
          data: data?.map((facet) => {
            const facetTageName = facet?.tagFacetId?.tagId?.['@name']
            if (facetTageName && facet?.facetType === 'tagFacets') {
              facet.id = facetTageName
            }
            return facet
          })
        })
      } catch (e) {
        console.debug('[magnolia.service] Unable to fetch products facets', e)
        if (!process.server) {
          throw new Error(e)
        }
      }
    },

    async getFaultCodeDetails(faultCode, serialNumber) {
      try {
        const url = $config.BOBCAT_API_SERVER + $config.BOBCAT_API_FAULT_CODE
        const {data} = await $axios.get(url, {params: {lang: CURRENT_LANGUAGE, faultCode, serialNumber, noerror: true}})
        return data
      } catch (e) {
        console.debug('[magnolia.service] Unable to fetch fault code details', e)
        if (!process.server) {
          throw new Error(e)
        }
      }
    },

    async getSparrowImage(serialNumber) {
      try {
        const region = $regions.current.name
        const url = $config.BOBCAT_API_SERVER + $config.BOBCAT_API_SPARROW_IMAGE
        const {data} = await $axios.get(url, {params: {lang: CURRENT_LANGUAGE, region, serialNumber, noerror: true}})
        return data
      } catch (e) {
        console.debug('[magnolia.service] Unable to fetch sparrow image', e)
        if (!process.server) {
          throw new Error(e)
        }
      }
    },

    async fetchTagsToInject(_path) {
      try {
        const url = `${$config.BOBCAT_API_SERVER}${$config.BOBCAT_API_MARKETING_TAGS.replace('%INSTANCE%', $config.INSTANCE_NAME).replace('%PARAMS_ANCESTOR%', $config.PARAMS_ANCESTOR).replace('%PATH%', _path)}`

        return await $axios.get(url, {params: {lang: CURRENT_LANGUAGE, limit: 100}})
      } catch (e) {
        console.debug('[magnolia.service] Unable to fetch marketing tags to inject', e)
        if (!process.server) {
          throw new Error(e)
        }
      }
    },

    async getCampaignById(id) {
      try {
        const url = `${$config.BOBCAT_API_SERVER}${$config.BOBCAT_API_CAMPAIGNS.replace('%INSTANCE%', $config.INSTANCE_NAME)}`
        const {data} = await $axios.get(url, {params: {pagesField: id, lang: CURRENT_LANGUAGE}})
        return data?.results || []
      } catch (e) {
        console.debug('[magnolia.service] Unable to fetch campaigns', e)
        if (!process.server) {
          throw new Error(e)
        }
      }
    },

    async getFormConfig(_id) {
      try {
        const url = useNative
          ? `${$config.MAGNOLIA_CONTENT_API_SERVER}${$config.BOBCAT_API_FORM_CONFIG.replace('content/public/api/', '')}`
          : $config.BOBCAT_API_SERVER + $config.BOBCAT_API_FORM_CONFIG
        const opts = {
          params: {
            lang: CURRENT_LANGUAGE,
            ...(_id && {'@jcr:uuid': _id})
          }
        }

        const {data} = await $axios.get(url, opts)
        return data?.results?.[0] || []
      } catch (e) {
        console.debug('[magnolia.service] Unable to fetch form config', e)
        if (!process.server) {
          throw new Error(e)
        }
      }
    },

    async getTemplateAnnotations(_path) {
      try {
        const url = useNative
          ? `${$config.MAGNOLIA_CONTENT_API_SERVER.replace('.rest/api/', '.rest/')}${$config.BOBCAT_API_TEMPLATE_ANNOTATIONS.replace('content/%INSTANCE%/', '')}${_path}`
          : `${$config.BOBCAT_API_SERVER}${$config.BOBCAT_API_TEMPLATE_ANNOTATIONS.replace('%INSTANCE%', $config.INSTANCE_NAME)}${_path}`
        const opts = {
          useCache: false,
          lang: CURRENT_LANGUAGE
        }

        return (await $axios.get(url, opts)).data
      } catch (e) {
        console.error(`[magnolia.service] Unable to get template annotations for path ${_path}!`, e)
        if (!process.server) {
          throw new Error(e)
        }
      }
    }
  }
}

export function mapChildren(node, contentType = 'mgnl:page') {
  return node?.['@nodes'].filter(z => node[z]['@nodeType'] === contentType && node[z].hideInNav !== 'true' && node[z].completelyHidden !== 'true').map((y) => {
    return {
      ...node[y],
      children: mapChildren(node[y])
    }
  }) || []
}
