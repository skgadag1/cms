<template>
  <div>
    <component :is="inEditorEdit ? 'div' : 'LazyHydrate'" v-for="(mt, i) in marketingTagsBodyTop" :key="`marketing-tag-top__${i}`" never>
      <div v-if="!$isLighthouse && mt.content" class="d-none" data-test-id="script-wrapper" v-html="mt.content" />
    </component>

    <EditablePage v-if="content && templateAnnotations"
                  :content="content"
                  :campaigns="campaigns"
                  :template-annotations="templateAnnotations"
    />

    <component :is="inEditorEdit ? 'div' : 'LazyHydrate'" v-for="(mt, i) in marketingTagsBodyBottom" :key="`marketing-tag-bottom__${i}`" never>
      <div v-if="!$isLighthouse && mt.content" class="d-none" data-test-id="script-wrapper" v-html="mt.content" />
    </component>
  </div>
</template>

<script>
import {onServerPrefetch, ref, useContext, useFetch, useRouter} from '@nuxtjs/composition-api'
import {inEditorEdit} from '~/helpers/vue-editor'
import useSchema from '~/helpers/composables/useSchema'
import usePageLoader from '~/helpers/composables/usePageLoader'
import useMarketingTags from '~/helpers/composables/useMarketingTags'
import MARKETING_TAGS from '~/constants/marketingTags'
import {
  DEALER_INVENTORY_MODEL_TEMPLATE_ROUTE,
  DEALER_INVENTORY_ROUTE,
  DEALER_LOCATOR_LANDING_PAGE_TEMPLATE_ROUTE,
  DEALER_INVENTORY_WINDOW_STICKER_TEMPLATE_ROUTE,
  DEALER_LOCATOR_PAGE_TEMPLATE_ROUTE,
  EQUIPMENT_ROUTE,
  EQUIPMENT_WINDOW_STICKER_TEMPLATE_ROUTE
} from '~/constants/staticRoutes'

