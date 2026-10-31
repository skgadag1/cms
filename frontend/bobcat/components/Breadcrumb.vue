<template>
  <section v-if="!excludeBreadcrumb" ref="container" class="container py-3" data-test-id="breadcrumb">
    <div class="row">
      <div class="col-12">
        <div v-if="!mobile && breadcrumbs && calculatedBreadcrumbs && calculatedBreadcrumbs.length"
             class="d-none d-sm-flex"
             data-test-id="desktop-breadcrumb-wrapper"
        >
          <BBreadcrumb>
            <BBreadcrumbItem v-for="(breadcrumb, i) in calculatedBreadcrumbs"
                             :key="`breadcrumb-item-${breadcrumb.href}__${i}`"
                             :active="breadcrumb.active"
                             :href="breadcrumb.href"
                             :data-test-id="`breadcrumb-item__${i}`"
                             class="font-weight-bold small"
            >
              {{ breadcrumb.text }}
            </BBreadcrumbItem>
          </BBreadcrumb>
        </div>
        <div v-if="mobile && currentRoute && currentRoute.text" class="d-flex d-sm-none" data-test-id="mobile-breadcrumb-wrapper">
          <a class="mobile-breadcrumb d-flex align-items-center font-weight-bold small" :href="currentRoute.href"><u>{{ currentRoute.text }}</u></a>
        </div>
      </div>
    </div>
  </section>
</template>

<script>
import {computed, inject, onBeforeUnmount, onMounted, ref, useContext, useFetch, useRouter} from '@nuxtjs/composition-api'
import {BBreadcrumb, BBreadcrumbItem} from 'bootstrap-vue'
import useSchema from '@/helpers/composables/useSchema'
import {
  DEALER_INVENTORY_MODEL_TEMPLATE_ROUTE,
  DEALER_LOCATOR_LANDING_PAGE_TEMPLATE_ROUTE,
  DEALER_LOCATOR_PAGE_TEMPLATE_ROUTE
} from '~/constants/staticRoutes'
import useMounted from '~/helpers/composables/useMounted'
import useBreakpoints from '~/helpers/composables/useBreakpoints'

export default {
  components: {
    BBreadcrumb,
    BBreadcrumbItem
  },
  props: {
    nodeId: {
      type: String
    }
  },
  setup(props) {
    const {$regions, $utils, $breakpoints, $services, $config} = useContext()
    const currentLanguage = $regions?.current?.language?.name
    const currentRegion = $regions?.current?.name
    const router = useRouter()
    const container = ref(null)
    const breadcrumbs = inject('breadcrumbs')
    const excludeBreadcrumb = inject('excludeBreadcrumb')
    const model = inject('model')
    const calculatedBreadcrumbs = ref([])
    const currentRoute = ref(null)
    const oneCharWidth = 8
    const mainRoute = `/${currentRegion}/${currentLanguage}`
    useMounted(props.nodeId)
    const {mobile} = useBreakpoints()

    const {fetch} = useFetch(async (bucket) => {
      calculatedBreadcrumbs.value = await Promise.all(
        breadcrumbs?.map(async (page, index) => {
          const href = $utils.pathWithoutBaseName(page.url)
          const isDINVPage = href?.includes(DEALER_INVENTORY_MODEL_TEMPLATE_ROUTE)
          const isDealerLocatorPage = href?.includes(DEALER_LOCATOR_LANDING_PAGE_TEMPLATE_ROUTE)
          let text = page?.title || ''

          if (isDINVPage) {
            text = model.serialNumber || model?.name
          } else if (isDealerLocatorPage) {
            const mdmId = router.currentRoute.path.split(DEALER_LOCATOR_PAGE_TEMPLATE_ROUTE)[1]?.replace('/', '')
            const dealerDetails = await $services.dealerlocator.getDealerDetails(mdmId)
            text = dealerDetails?.mailingLabel
          }

          const properties = {
            href,
            active: isDINVPage || isDealerLocatorPage || $utils.isCurrentActivePage(href, router.currentRoute.path)
          }
          const minTextLength = 3

          let cutText = text

          if (index === breadcrumbs?.length - 1 && ($utils.isSmallMobile() || $breakpoints?.mobile)) {
            cutText = `${text.slice(0, 19)}${text.length > 19 ? '...' : ''}`
          } else if (bucket + 3 > text?.length) {
            cutText = text.slice(0, minTextLength) + '...'
          } else if (bucket > 0) {
            cutText = text.slice(0, -Math.round(bucket)) + '...'
          }

          const breadcrumb = {
            text: cutText,
            fullText: text,
            ...properties
          }

          if (bucket > 0) {
            bucket -= cutText?.length
          }

          return breadcrumb
        }) || []
      )
    })

    const checkLineLimit = async () => {
      let totalCharsInBreadcrumbs = 0
      breadcrumbs?.forEach((page, index) => {
        if ($utils.isSmallMobile() || $breakpoints?.mobile) {
          if (index === breadcrumbs.length - 1) {
            totalCharsInBreadcrumbs += page?.title?.length
          }
        } else {
          totalCharsInBreadcrumbs += page?.title?.length
        }
      })

      const bucket = totalCharsInBreadcrumbs - (container.value?.clientWidth - 38) / oneCharWidth

      await fetch(bucket)

      currentRoute.value = {
        text: calculatedBreadcrumbs.value[calculatedBreadcrumbs.value?.length - 2]?.text,
        href: calculatedBreadcrumbs.value[calculatedBreadcrumbs.value?.length - 2]?.href || mainRoute
      }
    }

    checkLineLimit()

    onMounted(() => {
      checkLineLimit()
      window?.addEventListener('resize', checkLineLimit, {passive: true})
    })

    onBeforeUnmount(() => {
      window.removeEventListener('resize', checkLineLimit)
    })

    const breadcrumbsSchema = computed(() => {
      const {breadcrumbs} = useSchema({breadcrumbs: calculatedBreadcrumbs.value}, $utils, $regions, $config)
      return breadcrumbs()
    })

    return {
      breadcrumbs,
      breadcrumbsSchema,
      calculatedBreadcrumbs,
      currentRoute,
      container,
      excludeBreadcrumb,
      mobile
    }
  },
  jsonld() {
    return this.breadcrumbsSchema
  }
}
</script>

<style lang="scss" scoped>
@import '~bootstrap/scss/functions';
@import '~/assets/sass/variables';
@import '~bootstrap/scss/mixins';
@import '~/assets/sass/colors';
@import '~bootstrap/scss/breadcrumb';

.mobile-breadcrumb {
  &:before {
    display: block;
    margin-right: .5rem;
    width: 0;
    height: 0;
    border-top: 6px solid transparent;
    border-bottom: 6px solid transparent;
    border-right: 6px solid $primary;
    content: '';
  }
}
</style>
