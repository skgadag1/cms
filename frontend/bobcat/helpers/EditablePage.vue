<template>
  <section ref="element">
    <EditableArea v-for="(area, i) in areas"
                  :key="'editable-area__' + i"
                  :content="area"
                  :parent-template-id="pageTemplateId"
                  :has-anchor="hasAnchor"
    />
  </section>
</template>

<script>
import {computed, onMounted, provide, ref, useContext, useRouter, watch} from '@nuxtjs/composition-api'
import {inEditorEdit, inMgnlIframe, insertComments, inMgnlPreview} from '~/helpers/vue-editor'
import config from '~/magnolia.config'
import useSchema from '~/helpers/composables/useSchema'
import useViewedPages from '~/helpers/composables/useViewedPages'
import {MODEL_LIFECYCLE} from '~/constants'
import {BUILD_AND_PRICE_PAGE_TEMPLATE_NAME} from '~/constants/pageTemplates'

export default {
  props: ['content', 'templateAnnotations', 'campaigns'],
  setup(props) {
    const {$utils, $regions, $cookies, route, $ga4, $config} = useContext()
    const router = useRouter()
    const {content, templateAnnotations} = props
    const pageTemplateId = content['mgnl:template']
    const element = ref(null)
    const hasAnchor = ref(false)
    const selectedVariant = ref(null)
    const isModelPage = ref(pageTemplateId?.endsWith('-details'))

    const {product: productSchema} = useSchema(content, $utils, $regions, $config)
    provide('config', config)
    provide('model', content.model)
    provide('selectedVariant', selectedVariant)
    provide('templateAnnotations', templateAnnotations)
    provide('pageTemplateId', pageTemplateId)
    provide('breadcrumbs', content.breadcrumbs)
    provide('excludeBreadcrumb', content?.excludeBreadcrumb === 'true')
    const cpgn = ref(props.campaigns)
    watch(() => props.campaigns, (n) => {
      cpgn.value = n
    })
    provide('campaigns', cpgn)

    const hidesFooter = pageTemplateId === BUILD_AND_PRICE_PAGE_TEMPLATE_NAME

    const areas = computed({
      get: () => {
        return content['@areaNodes']
          ? content['@areaNodes']
            .filter(nodeName => !(hidesFooter && nodeName === 'footer'))
            .map(nodeName => content.areaNodes[nodeName] || content[nodeName])
            .filter(n => n?.['@nodeType'] === 'mgnl:area')
          : []
      },
      set: () => {
      }
    })

    if (process.client) {
      onMounted(async () => {
        if (!inEditorEdit) {
          const hash = route?.value?.hash?.replace('#', '')

          if (hash?.length) {
            hasAnchor.value = true
            document.addEventListener('componentAnchor', (event) => {
              if (event.target.id === hash) {
                setTimeout(() => $utils.scrollToElement(hash), 500)
              }
            }, false)
          }

          document.querySelector('body').addEventListener('click', (e) => {
            const link = e.target.closest('a')
            const linkUrl = link?.href ? new URL(link?.href?.split('#')?.[0]) : null
            const anchorHash = link?.href?.split('#')?.[1]

            // GA4 - events for anchors
            if (link && !$utils.isExternalLink(link)) {
              $ga4?.events?.browse(event, link)
            }

            // anchors - scroll to #id; preserve query params (e.g. ?facets=...#models) via Vue Router
            if (linkUrl &&
              link?.href?.includes('#') &&
              (window.location.host + window.location.pathname) === (linkUrl?.host + linkUrl?.pathname)) {
              e.preventDefault()
              const query = Object.fromEntries(linkUrl.searchParams)

              if (Object.keys(query).length > 0) {
                router.push({
                  path: linkUrl.pathname,
                  query,
                  hash: `#${anchorHash}`
                }).catch(() => {})
              }

              if (document.getElementById(anchorHash)) {
                $utils.scrollToElement(anchorHash)
              } else {
                document.addEventListener('componentAnchor', (event) => {
                  if (event.target.id === anchorHash) {
                    setTimeout(() => $utils.scrollToElement(anchorHash), 500)
                  }
                }, false)
                hasAnchor.value = true // trigger hydration
              }
            }
          })
        }

        if (inEditorEdit) {
          const {PageEditorBridge, EditorContextHelper} = await import('@magnolia/template-annotations')

          if (inMgnlIframe && templateAnnotations && !inMgnlPreview) {
            PageEditorBridge.init({})
            const $el = element.value?.$el || element.value
            insertComments($el, (templateAnnotations || {})[content['@path']], '/cms:page')
            EditorContextHelper.onFrameReady()
          }
        } else if (isModelPage.value && (content.model?.lifecycle?.toLowerCase() === MODEL_LIFECYCLE.CURRENT || content.model?.['@nodes']?.some(m => content.model?.[m]?.lifecycle?.toLowerCase() === MODEL_LIFECYCLE.CURRENT))) {
          const {
            addPage
          } = useViewedPages()

          addPage(content, $regions?.current?.name, $cookies)
        }
      })
    }

    return {
      element,
      areas,
      pageTemplateId,
      productSchema,
      hasAnchor
    }
  },
  jsonld() {
    return this.productSchema()
  }
}
</script>
