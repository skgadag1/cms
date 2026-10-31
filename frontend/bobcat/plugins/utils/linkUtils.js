import {mapUrlToSliced} from './path'

const ALLOWED_HREF_PROTOCOLS = new Set(['http:', 'https:', 'mailto:', 'tel:'])

/**
 * Block dangerous schemes (javascript:, data:, vbscript:, …) in href values.
 * Relative paths, hash-only, and protocol-relative URLs are allowed.
 * Vue 2 does not sanitize javascript: in :href (unlike Vue 3).
 */
const isSafeHref = (url) => {
  if (!url || typeof url !== 'string') {
    return true
  }
  const trimmed = url.trim()
  if (!trimmed) {
    return true
  }
  if (trimmed.startsWith('/') || trimmed.startsWith('#') || trimmed.startsWith('?') || trimmed.startsWith('./') || trimmed.startsWith('../')) {
    return true
  }
  // protocol-relative //example.com — not a javascript: XSS vector
  if (trimmed.startsWith('//')) {
    return true
  }
  // bare path / query without a scheme
  if (!/^[a-zA-Z][a-zA-Z0-9+.-]*:/.test(trimmed)) {
    return true
  }
  try {
    return ALLOWED_HREF_PROTOCOLS.has(new URL(trimmed).protocol)
  } catch (e) {
    return false
  }
}

const appendAnchor = (url, anchorID) => {
  if (!anchorID) {
    return url
  }
  const hash = anchorID.startsWith('#') ? anchorID : `#${anchorID}`
  if (!url) {
    return hash
  }
  return `${url}${hash}`
}

const resolveLinkItem = (_sItem, _t, _lang) => {
  if (!_t || !_sItem) {
    return null
  }

  if (_t === 'localAnchor') {
    const anchorID = _sItem.anchorID
    if (!anchorID) {
      return null
    }
    return {
      id: _sItem['@id'],
      url: appendAnchor(null, anchorID),
      label: _sItem.localAnchorLabel || _sItem['@name'],
      target: _sItem.target,
      type: _t
    }
  }

  if (_t === 'none' || !_sItem[_t + 'Url']) {
    return null
  }

  let url = _t === 'page' ? mapUrlToSliced(_sItem[_t + 'Url'], _lang) : _sItem[_t + 'Url']
  if (_t === 'page') {
    url = appendAnchor(url, _sItem.anchorID)
  }

  if (!isSafeHref(url)) {
    return null
  }

  return {
    id: _sItem['@id'],
    url,
    label: _sItem[_t + 'Label'] || _sItem['@name'],
    target: _sItem.target,
    type: _t
  }
}

export default {
  getLinkField: (_node, _lang) => {
    return _node
      ? _node?.['@nodes']?.map((x) => {
        const _sItem = _node[x]
        const _t = _node?.field
        return resolveLinkItem(_sItem, _t, _lang)
      }).filter(x => x)[0]
      : null
  },
  getLinks: (_node, _lang) => {
    return _node
      ? _node?.['@nodes']?.map((x) => {
        const _sItem = _node[x]
        const _t = _sItem?.field
        if (!_t || !_sItem.link) {
          return null
        }
        const resolved = resolveLinkItem(_sItem.link, _t, _lang)
        if (!resolved) {
          return null
        }
        return {
          ...resolved,
          id: _sItem['@id'],
          label: _sItem.link[_t + 'Label'] ||
            _sItem.link.localAnchorLabel ||
            _sItem['@name']
        }
      }).filter(x => x)
      : []
  },
  isExternalLink: (anchor) => {
    if (anchor) {
      const relAttribute = anchor?.getAttribute('rel')
      const isNoopener = relAttribute && relAttribute?.includes('noopener')
      const isNofollow = relAttribute && relAttribute?.includes('nofollow')
      const isTargetBlank = anchor.target === '_blank'

      return (isNoopener || isNofollow) && isTargetBlank
    }
  }
}
