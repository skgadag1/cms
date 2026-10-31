import {USER_COVEO_METADATA} from '~/constants'
export default {
  init: async (orgId, tokenUri, locationMarkers, regionInfo, addMarker, setMapCenter, dictionaryFieldContext, sortLocations) => {
    await customElements.whenDefined('atomic-search-interface')
    const searchInterface = document.querySelector('atomic-search-interface')
    let counter = 0
    const tokenSession = `coveo_token_${regionInfo.language}_${regionInfo.name}`

    const getSearchToken = async (checkStorage) => {
      try {
        if (checkStorage) {
          const coveoToken = localStorage.getItem(tokenSession)
          if (coveoToken) {
            return coveoToken
          }
        }
        const response = await fetch(tokenUri)
        if (response.status === 200) {
          const data = JSON.parse(await response.text() || '')
          localStorage.setItem(tokenSession, data.token)
          return data?.token
        }
      } catch (err) {
        // eslint-disable-next-line no-console
        console.error(err)
      }
    }

    let origin = {}

    await searchInterface.initialize({
      accessToken: await getSearchToken(true),
      renewAccessToken: getSearchToken,
      organizationId: orgId,
      organizationEndpoints: await searchInterface.getOrganizationEndpoints(orgId),
      search: {
        preprocessSearchResponseMiddleware: async (response) => {
          let centerIsSet = false
          const destinations = response.body.results.map(result => result.raw.bobc_address)
          if (destinations.length > 0) {
            const sorted = await sortLocations(origin, destinations)
            if (sorted.length > 0) {
              response.body.results = sorted.map((item) => {
                const result = response.body.results[item.index]
                result.raw = {
                  ...result.raw,
                  googleData: item.value
                }
                return result
              })
            }
          }
          response.body.results = response.body.results.map((result, index) => {
            // E.g., modify the result object
            const lat = result.raw.bobc_latitude
            const lng = result.raw.bobc_longitude
            if (lat && lng && !centerIsSet) {
              setMapCenter(lat, lng)
              centerIsSet = true
            }
            result.raw = {
              ...result.raw,
              index: counter + index + 1
            }
            if (lat && lng) {
              const marker = {
                lat,
                lng
              }

              addMarker(marker, counter + index + 1)

              locationMarkers.push({
                position: marker
              })
            }
            return result
          })
          return response
        }
      },
      preprocessRequest: (request, clientOrigin, metadata) => {
        if (clientOrigin === 'searchApiFetch') {
          let body = JSON.parse(request.body)
          if (metadata?.method === 'search') {
            body.dictionaryFieldContext = dictionaryFieldContext
            if (body.firstResult === 0) {
              locationMarkers.forEach((marker) => {
                marker.map = null
              })
            }

            counter = body.firstResult || 0
            if (body.context && body.context.lat && body.context.lng) {
              origin = {
                lat: body.context.lat,
                lng: body.context.lng
              }
              body.queryFunctions = [
                {
                  fieldName: '@distance',
                  function: `dist(@bobc_latitude, @bobc_longitude, ${body.context.lat}, ${body.context.lng})`
                },
                {
                  fieldName: '@distanceinmiles',
                  function: '@distance*0.000621371'
                },
                {
                  fieldName: '@distanceinkm',
                  function: '@distance*0.0009941936'
                }
              ]
            }
          } else if (metadata?.method === 'facetSearch') {
            body = {
              ...body,
              searchContext: {
                ...body.searchContext,
                context: {
                  ...body.searchContext.context,
                  method: 'facetSearch'
                },
                dictionaryFieldContext
              },
              ignoreValues: []
            }
          }

          request.body = JSON.stringify(body)
        }

        return request
      }
    })

    return searchInterface
  },
  setUserMetadataStorage: (state, productFacetField, industryFacetField, servicesFacetField) => {
    const {query = {}, facetSet = {}, context = {}, dealerAddress} = state
    localStorage.setItem(USER_COVEO_METADATA, JSON.stringify({
      q: query?.q || '',
      products: extractSelectedValuesFromFacet(facetSet, productFacetField),
      industry: extractSelectedValuesFromFacet(facetSet, industryFacetField),
      services: extractSelectedValuesFromFacet(facetSet, servicesFacetField),
      location: {
        lat: context?.contextValues?.lat,
        lng: context?.contextValues?.lng
      },
      dealerAddress
    }))
  }
}

const extractSelectedValuesFromFacet = (facetSet, field) => {
  const facet = facetSet?.[field]
  if (!facet || !facet.request || !facet.request.currentValues) {
    return []
  }

  return facet.request.currentValues
    .filter(item => item.state === 'selected')
    .map(item => item.value)
}

export const getValidWebsiteUrl = (url) => {
  if (!url) { return '' }
  const hasProtocol = /^(https?:)?\/\//i.test(url)
  return hasProtocol ? url : `https://${url}`
}
