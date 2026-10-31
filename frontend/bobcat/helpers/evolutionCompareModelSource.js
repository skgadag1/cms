import {PRODUCT_NODE_TYPES} from '~/constants/magnolia'

export function dedupeProductsById(products) {
  const byId = new Map()
  products.forEach((p) => {
    const key = p?.id || p?.['@id']
    if (key && !byId.has(key)) {
      byId.set(key, p)
    }
  })
  return [...byId.values()].sort((a, b) => (a.title || a['@name'] || '').localeCompare(b.title || b['@name'] || ''))
}

const PATH_PREFIX_NODE_TYPES = new Set([
  PRODUCT_NODE_TYPES.CATEGORY,
  PRODUCT_NODE_TYPES.SUBCATEGORY,
  PRODUCT_NODE_TYPES.PRODUCT_GROUP
])

export function normalizeJcrPath(path) {
  if (path == null || path === '') {
    return ''
  }
  const p = String(path).trim()
  return p.length > 1 && p.endsWith('/') ? p.slice(0, -1) : p
}

export function isPathUnderExcluded(productPath, excludedPaths) {
  const p = normalizeJcrPath(productPath)
  if (!p || !excludedPaths?.length) {
    return false
  }
  return excludedPaths.some((prefix) => {
    const base = normalizeJcrPath(prefix)
    if (!base) {
      return false
    }
    return p === base || p.startsWith(`${base}/`)
  })
}

function productPath(product) {
  return normalizeJcrPath(product?.['@path'] || product?.path)
}

function collectDescendantProductIds(node, ids, seen = new Set()) {
  if (!node || typeof node !== 'object' || seen.has(node)) {
    return
  }
  seen.add(node)
  const nodeType = String(node['@nodeType'] || '')
  if (nodeType === PRODUCT_NODE_TYPES.PRODUCT) {
    if (node['@id']) {
      ids.add(String(node['@id']))
    }
    return
  }
  const nodeNames = node['@nodes'] || []
  nodeNames.forEach((name) => {
    collectDescendantProductIds(node[name], ids, seen)
  })
  Object.keys(node)
    .filter(k => !k.startsWith('@'))
    .forEach((k) => {
      const child = node[k]
      if (child && typeof child === 'object') {
        collectDescendantProductIds(child, ids, seen)
      }
    })
}

function applyNodeExclusion(node, ids, paths) {
  if (!node || typeof node !== 'object') {
    return
  }
  const nodeType = String(node['@nodeType'] || '')
  if (nodeType === PRODUCT_NODE_TYPES.PRODUCT && node['@id']) {
    ids.add(String(node['@id']))
    return
  }
  if (PATH_PREFIX_NODE_TYPES.has(nodeType) && node['@path']) {
    paths.push(normalizeJcrPath(node['@path']))
    collectDescendantProductIds(node, ids)
    return
  }
  if (node['@path']) {
    paths.push(normalizeJcrPath(node['@path']))
    collectDescendantProductIds(node, ids)
  } else if (node['@id']) {
    ids.add(String(node['@id']))
  }
}

function listMultiLinkEntries(excludedModels) {
  if (!excludedModels) {
    return []
  }
  if (Array.isArray(excludedModels)) {
    return excludedModels
  }
  if (typeof excludedModels !== 'object') {
    return []
  }
  const nodeNames = excludedModels['@nodes']
  if (Array.isArray(nodeNames) && nodeNames.length) {
    return nodeNames.map(name => excludedModels[name]).filter(Boolean)
  }
  const numbered = Object.keys(excludedModels)
    .filter(k => /^excludedModels\d+$/i.test(k))
    .sort()
    .map(k => excludedModels[k])
    .filter(Boolean)
  if (numbered.length) {
    return numbered
  }
  return Object.keys(excludedModels)
    .filter(k => !k.startsWith('@'))
    .map(k => excludedModels[k])
    .filter(v => v && typeof v === 'object')
}

function accumulateExcludedEntry(entry, ids, paths) {
  if (!entry) {
    return
  }
  if (typeof entry === 'string') {
    ids.add(entry.trim())
    return
  }
  if (typeof entry !== 'object') {
    return
  }
  if (entry['@nodeType']) {
    applyNodeExclusion(entry, ids, paths)
    return
  }
  const inner = entry.excludedModel ?? entry.field ?? entry.value
  if (typeof inner === 'string') {
    ids.add(inner.trim())
  } else if (inner && typeof inner === 'object') {
    applyNodeExclusion(inner, ids, paths)
  } else if (entry['@id'] || entry['@path']) {
    applyNodeExclusion(entry, ids, paths)
  }
}

