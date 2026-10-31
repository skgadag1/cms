import {EVOLUTION_COMPARE_QUERY_KEY, serializeCompareQuery} from '~/helpers/evolutionCompareQuery'

export function firstProductGalleryImage(model) {
  const images = model?.images
  const firstImageAsset = (images?.['@nodes'] || [])
    .map(name => images?.[name]?.asset)
    .find(asset => asset?.field === 'img' && asset?.img?.src)
  return firstImageAsset?.img || model?.image || null
}

export function mapModelSelectorCard(model) {
  return {
    ...model,
    image: firstProductGalleryImage(model),
    title: model.name,
    description: model.shortDescription,
    properties: (model.keyProperties || []).map(property => ({
      ...property,
      value: property.value ? `${property.value}&nbsp;${property.unit || ''}` : '&nbsp;'
    }))
  }
}

/**
 * The compare page keeps the catalog model id (e.g. M0201) in its query, so deep links
 * from a listing card use the same id rather than the JCR uuid.
 */
export function buildModelCompareHref(baseUrl, model) {
  if (!baseUrl) {
    return null
  }
  const id = String(model?.id || model?.['@id'] || '').trim()
  if (!id) {
    return null
  }
  const hashIndex = baseUrl.indexOf('#')
  const pathAndQuery = hashIndex >= 0 ? baseUrl.slice(0, hashIndex) : baseUrl
  const hash = hashIndex >= 0 ? baseUrl.slice(hashIndex) : ''
  const separator = pathAndQuery.includes('?') ? '&' : '?'
  return `${pathAndQuery}${separator}${EVOLUTION_COMPARE_QUERY_KEY}=${serializeCompareQuery([id])}${hash}`
}
