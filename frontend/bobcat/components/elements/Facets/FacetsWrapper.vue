<template>
  <div>
    <aside data-test-id="facets" class="d-none d-md-block">
      <slot name="facets" />
    </aside>
    <aside data-test-id="facets-mobile" class="facets-mobile d-md-none">
      <b-button v-b-toggle.sidebar-1
                block
                variant="outline-primary"
                :title="generateConceptHint('filters')"
                class="d-inline-flex align-content-center align-items-center justify-content-center"
      >
        <b-icon icon="filter" />
        <span class="px-1">{{ labels.filterResults || 'Filter Results' }}</span>
        <div v-if="filterCount" class="py-1 small">({{ filterCount }})</div>
      </b-button>
      <BSidebar id="sidebar-1"
                v-model="isSideBarVisible"
                title="Filters"
                header-class="font-weight-bold border-bottom bg-gray-lighter"
                shadow
                backdrop
                bg-variant="white"
                @hidden="$emit('hidden', $event)"
      >
        <div class="px-3 py-2 pb-5">
          <slot name="facets" />
        </div>
      </BSidebar>
    </aside>
  </div>
</template>

<script>
import {BSidebar} from 'bootstrap-vue'
import {ref} from '@nuxtjs/composition-api'
import useConcepts from '~/helpers/composables/useConcepts'

export default {
  components: {BSidebar},
  props: {
    filterCount: {
      type: Number,
      default: 0
    },
    labels: Object
  },
  setup() {
    const {generateConceptHint} = useConcepts()
    const isSideBarVisible = ref(false)

    const hideSideBar = () => {
      isSideBarVisible.value = false
    }

    return {
      generateConceptHint,
      isSideBarVisible,
      hideSideBar
    }
  }
}
</script>

<style lang="scss">
.facets-mobile {
  .b-sidebar, .b-sidebar-backdrop {
    top: initial !important;
    padding-bottom: initial !important;

    .b-sidebar-header {
      font-size: 1.125rem;

      strong {
        font-weight: normal;
      }
    }
  }
}
</style>
