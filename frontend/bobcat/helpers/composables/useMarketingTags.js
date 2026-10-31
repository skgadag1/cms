import {useContext} from '@nuxtjs/composition-api'
import {removeCurrentLanguage} from '~/helpers/functional/AppHelpers'

export default function useMarketingTags() {
  const {$services, $utils, $regions, app} = useContext()

  const getMarketingTagsObject = async (_path = '/', _routeNode = process.env.VUE_APP_SITE_BASENAME) => {
    try {
      const {data} = await $services.bobcatapi.fetchTagsToInject(_routeNode)
      const allTags = data?.results
      _path = removeCurrentLanguage(_path, $regions.current.language.name)

      return allTags?.filter((script) => {
        const pageLinks = script.pageLink
        if (pageLinks && typeof pageLinks === 'object') {
          const linksArray = Array.isArray(pageLinks) ? pageLinks : Object.values(pageLinks)
          return linksArray.some((link) => {
            const _condPath = $utils.pathWithoutBaseName(link?.path || '')
            // check if marketing tag meets condition to be injected
            return _condPath === _path || (link?.insertInSubPages === 'true' && _path?.startsWith(_condPath))
          })
        }
        return false
      })
    } catch (e) {
      // eslint-disable-next-line no-console
      console.warn(`[MarketingTags] Unable to retreive marketing tags: ${e}`)
    }
    return []
  }

  const injectHeadScriptObject = (scriptTag) => {
    app.head().__dangerouslyDisableSanitizersByTagID = app.head().__dangerouslyDisableSanitizersByTagID || {}
    app.head().__dangerouslyDisableSanitizersByTagID[scriptTag['@id']] = ['innerHTML']

    // removal of </script> at the end of scriptTag to prevent duplication of closure
    scriptTag.content = scriptTag.content.trim()
    if (scriptTag.content.endsWith('</script>')) {
      scriptTag.content = scriptTag.content.substring(0, scriptTag.content.length - '</script>'.length)
    }
    // REMEMBER! we only can inject objects to head via vue-meta script [https://vue-meta.nuxtjs.org/api/#script]
    // this is hack to inject "empty" script and then one from marketing tags
    app.head().script.unshift({
      hid: scriptTag['@id'],
      innerHTML: `</script>${scriptTag.content}`
    })
  }

  return {
    getMarketingTagsObject,
    injectHeadScriptObject
  }
}
