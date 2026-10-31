import linkUtils from './linkUtils'

const PAGE_NODE_NAME = process?.env?.VUE_APP_SITE_BASENAME

const pathWithoutBaseName = (_p, nodeName = PAGE_NODE_NAME, removeTrailingSlash = true) => {
  if (!_p) { return }
  const nn = _p.startsWith('/') ? `${nodeName}/` : `/${nodeName}/`
  if (removeTrailingSlash) { _p = _p.endsWith('/') ? _p.slice(0, -1) : _p }
  return (_p.includes(nn) ? _p.substring(_p.indexOf(nn) + nn.length - 1) || '/' : (_p === nn.slice(0, -1) ? '/' : _p)).replace('.html', '')
}

export const mapUrlToSliced = (_p, _lang = process.env.NUXT_DEFAULT_LANGUAGE) => {
  if (!_p || (typeof _p === 'object' && (_p.pageLink?.field === 'none' || !_p.pageLink?.field))) { return null }
  const path = pathFix(_p)
  return `/${path[0]}/${path[1] === _lang ? '' : _lang + '/'}${path?.slice(1)?.join('/') || ''}`
}

function pathFix(_path, nodeName = PAGE_NODE_NAME) {
  _path = _path.startsWith('http') ? _path.replace(/^[a-z]{4,5}:\/{2}[a-z.]{1,}(:[0-9]{1,4})?(.*)/, '$2') : _path
  const forbidden = [process?.env?.VUE_APP_SITE_BASENAME?.replace('/', ''), 'undefined']
  const fixedPath = `${nodeName}${pathWithoutBaseName(_path, nodeName)}`
  return fixedPath.split('/').filter(x => x && x.length && !forbidden.includes(x))
}

function getPageUrl(_itm, _lang) {
  const linkField = linkUtils.getLinkField(_itm?.pageLink, _lang)
  if (linkField?.type === 'custom' || linkField?.type === 'localAnchor') { return linkField.url } // return links that must not be remapped as page paths
  if (_itm?.external) { return _itm.url } // return always raw custom links

  const p = linkField?.url || _itm.url || _itm.pageUrl || _itm
  return mapUrlToSliced(p, _lang)
}

function injectLanguageToPath(_path, _lang) {
  if (!_path || !_lang) { return _path }
  return pathWithoutBaseName(mapUrlToSliced(_path, _lang))
}

function isCurrentActivePage(pageUrl, currentPath) {
  const path = currentPath?.endsWith('/') ? currentPath.slice(0, -1) : currentPath
  const url = pageUrl?.endsWith('/') ? pageUrl.slice(0, -1) : pageUrl
  return url?.endsWith(path)
}

function extractInheritancePaths(_path = '/') {
  // remove root to check how deep is navigation - if there is anything to inherit
  // ATTENTION: we don't inherit /bobcat page's components. We start from region
  const pathArray = _path.replace(process.env.VUE_APP_SITE_BASENAME, '').split('/').filter(x => x.length)
  if (pathArray.length) { pathArray.pop() } // remove last (current) element as we need only inheritance paths
  return pathArray
}

export default {
  pathFix,
  getPageUrl,
  isCurrentActivePage,
  pathWithoutBaseName,
  extractInheritancePaths,
  injectLanguageToPath
}
