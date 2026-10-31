<template>
  <div>
    <InPageNavigationMobile :disabled="disabled"
                            :loading="loading"
                            :items="items"
    />
    <InPageNavigationDesktop :disabled="disabled"
                             :loading="loading"
                             :items="items"
    />
  </div>
</template>

<script>
import { useLabels } from '@/composables/useLabels'
import InPageNavigationDesktop from '@/components/common/Navigation/InPageNavigationDesktop'
import InPageNavigationMobile from '@/components/common/Navigation/InPageNavigationMobile'
import {extractSubnavigation} from '@/utils'
import {mapGetters} from 'vuex'
import {contentService} from '@/services'

export default {
  conceptName: ['InPageNavigation'],
  setup() { return useLabels() },
  components: {
    InPageNavigationDesktop,
    InPageNavigationMobile
  },
  props: {
    disabled: {
      type: Boolean,
      required: false,
      default: false
    }
  },
  data() {
    return {
      loading: true,
      error: null,
      items: []
    }
  },
  computed: {
    ...mapGetters([
      'getCurrentAbbreviation'
    ])
  },
  created() {
    this.getNavigationStructure()
  },
  methods: {
    getNavigationStructure() {
      this.loading = true
      contentService.fetchSitemap().then(r => {
        this.items = extractSubnavigation(r.data.items)
        this.loading = false
      }).catch(e => {
        this.loading = false
        this.error = e
      })
    }
  }
}
</script>
