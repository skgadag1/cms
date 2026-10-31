<template>
  <section class="container py-4 attachment-info" data-test-id="attachment-info">
    <b-overlay :show="$fetchState.pending">
      <WarningAlert v-if="(!model || !model['@path']) && inEditorEdit"
                    data-test-id="attachment-info-model-alert"
      >
        No attachment defined for page. Please check page template configuration.
      </WarningAlert>

      <div v-else-if="model" class="row">
        <template v-if="model.topTasksField && topTasksToggle !== 'true'">
          <div class="col-12">
            <div class="border-bottom mb-5">
              <component :is="htag" :class="htag" data-test-id="product-top-task-field-title">{{ labels.topTasksTitle || 'Top Tasks' }}</component>
            </div>
          </div>
          <div data-test-id="top-task-html" class="col-12 col-md-10 offset-md-1 col-lg-6 offset-lg-3 top-task-html" v-html="model.topTasksField" />
        </template>
        <template v-if="model.howItWorksField && howItWorksToggle !== 'true'">
          <div class="col-12">
            <div class="border-bottom mb-5">
              <component :is="htag" :class="htag" data-test-id="how-it-works-field-title">{{ labels.howItWorksTitle || 'How it works' }}</component>
            </div>
          </div>
          <div data-test-id="how-it-works-html" class="col-12 col-md-10 offset-md-1 col-lg-6 offset-lg-3" v-html="model.howItWorksField" />
        </template>
        <template v-if="model.requiredComponentsField && requiredComponentsToggle !== 'true'">
          <div class="col-12">
            <div class="border-bottom mb-5">
              <component :is="htag" :class="htag" data-test-id="required-components-field-title">{{ labels.requiredComponentsTitle || 'Required components' }}</component>
            </div>
          </div>
          <div data-test-id="required-components-html" class="col-12 col-md-10 offset-md-1 col-lg-6 offset-lg-3" v-html="model.requiredComponentsField" />
        </template>
        <template v-if="model.optionsAccessoriesField && optionsAccessoriesToggle !== 'true'">
          <div class="col-12">
            <div class="border-bottom mb-5">
              <component :is="htag" :class="htag" data-test-id="options-accessories-field-title">{{ labels.optionsAccessoriesTitle || 'Options & Accessories' }}</component>
            </div>
          </div>
          <div data-test-id="options-accessories-html" class="col-12 col-md-10 offset-md-1 col-lg-6 offset-lg-3" v-html="model.optionsAccessoriesField" />
        </template>
        <div v-if="(!model.topTasksField || topTasksToggle === 'true') && (!model.howItWorksField || howItWorksToggle === 'true') && (!model.requiredComponentsField || requiredComponentsToggle === 'true') && (!model.optionsAccessoriesField || optionsAccessoriesToggle === 'true')" class="col-12">
          <WarningAlert v-if="inEditorEdit" data-test-id="attachment-info-empty-alert">
            Attachment has no additional info.
          </WarningAlert>
        </div>
      </div>
    </b-overlay>
  </section>
</template>

<script>
import {onServerPrefetch, ref, useContext, useFetch} from '@nuxtjs/composition-api'
import {inEditorEdit} from '~/helpers/vue-editor'
import useMounted from '~/helpers/composables/useMounted'

export default {
  inject: ['model'],
  props: {
    nodeId: {
      type: String
    },
    htag: {
      type: String,
      default: 'h2'
    },
    topTasksToggle: {
      type: String,
      default: 'false'
    },
    howItWorksToggle: {
      type: String,
      default: 'false'
    },
    requiredComponentsToggle: {
      type: String,
      default: 'false'
    },
    optionsAccessoriesToggle: {
      type: String,
      default: 'false'
    }
  },
  setup(props) {
    const {$services} = useContext()
    const labels = ref({})
    useMounted(props.nodeId)

    const {fetch, fetchState} = useFetch(async () => {
      if (!Object.keys(labels.value || {}).length) {
        labels.value = await $services.bobcatapi.getConcepts(['/common/labels/specifications', '/%region%/common/labels/specifications'])
      }
    })

    onServerPrefetch(async () => {
      await fetch()
    })

    return {
      fetchState,
      labels,
      inEditorEdit
    }
  }
}
</script>

<style lang="scss">
.attachment-info {
  ul {
    padding-left: 1.1rem;
  }
}
.top-task-html {
  iframe {
    max-width: 100%;
  }
}
</style>
