<template>
  <a v-if="item && !item.hideFromNavigation"
     :href="itemUrl"
     class="col-12 mb-0 py-3 d-flex align-items-center text-decoration-none w-100"
     :class="[
       {'active': isActive && !highlight},
       highlight ? 'font-weight-bold text-primary' : 'font-weight-normal text-black'
     ]"
     role="menuitem"
     :aria-label="`${item?.link?.label || item.label} ${parentLabel}`"
  >
    <span>{{ item.link.label || item.label }}</span>
    <b-icon v-if="highlight" icon="chevron-right" class="ml-1 text-primary flex-shrink-0" />
    <span v-if="item.badge" class="d-flex align-items-center ml-auto flex-shrink-0">
      <NavigationIndicator :text="item.badge" />
    </span>
  </a>
</template>

<script setup>
import {useContext, useRouter} from '@nuxtjs/composition-api'
import NavigationIndicator from '@/components/elements/Navigation/NavigationIndicator.vue'

const {$utils, $regions} = useContext()
const $router = useRouter()

const props = defineProps({
  item: {
    type: Object,
    required: true
  },
  highlight: {
    type: Boolean,
    default: false
  },
  parentLabel: {
    type: [String, null]
  }
})

const getPageUrl = (_p) => {
  return $utils?.getPageUrl(_p, $regions.current.language.name) || ''
}
const itemUrl = getPageUrl(props.item?.link, $regions.current.language.name)
// we have to calculate isActive here, cause `item.isActive` may be true because of `item.items`
const isActive = $utils.isCurrentActivePage(itemUrl, $router?.currentRoute?.path)

</script>