/**
 * Builds { ids, paths } from Magnolia excludedModel links (expanded in page/component delivery).
 * Subcategory / product group / category selections add a path prefix so every descendant
 * product in flatModels is removed; expanded child products are also collected when present.
 */
export function extractExcludedSelectors(excludedModels) {
  const ids = new Set()
  const paths = []
  listMultiLinkEntries(excludedModels).forEach(entry => accumulateExcludedEntry(entry, ids, paths))
  return {ids, paths}
}

async function fetchExcludedNode($services, id) {
  const fromProducts = await $services.bobcatapi.getProduct(id, null)
  if (fromProducts?.['@path']) {
    return fromProducts
  }
  const fromCategories = await $services.bobcatapi.getCategories([id])
  return fromCategories?.[0] || null
}

/**
 * Like extractExcludedSelectors, but resolves bare UUID strings via the products API when
 * page delivery did not expand excludedModel references (e.g. legacy cached responses).
 */
export async function resolveExcludedSelectors($services, excludedModels) {
  const ids = new Set()
  const paths = []
  const pendingIds = []

  listMultiLinkEntries(excludedModels).forEach((entry) => {
    if (typeof entry === 'string') {
      pendingIds.push(entry.trim())
      return
    }
    if (!entry || typeof entry !== 'object') {
      return
    }
    if (entry['@nodeType']) {
      applyNodeExclusion(entry, ids, paths)
      return
    }
    const inner = entry.excludedModel ?? entry.field ?? entry.value
    if (typeof inner === 'string') {
      pendingIds.push(inner.trim())
    } else if (inner && typeof inner === 'object') {
      applyNodeExclusion(inner, ids, paths)
    } else if (entry['@id'] || entry['@path']) {
      applyNodeExclusion(entry, ids, paths)
    }
  })

  const uniquePending = [...new Set(pendingIds.filter(Boolean))]
  await Promise.all(uniquePending.map(async (id) => {
    const node = await fetchExcludedNode($services, id)
    if (node) {
      applyNodeExclusion(node, ids, paths)
    } else {
      ids.add(id)
    }
  }))

  return {ids, paths}
}

/**
 * Removes products that are explicitly excluded by the editor (per-instance).
 * Matches by @id (exact) and @path prefix (subcategory, product group, or category).
 */
export function applyExcludedModels(products, excludedSelectors) {
  if (!excludedSelectors) {
    return products
  }
  const {ids, paths} = excludedSelectors
  const normalizedPaths = (paths || []).map(normalizeJcrPath).filter(Boolean)
  if ((!ids || !ids.size) && !normalizedPaths.length) {
    return products
  }
  return products.filter((m) => {
    const id = String(m?.['@id'] || '')
    if (id && ids?.has(id)) {
      return false
    }
    if (isPathUnderExcluded(productPath(m), normalizedPaths)) {
      return false
    }
    return true
  })
}

/**
 * Retries a single request on transient failures (5xx or no response/network error).
 * Scoped to one request so a transient failure retries only that request instead of
 * restarting the whole compare load.
 */
export async function fetchWithRetry(fn, retries = 2, delayMs = 1500) {
  for (let attempt = 0; attempt <= retries; attempt++) {
    try {
      return await fn()
    } catch (err) {
      const status = err?.response?.status
      const isRetryable = status >= 500 || !status
      if (!isRetryable || attempt === retries) {
        throw err
      }
      await new Promise(resolve => setTimeout(resolve, delayMs * (attempt + 1)))
    }
  }
}

/**
 * Current models of a category or subcategory from the compare models endpoint.
 * Items are lightweight (id, @id, @path, @name, title, parent.parentTitle);
 * full product data is loaded per selected slot with loadCompareModelDetails.
 * @param {string} nodeId Magnolia UUID of the category or subcategory
 */
export async function collectCompareModels($services, nodeId) {
  if (!nodeId) {
    return []
  }
  const models = await fetchWithRetry(() => $services.products.getCompareModels({id: nodeId}))
  return dedupeProductsById(models || [])
}

/**
 * Loads the full product node for a lightweight compare model (images, links, indicators,
 * pricing and offer fields). Falls back to the lightweight model when the lookup fails,
 * which still carries everything the specifications request needs (id, @path).
 */
export async function loadCompareModelDetails($services, model) {
  if (!model?.['@id']) {
    return model
  }
  try {
    const full = await $services.bobcatapi.getProduct(model['@id'], null)
    if (!full?.['@id']) {
      return model
    }
    return {...full, parent: full.parent || model.parent, title: full.title || model.title}
  } catch (err) {
    // eslint-disable-next-line no-console
    console.error('[compare] model details fetch failed:', err)
    return model
  }
}
