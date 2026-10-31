/* eslint-disable no-console, no-lone-blocks */
import pathUtils from '~/plugins/utils/path'
import {removeCurrentLanguage} from '~/helpers/functional/AppHelpers'
import {FORM_SENDER_TYPES} from '~/constants/formSenders'

const mapFormFields = async (fields, configDetails, sender, securityToken, formCaptcha, getUploader, uploadFile) => {
  const formFields = []
  const attachments = []

  function renameFile(originalFile, newName) {
    return new File([originalFile], newName, {
      type: originalFile.type,
      lastModified: originalFile.lastModified
    })
  }

  // eslint-disable-next-line
  for (const [key, value] of Object.entries(fields)) {
    if (value?.isAttachment) {
      const fileList = value?.value
      for (let i = 0; i < fileList.length; i++) {
        const timestamp = Date.now()
        const name = `${timestamp}_${fileList[i]?.name.replaceAll(',', '')}`
        const file = renameFile(fileList[i], name)
        const uploadURL = await getUploader(name)
        const blob = new Blob([file], {type: file?.type})
        await uploadFile(uploadURL, blob)
        attachments.push(`/${name}`)
      }
    } else if (value?.value) {
      delete value?.isAttachment
      formFields.push(value)
    }
  }

  const data = {
    formName: configDetails['@name'],
    formFields,
    formCaptcha
  }

  const config = {
    headers: {
      securityToken
    },
    params: {
      noerror: true,
      ...(!!attachments.length && {attachments: attachments?.join(',')})
    }
  }

  switch (sender.field) {
  case FORM_SENDER_TYPES.EMAIL : {
    Object.assign(data, {
      formConfiguration: {
        formSubject: fields?.subject?.value || `[${configDetails['@name']}] Form request`,
        emailConf: sender?.link?.email['@path']
      }
    })
  }
    break
  case FORM_SENDER_TYPES.SALESFORCE : {
    Object.assign(data, {
      formConfiguration: {
        requestUrl: sender?.link?.requestUrl
      }
    })
  }
    break
  case FORM_SENDER_TYPES.DYNAMODB : {
    Object.assign(data, {
      formConfiguration: {}
    })
  }
    break
  case FORM_SENDER_TYPES.SALESFORCE_SALESCLOUD : {
    Object.assign(data, {
      formConfiguration: {
        ...(sender?.link?.recordType && {recordType: sender.link.recordType}),
        ...(sender?.link?.currency && {currency: sender.link.currency})
      }
    })
  }
    break
  }
  data.formConfiguration.formType = sender.field

  return {
    config,
    data
  }
}

const preparePageDetails = ($config, pd) => {
  pd['@areaNodes'] = []
  pd.areaNodes = {}
  if ($config.USE_NATIVE_MAGNOLIA) {
    pd['@nodes'].forEach((area) => {
      if (pd[area]['@nodeType'] === 'mgnl:area') {
        pd['@areaNodes'].push(area)
        pd.areaNodes[area] = {...pd[area], areaNodes: []}
        pd[area]['@nodes'].forEach((component) => {
          pd.areaNodes[area].areaNodes.push(pd[area][component]['@id'])
        })
      }
    })
  }
  return pd
}

// Helper function to generate breadcrumbs by walking up the page tree (native API only)
const nativeGenerateBreadcrumbs = async ($axios, $config, pagePath, lang) => {
  const breadcrumbs = []
  const pathSegments = pagePath.split('/').filter(x => x)

  // Build breadcrumbs by fetching each parent page
  for (let i = 0; i < pathSegments.length; i++) {
    const currentPath = '/' + pathSegments.slice(0, i + 1).join('/')

    try {
      const url = `${$config.MAGNOLIA_CONTENT_API_SERVER}${$config.CONTENT_API_PAGES}${removeCurrentLanguage(currentPath, lang)}`
      const {data} = await $axios.get(url, {params: {lang}})
      const pageData = data?.results?.[0] || data?.results || data

      if (pageData) {
        breadcrumbs.push({
          title: pageData.navigationLabel || pageData.title || pageData['@name'],
          url: currentPath
        })
      }
    } catch (e) {
      // Use page name as fallback
      breadcrumbs.push({
        title: pathSegments[i],
        url: currentPath
      })
    }
  }

  return breadcrumbs
}

