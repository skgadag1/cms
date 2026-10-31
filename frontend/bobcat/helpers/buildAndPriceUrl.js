import {
  BUILD_AND_PRICE_CATALOG_ROOT,
  BUILD_AND_PRICE_FIELDS,
  BUILD_AND_PRICE_PAGE,
  BUILD_AND_PRICE_QUERY,
  BUILD_AND_PRICE_REGION
} from '~/constants/buildAndPrice'

export function buildAndPriceUrl(product, {region, language} = {}) {
  if (region !== BUILD_AND_PRICE_REGION || !language || !isBuildAndPriceEnabled(product)) {
    return null
  }

  const segments = catalogSegments(product, region)
  const category = segments[0] || ''
  const subcategory = segments.length > 2 ? segments[1] : ''
  const model = modelFromTitle(product)
  if (!category || !model) {
    return null
  }

  const params = new URLSearchParams()
  params.set(BUILD_AND_PRICE_QUERY.CATEGORY, category)
  if (subcategory) {
    params.set(BUILD_AND_PRICE_QUERY.SUBCATEGORY, subcategory)
  }
  params.set(BUILD_AND_PRICE_QUERY.MODEL, model)

  return `/${region}/${language}/${BUILD_AND_PRICE_PAGE}?${params.toString()}`
}

const isBuildAndPriceEnabled = (product) => {
  const value = product?.[BUILD_AND_PRICE_FIELDS.ENABLED]
  return value !== false && value !== 'false'
}

const catalogSegments = (product, region) => {
  const path = product?.['@path'] || product?.path || ''
  const segments = String(path).split('/').map(segment => segment.trim()).filter(Boolean)
  if (segments[0]?.toLowerCase() === BUILD_AND_PRICE_CATALOG_ROOT) {
    segments.shift()
  }
  if (segments[0]?.toLowerCase() === String(region).toLowerCase()) {
    segments.shift()
  }
  return segments.map(segment => segment.toLowerCase())
}

const modelFromTitle = (product) => {
  const title = product?.title || product?.name || ''
  return String(title).trim().toLowerCase()
}