export default {
  setup() {
    const {res, $utils, $regions, $isBot, $isLighthouse, route, $services, redirect, $config} = useContext()
    const router = useRouter()
    const schemas = ref([])
    const marketingTags = ref([])
    const marketingTagsHead = ref([])
    const marketingTagsBodyTop = ref([])
    const marketingTagsBodyBottom = ref([])

    const {
      templateAnnotations,
      content,
      pageRobots,
      excludeFromSearch,
      primaryImage,
      facetTitle,
      title,
      navigationLabel,
      description,
      keywords,
      loadPage,
      generateFacetsFromDxpTags,
      generateTemplateName,
      fetchCampaings,
      generateSortRating
    } = usePageLoader({$utils, $services, $regions, route, redirect, $config})

    const campaigns = ref([])
    const {getMarketingTagsObject, injectHeadScriptObject} = useMarketingTags()

    const {fetch} = useFetch(async () => {
      try {
        // [NGB-4095] ToDo: revert below to have working external edit mode server
        const inEditor = inEditorEdit || ($config.IS_AUTHOR && !$config.USE_NATIVE_MAGNOLIA) || router.currentRoute.fullPath.includes('mgnlPreview') || router.currentRoute.fullPath.includes('editmode=on')
        const currentRoute = (inEditor && !['/regions', '/error'].includes($utils.pathWithoutBaseName(router.currentRoute.path))) ? {...router.currentRoute, path: $utils.injectLanguageToPath(router.currentRoute.path, $regions.current.language.name)} : router.currentRoute
        // const currentRoute = router.currentRoute
        if (currentRoute.path.includes(`${DEALER_INVENTORY_ROUTE}`)) {
          const params = router.currentRoute.path.split(DEALER_INVENTORY_ROUTE)[1]
          const segments = params?.split('/')?.filter(segment => segment?.length > 0)

          if (segments.length === 3) {
            // eslint-disable-next-line no-unused-vars
            const [category, dealerID, modelID] = segments

            const modifiedPath = currentRoute.path.replace(params, '') + (router.currentRoute.query?.print === 'true' ? DEALER_INVENTORY_WINDOW_STICKER_TEMPLATE_ROUTE : DEALER_INVENTORY_MODEL_TEMPLATE_ROUTE)
            const modifiedCurrentRoute = {...currentRoute, path: modifiedPath, fullPath: modifiedPath}
            await loadPage(modifiedCurrentRoute, {dealerID, modelID})
          } else if ((params?.match(/\//ig))?.length === 2) {
            // solution for old dealer inventory
            const splitedParams = params.split('/')
            const dealerID = splitedParams[1]
            const modelID = splitedParams[2]
            const modifiedPath = currentRoute.path.replace(params, '') + (router.currentRoute.query?.print === 'true' ? DEALER_INVENTORY_WINDOW_STICKER_TEMPLATE_ROUTE : DEALER_INVENTORY_MODEL_TEMPLATE_ROUTE)
            const modifiedCurrentRoute = {...currentRoute, path: modifiedPath, fullPath: modifiedPath}
            await loadPage(modifiedCurrentRoute, {dealerID, modelID})
          } else {
            await loadPage(currentRoute)
          }
        } else if (currentRoute.path.includes(`${EQUIPMENT_ROUTE}`)) {
          const params = router.currentRoute.path.split(EQUIPMENT_ROUTE)[1]
          if (router.currentRoute.query?.print === 'true') {
            const modelID = router.currentRoute.query?.modelID
            const modifiedPath = currentRoute.path.replace(params, '') + EQUIPMENT_WINDOW_STICKER_TEMPLATE_ROUTE
            const modifiedCurrentRoute = {...currentRoute, path: modifiedPath, fullPath: modifiedPath}
            await loadPage(modifiedCurrentRoute, {modelID, equipmentPrint: true})
          } else {
            await loadPage(currentRoute)
          }
        } else if (currentRoute.path.includes(`${DEALER_LOCATOR_PAGE_TEMPLATE_ROUTE}/`)) {
          const mdmId = router.currentRoute.path.split(DEALER_LOCATOR_PAGE_TEMPLATE_ROUTE)[1]?.replace('/', '')
          if (mdmId) {
            const modifiedPath = currentRoute.path.replace(mdmId, '') + DEALER_LOCATOR_LANDING_PAGE_TEMPLATE_ROUTE.replace('/', '')
            const modifiedCurrentRoute = {...currentRoute, path: modifiedPath, fullPath: modifiedPath}
            await loadPage(modifiedCurrentRoute)
          } else {
            await loadPage(currentRoute)
          }
        } else {
          await loadPage(currentRoute)
        }
      } catch (e) {
        if (process.server) {
          res.statusCode = 404
          return
        } else {
          throw new Error(e)
        }
      }

      try {
        campaigns.value = await fetchCampaings()
      } catch (e) {
        // eslint-disable-next-line no-console
        console.error('[PageLoader] Unable to retreive campaigns')
      }

      if (Object.keys(content.value).length) {
        const {corporation, webSite} = useSchema(content.value, $utils, $regions, $config)
        const isNaHomepage = $utils.pathWithoutBaseName(router.currentRoute.path) === '/na/en'
        schemas.value = [isNaHomepage && corporation(), webSite()].filter(x => x)
        if (!($isLighthouse || $isBot)) {
          marketingTags.value = await getMarketingTagsObject($utils.pathWithoutBaseName(router.currentRoute.path))
          marketingTagsBodyTop.value = marketingTags.value.filter(mt => mt.placeToPut === MARKETING_TAGS.BODY_START)
          marketingTagsBodyBottom.value = marketingTags.value.filter(mt => mt.placeToPut === MARKETING_TAGS.BODY_END)
          marketingTagsHead.value = marketingTags.value.filter(mt => mt.placeToPut === MARKETING_TAGS.HEAD && mt.content.startsWith('<script'))

          if (process.server && marketingTagsHead.value.length) {
            marketingTagsHead.value.forEach(mt => injectHeadScriptObject(mt))
          }
        }
      }
    })

    onServerPrefetch(async () => {
      await fetch()
    })

    if (inEditorEdit && process.client) {
      // hack to delay reload editor frame by (300ms * component's number) to prevent getting old content after change
      window.addEventListener('unload', () => {
        const areas = (content.value['@areaNodes'] || content.value.areaNodes)?.map(nodeName => content.value.areaNodes[nodeName] || content.value[nodeName]).filter(n => n?.['@nodeType'] === 'mgnl:area')
        let delay = 0
        areas.forEach((area) => { delay += (300 * area.areaNodes?.length) || 0 })
        const currentTime = new Date().getTime()
        // eslint-disable-next-line no-empty
        while (new Date().getTime() < currentTime + delay) {}
      })
    }

    return {
      content,
      pageRobots,
      excludeFromSearch,
      campaigns,
      primaryImage,
      facetTitle,
      generateTemplateName,
      generateFacetsFromDxpTags,
      generateSortRating,
      templateAnnotations,
      title,
      navigationLabel,
      description,
      keywords,
      schemas,
      marketingTagsHead,
      marketingTagsBodyTop,
      marketingTagsBodyBottom,
      inEditorEdit
    }
  },
  head() {
    const fullUrl = `${this.$config.PUBLIC_HOST}${this?.$route.path}`
    const headObj = {
      htmlAttrs: {
        lang: this?.$regions ? this.$regions.current.language.name : process.env.NUXT_DEFAULT_LANGUAGE
      },
      title: this?.title,
      meta: [
        {property: 'og:title', content: this?.title},
        {property: 'og:facet-title', content: this?.facetTitle},
        {property: 'og:url', content: fullUrl},
        {property: 'og:type', content: this?.generateTemplateName()}
      ],
      bodyAttrs: {
        'data-index-facets': this?.generateFacetsFromDxpTags(),
        'data-page-type': this?.generateTemplateName(),
        'data-sort-rating': this?.generateSortRating()
      },
      link: [
        {rel: 'canonical', href: fullUrl, hid: 'canonical'}
      ]
    }

    if (this.description?.length) {
      headObj.meta.push({hid: 'description', name: 'description', content: this.description})
      headObj.meta.push({property: 'og:description', content: this.description})
    }

    if (this.excludeFromSearch === 'true') {
      headObj.meta.push({hid: 'excludeFromSearch', name: 'excludeFromSearch', content: this.excludeFromSearch})
    }

    if (this.keywords?.length) { headObj.meta.push({hid: 'keywords', name: 'keywords', content: this.keywords}) }
    if (this.pageRobots?.length) { headObj.meta.push({name: 'robots', content: `${this?.pageRobots}`, hid: 'robots'}) }

    if (this.primaryImage?.src) {
      headObj.meta.push(...[
        {property: 'og:image', content: this.primaryImage.src},
        {property: 'og:image:type', content: this.primaryImage.format},
        {property: 'og:image:height', content: this.primaryImage.height},
        {property: 'og:image:width', content: this.primaryImage.width},
        (this.primaryImage.alt && {property: 'og:image:alt', content: this.primaryImage.alt}),
        {property: 'cloudinary:id', content: this.primaryImage.cloudinaryAsset},
        (this.primaryImage.alt && {property: 'cloudinary:alt', content: this.primaryImage.alt}),
        (this.primaryImage.title && {property: 'cloudinary:title', content: this.primaryImage.title})
      ].filter(x => x))
    }

    return headObj
  },
  jsonld() {
    return this.schemas.filter(x => x)
  }
}
</script>

<style lang="scss">
.mgnlEditorBar.mgnlEditor {
  align-self: stretch;
}
</style>
