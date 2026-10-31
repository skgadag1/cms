<template>
  <section data-test-id="packages-and-options" class="container mb-5">
    <LazyHydrate never>
      <div class="row justify-content-center">
        <div v-if="model" class="col-md-8 col-12">
          <div class="w-100">
            <div v-if="title" class="border-bottom mb-5 mt-3 w-100">
              <component :is="htag" :class="htag" data-test-id="packages-and-options-title">{{ title }}</component>
            </div>
          </div>

          <div v-if="model.pricedOptions && model.pricedOptions.length" data-test-id="packages-and-options-list">
            <div v-for="item in model.pricedOptions" :key="item.short" class="dl-inv-package p-3 mb-3 border-left border-4">
              <h6>{{ item.shortDesc }}</h6>
              <div v-html="item.longDesc" />
            </div>
          </div>
          <WarningAlert v-else type="info" data-test-id="packages-and-options-alert">{{ noPackagesMessage }}</WarningAlert>
        </div>
      </div>
    </LazyHydrate>
  </section>
</template>

<script>
import {computed, inject, ref, useContext, useFetch} from '@nuxtjs/composition-api'
import useMounted from '~/helpers/composables/useMounted'

export default {
  props: {
    nodeId: {
      type: String
    },
    title: {
      type: String
    },
    htag: {
      type: String,
      default: 'h2'
    }
  },
  setup(props) {
    const model = inject('model')
    const {$services, $regions} = useContext()
    const labels = ref({})
    useMounted(props.nodeId)

    useFetch(async () => {
      if ($regions?.current?.name === 'na') {
        labels.value = await $services.bobcatapi.getConcepts(['/common/labels/dealerinventory', '/%region%/common/labels/dealerinventory'])
      }
    })

    const noPackagesMessage = computed(() => {
      const template = labels.value.noPackagesOrOptions || 'This serial #{serial} has no added packages or options'
      return template.replace('{serial}', model.serial || model.id)
    })

    return {
      model,
      labels,
      noPackagesMessage
    }
  }
}
</script>
