export const EVOLUTION_COMPARE_QUERY_KEY = 'compare'

/**
 * @param {string[]} modelIds
 */
export function serializeCompareQuery(modelIds) {
  const ids = (modelIds || []).filter(Boolean)
  return ids.length ? ids.join(',') : ''
}

/**
 * @param {string|undefined|null} raw
 * @returns {string[]}
 */
export function parseCompareQuery(raw) {
  if (!raw || typeof raw !== 'string') {
    return []
  }
  return raw.split(',').map((s) => s.trim()).filter(Boolean)
}