// Helper function to resolve inherited components from parent pages (native API only)
const nativeResolveInheritedComponents = async ($axios, $config, pagePath, areaName, lang) => {
  // Areas that support inheritance (header and footer)
  const inheritableAreas = ['header', 'footer']
  if (!inheritableAreas.includes(areaName)) {
    return null
  }

  const pathSegments = pagePath.split('/').filter(x => x)

  // Walk up the page tree to find inherited components
  while (pathSegments.length > 0) {
    pathSegments.pop() // Remove last segment to go to parent
    const parentPath = '/' + pathSegments.join('/')

    if (!parentPath || parentPath === '/') {
      break
    }

    try {
      const url = `${$config.MAGNOLIA_CONTENT_API_SERVER}${$config.CONTENT_API_PAGES}${removeCurrentLanguage(parentPath, lang)}`
      const {data} = await $axios.get(url, {params: {lang}})
      const parentData = data?.results?.[0] || data?.results || data

      if (parentData && parentData[areaName] && parentData[areaName]['@nodes'] && parentData[areaName]['@nodes'].length > 0) {
        // Found components in parent area, return them
        const components = []
        parentData[areaName]['@nodes'].forEach((component) => {
          if (parentData[areaName][component]) {
            components.push(parentData[areaName][component]['@id'])
          }
        })
        return components
      }
    } catch (e) {
      // Continue to next parent
    }
  }

  return null
}

// Wrapper method to prepare page details with inheritance resolution for native Magnolia
// This should ONLY be called when _useNative is true (local mode)
const nativePreparePageDetailsWithInheritance = async (pageData, $axios, $config, lang) => {
  // Prepare page structure
  const processedData = preparePageDetails($config, pageData)

  // Generate breadcrumbs (not included in native Magnolia API)
  if (processedData && processedData['@path']) {
    try {
      processedData.breadcrumbs = await nativeGenerateBreadcrumbs($axios, $config, processedData['@path'], lang)
    } catch (e) {
      processedData.breadcrumbs = []
    }
  }

  // Resolve inheritance for empty areas
  if (processedData && processedData['@areaNodes']) {
    for (const areaName of processedData['@areaNodes']) {
      const area = processedData.areaNodes[areaName]
      // Check if area is empty and supports inheritance (header/footer)
      if (area && (!area.areaNodes || area.areaNodes.length === 0)) {
        const inheritedComponents = await nativeResolveInheritedComponents($axios, $config, processedData['@path'], areaName, lang)
        if (inheritedComponents && inheritedComponents.length > 0) {
          // Populate the area with inherited components
          processedData.areaNodes[areaName].areaNodes = inheritedComponents
        }
      }
    }
  }

  return processedData
}

