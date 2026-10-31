const VIEWED_OBJECT_KEY = 'viewed'
export const PREVIOUSLY_VISITED_COOKIE = 'hasPreviouslyVisited'
const EMPTY_PAGES = {pages: {}}

export default function useViewedPages() {
  const MAX_HISTORY_NUMBER = 4
  let _viewed = localStorage.getItem(VIEWED_OBJECT_KEY) ? JSON.parse(localStorage.getItem(VIEWED_OBJECT_KEY)) : EMPTY_PAGES

  const _saveViewed = () => {
    localStorage.setItem(VIEWED_OBJECT_KEY, JSON.stringify(_viewed))
  }

  if (Array.isArray(_viewed.pages)) {
    _viewed = EMPTY_PAGES
    _saveViewed()
  }

  const getPagesForRegion = (_region = process.env.NUXT_DEFAULT_REGION) => {
    return _viewed.pages?.[_region] || []
  }

  const addPage = (p, _region = process.env.NUXT_DEFAULT_REGION, $cookies) => {
    if (p) {
      const pageImage = p.model?.images?.[p.model?.images?.['@nodes']?.find(x => p.model?.images?.[x]?.asset?.field === 'img')]?.asset?.img || p?.primaryPageImage?.src
      const _page = {
        '@id': p['@id'],
        title: p.model?.title || p.title,
        pageUrl: p.pageUrl,
        ...(pageImage?.src && {
          image: {
            alt: pageImage.imageAlt,
            title: pageImage.imageTitle,
            src: pageImage.src
          }
        })
      }
      _viewed.pages[_region] = [_page, ...(_viewed.pages?.[_region]?.filter(page => page['@id'] !== _page['@id']).slice(0, MAX_HISTORY_NUMBER - 1) || [])]
      _saveViewed()
      $cookies.set(`${PREVIOUSLY_VISITED_COOKIE}`, `${_region}`, {path: '/', maxAge: 60 * 60 * 24 * 14})
    }
  }

  return {
    addPage,
    getPagesForRegion,
    PREVIOUSLY_VISITED_COOKIE
  }
}
