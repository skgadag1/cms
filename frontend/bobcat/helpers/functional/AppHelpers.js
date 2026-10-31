export function removeCurrentLanguage(string, currentLanguage) {
  if (!string) { return null }
  const _langPattern = `/${currentLanguage}`
  if (string.endsWith(_langPattern) && string.indexOf(_langPattern) === string.length - _langPattern.length) { return string.replace(_langPattern, '') }
  return string.replace(`${_langPattern}/`, '/')
}

export function getVersion(path) {
  return new URLSearchParams(path).get('mgnlVersion')
}

export function addYoutubeNoCookie(content) {
  return content?.replace(/(src="(?:https?:)?\/\/(?:(?:www|m)\.)?)((?:youtube\.com|youtu.be))(\/(?:[\w-]+\?v=|embed\/|live\/|v\/)?[\w-]+\S+?)/g, '$1youtube-nocookie.com$3')
}

export function stripEmptyParagraphs(content) {
  return content?.replace(/<p>\s*(&nbsp;|\u00A0)?\s*<\/p>/gi, '').trim() || ''
}

export function treeSearch(tree, value, key = 'id', reverse = false) {
  const stack = [tree[0]]
  while (stack.length) {
    const node = stack[reverse ? 'pop' : 'shift']()
    if (node[key] === value) { return node }
    node.items && stack.push(...node.items)
  }
  return null
}

// default expiry value is 24 hours
export function setWithExpiry(key, value, ttl = 24 * 60 * 60 * 1000) {
  const now = new Date()

  if (!process.client) { return }

  const item = {
    value,
    expiry: now.getTime() + ttl
  }
  localStorage.setItem(key, JSON.stringify(item))
}

export function getWithExpiry(key) {
  if (!process.client) { return null }

  const itemStr = localStorage.getItem(key)
  if (!itemStr) {
    return null
  }
  const item = JSON.parse(itemStr)
  const now = new Date()
  if (now.getTime() > item.expiry) {
    localStorage.removeItem(key)
    return null
  }
  return item.value
}

export function removeWithExpiry(key) {
  if (!process.client) { return }

  localStorage.removeItem(key)
}

export function convertToPlain(html = '', applyAdditionalValidationCheck = false) {
  const plainText = html.replace(/<[^>]*>/g, '')

  if (applyAdditionalValidationCheck) {
    return plainText.replace(/&nbsp;/g, ' ')
      .replace(/\n/g, ' ')
      .replace(/\t/g, ' ')
      .trim()
  }

  return plainText || ''
}

export function getNodeName(item) {
  if (!item) { return null }
  if (item['@name']) { return item['@name'] }
  if (item.jcrName) { return item.jcrName }
  const path = item['@path'] || item.path
  if (typeof path === 'string' && path.length) {
    const segments = path.split('/').filter(Boolean)
    if (segments.length) { return segments[segments.length - 1] }
  }
  return null
}

export function appendVariantParam(url, variantName) {
  if (!url || !variantName) { return url }
  const [pathAndQuery, hash] = String(url).split('#')
  if (/(?:^|[?&])variant=/.test(pathAndQuery)) { return url }
  const separator = pathAndQuery.includes('?') ? '&' : '?'
  const withParam = `${pathAndQuery}${separator}variant=${encodeURIComponent(variantName)}`
  return hash !== undefined ? `${withParam}#${hash}` : withParam
}

export default {}

// Placeholder tokens that should be treated as "no value" (e.g. in schema markup).
const SCHEMA_PLACEHOLDER_VALUES = new Set(['n/a', 'na', 'unknown', 'null', 'undefined', '-'])

// Returns a trimmed value, or '' when the value is empty or a known placeholder
export function cleanValue(value) {
  if (value === null || value === undefined) {
    return ''
  }
  const trimmed = String(value).trim()
  if (!trimmed || SCHEMA_PLACEHOLDER_VALUES.has(trimmed.toLowerCase())) {
    return ''
  }
  return trimmed
}

// Returns '' when no usable phone number is present.
export function formatPhone(rawPhone, isNARegion) {
  if (!rawPhone) {
    return ''
  }
  const trimmed = String(rawPhone).trim()
  if (!trimmed) {
    return ''
  }
  // Already in international format (starts with +): keep digits after the +.
  if (trimmed.startsWith('+')) {
    const digits = trimmed.replace(/[^\d]/g, '')
    return digits ? `+${digits}` : ''
  }
  const digits = trimmed.replace(/[^\d]/g, '')
  if (!digits) {
    return ''
  }
  // NA dealers: ensure a country code is present (+1).
  if (isNARegion) {
    if (digits.length === 10) {
      return `+1-${digits.slice(0, 3)}-${digits.slice(3, 6)}-${digits.slice(6)}`
    }
    if (digits.length === 11 && digits.startsWith('1')) {
      const national = digits.slice(1)
      return `+1-${national.slice(0, 3)}-${national.slice(3, 6)}-${national.slice(6)}`
    }
  }
  return `+${digits}`
}