export default ($axios, $regions, $config) => {
  const _useNative = $config.USE_NATIVE_MAGNOLIA
  const _lang = $regions?.current?.language?.name || process.env.NUXT_DEFAULT_LANGUAGE

  return {
    async getListingResults(searchApiPath, facetCategory, category, subcategory = null, config = {}) {
      const filters = config?.filters
      const showAllPages = config?.showAllPages
      const sort = config.sort || false
      const page = config.page || 0
      const perPage = config.perPage || 20
      const pageType = config?.pageType
      const searchText = config?.searchText
      const productType = config?.productType
      const modelIds = config?.modelIds || []
      const displayOnGroupLevel = config?.displayOnGroupLevel
      const hideSelectedVariants = config?.hideSelectedVariants

      const url = ($config.BOBCAT_API_SERVER + $config.BOBCAT_API_SEARCH.replace('%CATEGORY%', searchApiPath))
      if (subcategory === '') {
        subcategory = null
      }
      const params = {
        region: $regions.current.name,
        ...(!!category && {category}),
        ...(!!subcategory && {subcategory}),
        ...(!!filters && {filters}),
        ...(!!searchText && {searchText}),
        ...(!!pageType && {pageType}),
        ...(!!hideSelectedVariants && {hideSelectedVariants}),
        ...(!!displayOnGroupLevel && {displayOnGroupLevel}),
        ...(!!productType && {productType}),
        ...(!!modelIds.length && {modelIds: modelIds.join(',')}),
        facetCategory,
        lang: _lang,
        from: page * perPage,
        size: perPage,
        sort
      }

      if (!showAllPages && searchApiPath === 'page') {
        Object.assign(params, {excludePagesFromSearch: true})
      }

      try {
        const res = await $axios.get(url, {
          params
        })

        if (process.client && searchText?.length && window?.dataLayer && res.data) {
          window.dataLayer.push({event: 'gpm.search_result_click', searchPhrase: searchText})
        }

        return res
      } catch (e) {
        console.debug('[Products Facets Filtering] Unable to fetch', e)
        throw new Error(e)
      }
    },

    async getSearchResultsByType(searchConfig) {
      const url = $config.BOBCAT_API_SERVER + $config.BOBCAT_API_SEARCH.replace('%CATEGORY%', searchConfig.contentType)
      const params = {
        region: $regions.current.name,
        ...(!!searchConfig.tags && {tags: searchConfig.tags.join(',')}),
        ...(!!searchConfig.pageType && {pageType: searchConfig.pageType}),
        ...(!!searchConfig.productCategory && {category: searchConfig.productCategory}),
        ...(!!searchConfig.displayOnGroupLevel && {displayOnGroupLevel: searchConfig.displayOnGroupLevel}),
        ...(!!searchConfig.hideSelectedVariants && {hideSelectedVariants: searchConfig.hideSelectedVariants}),
        ...(!!searchConfig.productSubcategory && {subcategory: searchConfig.productSubcategory}),
        facetCategory: searchConfig.facet,
        lang: _lang,
        from: searchConfig.page * searchConfig.maxItems,
        size: searchConfig.maxItems,
        sort: searchConfig.sort,
        ...(!!searchConfig.model && {model: searchConfig.model})
      }

      try {
        return await $axios.get(url, {
          params
        })
      } catch (e) {
        console.debug('[Content API][Search By Type] Unable to fetch', e)
        throw new Error(e)
      }
    },

    async getClientLocation(req) {
      try {
        let url = `${$config.CONTENT_API_SERVER}${$config.CONTENT_API_LOCATION_DETECTED}`
        const countryCode = req?.headers?.['cloudfront-viewer-country']

        if (countryCode) {
          console.warn(`[Content API] Using detected user country code: ${countryCode}`)
          url = `${$config.CONTENT_API_SERVER}${$config.CONTENT_API_LOCATION}${countryCode}`
        }
        return await $axios.get(url, {params: {noerror: true}})
      } catch (e) {
        console.debug('[Content API] Unable to fetch location', e)
      }
    },

    async getProductSelector(useMapping = true) {
      try {
        const url = `${$config.BOBCAT_API_SERVER}${$config.BOBCAT_API_SEARCH.replace('%CATEGORY%', 'productSelector')}`
        const params = {
          region: $regions.current.name,
          lang: _lang,
          ignoreHavingUrl: true,
          noerror: true,
          useCache: true,
          useMapping
        }
        return await $axios.get(url, {params})
      } catch (e) {
        console.debug('[Content API] Unable to fetch product selector items', e)
      }
    },

    async getUploader(fileName) {
      try {
        const url = $config.BOBCAT_API_SERVER + $config.BOBCAT_API_UPLOADER
        const {data} = await $axios.get(url, {params: {key: fileName}})
        return data?.uploadURL
      } catch (e) {
        console.debug('[magnolia.service] Unable to fetch uploadURL', e)
        if (!process.server) {
          throw new Error(e)
        }
      }
    },

    async uploadFile(url, blob) {
      try {
        const {data} = await $axios.put(url, blob)
        return data
      } catch (e) {
        console.debug('[magnolia.service] Unable to upload static file', e)
        if (!process.server) {
          throw new Error(e)
        }
      }
    },

    async submitForm(_path, fields, configDetails, sender, securityToken, captcha) {
      try {
        const url = `${$config.BOBCAT_API_SERVER}${$config.BOBCAT_API_FORM.replace('%PATH%', _path)}`
        const {data, config} = await mapFormFields(fields, configDetails, sender, securityToken, captcha, this.getUploader, this.uploadFile)
        config.params.instance = $config.INSTANCE_NAME
        return await $axios.post(url, data, config)
      } catch (e) {
        console.debug('[Content API] Unable to submit form', e)
        if (!process.server) {
          throw new Error(e)
        }
      }
    },

    async getToken() {
      try {
        const url = `${$config.BOBCAT_API_SERVER}${$config.BOBCAT_API_TOKEN}`
        return await $axios.post(url)
      } catch (e) {
        console.debug('[Content API] Unable to fetch token', e)
        if (!process.server) {
          throw new Error(e)
        }
      }
    },

    async getModelPageId(id, lifecycle) {
      try {
        const lifecycleStr = lifecycle.charAt(0).toUpperCase() + lifecycle.slice(1)
        const url = `${$config.BOBCAT_API_SERVER}${$config.BOBCAT_API_SEARCH.replace('%CATEGORY%', 'productSelector')}`
        const {data} = await $axios.get(url, {
          params: {
            id,
            lifecycle: lifecycleStr,
            lang: _lang,
            region: $regions?.current?.name
          }
        })
        return data?.results?.[0]['@id'] || ''
      } catch (e) {
        console.debug('[Content API] Unable to fetch model page id', e)
        if (!process.server) {
          throw new Error(e)
        }
      }
    },

    async getModelPage(id, lifecycle) {
      try {
        const lifecycleStr = lifecycle.charAt(0).toUpperCase() + lifecycle.slice(1)
        const url = `${$config.BOBCAT_API_SERVER}${$config.BOBCAT_API_SEARCH.replace('%CATEGORY%', 'productSelector')}`
        const {data} = await $axios.get(url, {
          params: {
            id,
            lifecycle: lifecycleStr,
            lang: _lang,
            region: $regions?.current?.name
          }
        })
        return data?.results?.[0]?.link?.url || ''
      } catch (e) {
        console.debug('[Content API] Unable to fetch model page id', e)
        if (!process.server) {
          throw new Error(e)
        }
      }
    },
    async getAttachmentsProductsAssociations(ids = []) {
      if (!ids.length) {
        return null
      }
      try {
        const url = `${$config.BOBCAT_API_SERVER}${$config.BOBCAT_API_SEARCH.replace('%CATEGORY%', 'attachmentProductSelector')}`
        const options = {
          params: {
            ids: ids.join(','),
            lang: _lang,
            region: $regions?.current?.name
          },
          useCache: true
        }
        return (await $axios.get(url, options))?.data
      } catch (e) {
        console.debug('[Content API] Unable to fetch attachment products associations', e)
        if (!process.server) {
          throw new Error(e)
        }
      }
    },

    async getRelatedAttachments(ids = [], pagination = {}, useRelatedAttachments = true) {
      try {
        const url = `${$config.BOBCAT_API_SERVER}${$config.BOBCAT_API_SEARCH.replace('%CATEGORY%', 'related-attachments')}`
        const options = {
          params: {
            ...(ids.length && {attachmentIds: ids.join(',')}),
            useRelatedAttachments,
            lang: _lang,
            region: $regions?.current?.name,
            ...pagination
          },
          useCache: true
        }
        return (await $axios.get(url, options))
      } catch (e) {
        console.debug('[Content API] Unable to fetch related attachments', e)
        if (!process.server) {
          throw new Error(e)
        }
      }
    },

    async getAttachmentsByIds(ids = [], pagination = {}) {
      if (!ids.length) {
        return null
      }
      try {
        const url = `${$config.BOBCAT_API_SERVER}${$config.BOBCAT_API_SEARCH.replace('%CATEGORY%', 'attachments-by-ids')}`
        const options = {
          params: {
            attachmentIds: ids.join(','),
            lang: _lang,
            region: $regions?.current?.name,
            ...pagination
          },
          useCache: true
        }
        return (await $axios.get(url, options))
      } catch (e) {
        console.debug('[Content API] Unable to fetch attachments by ids', e)
        if (!process.server) {
          throw new Error(e)
        }
      }
    },

    async getCompatibleAttachmentsCount(_ids) {
      try {
        const validIds = _ids?.filter(Boolean)
        if (!validIds?.length) {
          return
        }
        const url = _useNative
          ? `${$config.MAGNOLIA_CONTENT_API_SERVER.replace('.rest/api/', '.rest/')}${$config.BOBCAT_API_PRODUCTS.replace('content/%INSTANCE%/', '').replace('%PATH%', '')}`
          : `${$config.BOBCAT_API_SERVER}${$config.BOBCAT_API_PRODUCTS.replace('%INSTANCE%', $config.INSTANCE_NAME).replace('%PATH%', '')}`
        const opts = {
          params: {
            lang: _lang,
            '@jcr:uuid': validIds.join('|'),
            limit: validIds.length
          },
          useCache: true
        }

        const {data} = await $axios.get(url, opts)
        const products = Array.isArray(data?.results) ? data.results : [data].filter(Boolean)
        return products
          .filter(product => product?.['@id'])
          .map(product => ({
            id: product['@id'],
            compatibleAttachmentsCount: product.compatibleAttachmentsCount
          }))
      } catch (e) {
        console.debug('[Content API] Unable to fetch compatible attachments count', e)
        if (!process.server) {
          throw new Error(e)
        }
      }
    },

    async fetchPageDetails(_route = null) {
      if (!_route) {
        return
      }

      try {
        const options = {params: {lang: _lang, instance: $config.INSTANCE_NAME}}
        let path

        if (_route) {
          const nodeName = process?.env?.VUE_APP_SITE_BASENAME
          const pagePath = pathUtils.pathWithoutBaseName(_route.path, undefined, true)
          let shouldAddLangSuffix = pagePath.startsWith('/regions') || (!_useNative && pagePath.startsWith(`${nodeName}/regions`))
          shouldAddLangSuffix = shouldAddLangSuffix || pagePath.startsWith('/error') || (!_useNative && pagePath.startsWith(`${nodeName}/error`))
          const _routePath = shouldAddLangSuffix ? `${pagePath}/${_lang}` : pagePath

          path = `${_useNative ? nodeName : ''}${_routePath}`
          options.params.path = path
        }

        const url = _useNative
          ? `${$config.MAGNOLIA_CONTENT_API_SERVER}${$config.CONTENT_API_PAGES}${removeCurrentLanguage(path, _lang)}`
          : `${$config.BOBCAT_API_SERVER}${$config.BOBCAT_API_PAGEDETAILS}`

        const {data} = await $axios.get(url, options)
        const _data = data?.results?.[0] || data?.results || data

        // Prepare page details with inheritance resolution (only for local/native mode)
        const processedData = _useNative
          ? await nativePreparePageDetailsWithInheritance(_data, $axios, $config, _lang)
          : _data

        return {data: processedData}
      } catch (e) {
        console.debug('[magnolia.service] Unable to fetch page details', e)
        if (!process.server) {
          throw new Error(e)
        }
      }
    },

    async fetchComponentDetails(_id = null) {
      if (!_id) {
        return
      }

      try {
        const options = {params: {lang: _lang}}
        options.params['@jcr:uuid'] = _id

        const url = _useNative
          ? `${$config.MAGNOLIA_CONTENT_API_SERVER}${$config.BOBCAT_API_COMPONENTS.replace('content/%INSTANCE%/api/', '')}`
          : `${$config.BOBCAT_API_SERVER}${$config.BOBCAT_API_COMPONENTS.replace('%INSTANCE%', $config.INSTANCE_NAME)}`

        const {data} = await $axios.get(url, options)
        const _data = data?.results?.[0] || data?.results || data
        const pageData = {data: _useNative && !_id ? preparePageDetails($config, _data) : _data}

        return pageData
      } catch (e) {
        console.debug(`[magnolia.service] Unable to fetch component ${_id} details ${e}`)
        if (!process.server) {
          throw new Error(e)
        }
      }
    }
  }
}
