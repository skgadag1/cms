import {fixKey} from '~/plugins/utils/productUtils'

const CONCEPT_URL_REGEX = /(https?:\/\/[^" ]*)/i

export const EVOLUTION_COMPARE_SPEC_SECTION_ICON_CONCEPT_PATHS = [
  '/common/labels/specifications/compare_spec_section_icons',
  '/%region%/common/labels/specifications/compare_spec_section_icons'
]

export function extractUrlFromConceptValue(value) {
  if (value == null || value === '') {
    return null
  }
  const s = String(value).trim()
  const httpMatch = s.match(CONCEPT_URL_REGEX)
  if (httpMatch?.[0]) {
    return httpMatch[0]
  }
  if (s.startsWith('cloudinary:')) {
    return s
  }
  return null
}

export function buildSectionTitleToConceptImageMap(concepts) {
  const map = {}
  if (!concepts || typeof concepts !== 'object') {
    return map
  }
  for (const [rawKey, value] of Object.entries(concepts)) {
    if (String(rawKey).startsWith('@')) {
      continue
    }
    const k = fixKey(String(rawKey))
    const url = extractUrlFromConceptValue(value)
    if (k && url) {
      map[k] = url
    }
  }
  return map
}

export function mergeSectionTitleConceptImageMaps(...sources) {
  const map = {}
  for (const source of sources) {
    const built = buildSectionTitleToConceptImageMap(source)
    for (const [k, v] of Object.entries(built)) {
      if (k && v) {
        map[k] = v
      }
    }
  }
  return map
}

export async function fetchEvolutionCompareSpecSectionIconConcepts(
  bobcatapi,
  region,
  siteBasename = ''
) {
  const maps = await Promise.all(
    EVOLUTION_COMPARE_SPEC_SECTION_ICON_CONCEPT_PATHS.map(path =>
      bobcatapi.fetchSingleConceptPath(`${siteBasename}${path}`.replace('%region%', region))
    )
  )
  return Object.assign({}, ...maps)
}

export function getSectionIconImageFromConcepts(sectionTitle, conceptImageMap) {
  if (!sectionTitle || !conceptImageMap) {
    return null
  }
  return conceptImageMap[fixKey(String(sectionTitle))] || null
}

export function resolveSectionIconDisplay(sectionTitle, {conceptImageMap = {}, bootstrapIconMap = {}} = {}) {
  if (!sectionTitle) {
    return {type: 'none'}
  }
  const imageSrc = getSectionIconImageFromConcepts(sectionTitle, conceptImageMap)
  if (imageSrc) {
    return {type: 'image', src: imageSrc}
  }
  const key = fixKey(String(sectionTitle))
  const iconName = bootstrapIconMap[key]
  if (iconName) {
    return {
      type: 'icon',
      name: String(iconName).toLowerCase().replace(/\s/g, '-')
    }
  }
  const fallback = String(sectionTitle).toLowerCase().replace(/\s/g, '-')
  return fallback ? {type: 'icon', name: fallback} : {type: 'none'}
}

/**
 * Normalizes CMS multilink / array / JSON string / plain icon record into rows for {@link buildSectionTitleToIconMap}.
 *
 * Supported `raw` shapes:
 * - Array of `{ pddSectionTitle | sectionTitle, iconName | icon }` (unchanged)
 * - Magnolia composite object with `@nodes` and child nodes
 * - JSON string of an array or object map (e.g. from concepts `compareSpecSectionIcons`)
 * - Plain object map `{ "Engine": "gear", "Performance": "speedometer2" }` (keys = section titles as in PDD)
 *
 * @param {unknown} raw
 * @returns {Array<{ pddSectionTitle?: string, sectionTitle?: string, iconName?: string, icon?: string }>}
 */
export function coerceSpecSectionIconMappingsList(raw) {
  if (raw == null || raw === '') {
    return []
  }
  if (typeof raw === 'string') {
    const s = raw.trim()
    if (!s) {
      return []
    }
    try {
      const parsed = JSON.parse(s)
      return coerceSpecSectionIconMappingsList(parsed)
    } catch {
      return []
    }
  }
  if (Array.isArray(raw)) {
    return raw.filter(Boolean)
  }
  if (typeof raw === 'object') {
    const nodes = raw['@nodes']
    if (nodes?.length) {
      return nodes.map(k => raw[k]).filter(Boolean)
    }
    return Object.entries(raw)
      .filter(([k]) => !String(k).startsWith('@'))
      .map(([pddSectionTitle, iconName]) => ({pddSectionTitle, iconName}))
      .filter(row => row.pddSectionTitle != null && String(row.pddSectionTitle).trim() && String(row.iconName || '').trim())
  }
  return []
}

/**
 * @param {Array<{ pddSectionTitle?: string, sectionTitle?: string, iconName?: string, icon?: string }>} list
 * @returns {Record<string, string>} fixKey(sectionTitle) -> icon name for b-icon
 */
export function buildSectionTitleToIconMap(list) {
  const map = {}
  for (const row of list) {
    const title = row?.pddSectionTitle || row?.sectionTitle
    const icon = row?.iconName || row?.icon
    const k = title != null ? fixKey(String(title)) : ''
    if (k && icon) {
      map[k] = String(icon).trim()
    }
  }
  return map
}

function stripHtml(s) {
  if (typeof s !== 'string') {
    return s
  }
  return s.replace(/<[^>]*>/g, '').replace(/\s+/g, ' ').trim()
}

/**
 * @param {unknown} cell — ProductSpec row cell: object with value/unit, string, or missing
 * @param {unknown} [rowUnitFallback] — row-level unit (same as UI: cell.unit || row.unit)
 * @returns {string}
 */
export function cellToComparableString(cell, rowUnitFallback) {
  if (cell == null) {
    return ''
  }
  if (typeof cell === 'string') {
    return stripHtml(cell)
  }
  if (typeof cell === 'object') {
    if (cell.type === 'table' && cell.value?.items) {
      try {
        return JSON.stringify(cell.value.items)
      } catch {
        return '[table]'
      }
    }
    const rowU = rowUnitFallback != null && String(rowUnitFallback).trim() !== '' ? String(rowUnitFallback).trim() : ''
    const v = cell.value
    if (Array.isArray(v)) {
      return v
        .map((x) => {
          const base = stripHtml(String(x?.value ?? ''))
          const u = (x?.unit != null && String(x.unit).trim() !== '' ? String(x.unit).trim() : '') || rowU
          return u ? `${base}_${u}` : base
        })
        .join('|')
    }
    if (v != null && typeof v === 'object') {
      return stripHtml(String(v))
    }
    if (v != null) {
      const base = stripHtml(String(v))
      const cellU = cell.unit != null && String(cell.unit).trim() !== '' ? String(cell.unit).trim() : ''
      const u = cellU || rowU
      return u ? `${base}_${u}` : base
    }
    return ''
  }
  return String(cell)
}

/**
 * @param {Record<string, unknown>} row — one spec row (label + per-modelId cells)
 * @param {string[]} modelIds
 * @returns {boolean} true if every present model has the same comparable value (missing counts as '')
 */
export function rowEntirelySameAcrossModels(row, modelIds) {
  if (!modelIds?.length) {
    return true
  }
  const rowUnit = row?.unit
  const vals = modelIds.map(id => cellToComparableString(row[id], rowUnit))
  const first = vals[0]
  return vals.every(v => v === first)
}

/**
 * Drops spec rows where all model values compare equal.
 * @param {Record<string, { title?: string, properties?: Record<string, unknown> }>} sections — tableData.sections
 * @param {string[]} modelIds
 * @returns {typeof sections}
 */
export function filterSectionsForDifferencesOnly(sections, modelIds) {
  if (!sections || !modelIds?.length) {
    return sections || {}
  }
  const out = {}
  for (const key of Object.keys(sections)) {
    const section = sections[key]
    const props = section?.properties || {}
    const filteredProps = {}
    for (const pk of Object.keys(props)) {
      const row = props[pk]
      if (!rowEntirelySameAcrossModels(row, modelIds)) {
        filteredProps[pk] = row
      }
    }
    if (Object.keys(filteredProps).length) {
      out[key] = {...section, properties: filteredProps}
    }
  }
  return out
}

/**
 * Collapsed "Show more" preview: first section(s) with at most N property rows each.
 * Skips sections with no properties.
 *
 * @param {Record<string, { title?: string, properties?: Record<string, unknown> }>} sections
 * @param {{ maxSections?: number, maxRowsPerSection?: number }} [opts]
 * @returns {typeof sections}
 */
export function sliceSpecsToPreview(sections, opts = {}) {
  const maxSections = opts.maxSections ?? 1
  const maxRowsPerSection = opts.maxRowsPerSection ?? 1
  if (!sections || typeof sections !== 'object') {
    return {}
  }
  const out = {}
  let sectionCount = 0
  for (const sk of Object.keys(sections)) {
    const section = sections[sk]
    const props = section?.properties || {}
    const propKeys = Object.keys(props)
    if (!propKeys.length) {
      continue
    }
    const take = propKeys.slice(0, maxRowsPerSection)
    const sliced = {}
    take.forEach((pk) => {
      sliced[pk] = props[pk]
    })
    out[sk] = {...section, properties: sliced}
    sectionCount += 1
    if (sectionCount >= maxSections) {
      break
    }
  }
  return out
}
