<template>
  <section class="bg-gray-lighter" data-test-id="bq-selector">
    <LazyHydrate when-visible>
      <div class="container py-4">
        <div v-if="title" class="row" data-test-id="bq-selector__title">
          <div class="col-12">
            <component :is="htag" :class="htag">{{ title }}</component>
          </div>
        </div>

        <b-overlay :show="$fetchState.pending">
          <div class="row">
            <div v-if="allCategories.length" class="col-12" data-test-id="bq-selector__categories">
              <BuildAndQuoteCategoryItem v-for="c in allCategories"
                                         :key="`bq-category__${c['@id']}`"
                                         :category="c"
                                         :url="bqUrl"
              />
            </div>
            <WarningAlert v-else-if="inEditorEdit" class="col-12" data-test-id="bq-selector-error__nocategories">
              Categories not defined.
            </WarningAlert>
          </div>
        </b-overlay>
      </div>
    </LazyHydrate>
  </section>
</template>

<script>
import {ref, useFetch, useContext} from '@nuxtjs/composition-api'
import {inEditorEdit} from '~/helpers/vue-editor'
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
    },
    switchable: {
      type: Object,
      required: false
    },
    categories: {
      type: Object,
      required: true
    }
  },
  setup(props) {
    const {categories, switchable} = props
    const {$services} = useContext()
    const allCategories = ref([])
    const bqUrl = switchable?.field === 'automatic' ? switchable.automaticUrl : null
    useMounted(props.nodeId)

    useFetch(async () => {
      const catIds = categories['@nodes']?.map(name => categories[name].productCategory)
      allCategories.value = await $services.bobcatapi.getCategories(catIds)
    })

    return {
      allCategories,
      inEditorEdit,
      bqUrl
    }
  }
}
</script>
