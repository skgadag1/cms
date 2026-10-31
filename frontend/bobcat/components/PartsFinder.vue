<template>
  <aside data-test-id="parts-finder" class="parts-finder" :class="variant !== 'white' ? 'bg-gray-lighter' : 'bg-white'">
    <LazyHydrate :on-interaction="['click', 'touchstart']">
      <div class="container">
        <div class="parts-finder__wrapper d-flex flex-column"
             :style="backgroundUrl ? `background-image: url(${backgroundUrl})` : ''"
        >
          <div class="row my-lg-2 py-4 py-md-5 py-lg-6">
            <div class="col-12">
              <component :is="htag" v-if="title" :class="htag">{{ title }}</component>
              <component :is="htagSubtitle" v-if="!useForm && subtitle">{{ subtitle }}</component>
            </div>

            <form v-if="useForm"
                  class="col-12"
                  :action="partsUrl.url"
                  :target="partsUrl.target === '_blank_follow' ? '_blank' : partsUrl.target"
                  data-test-id="parts-finder-form"
            >
              <div class="row">
                <div class="col-12 col-md col-lg-6 mb-3 mb-md-0">
                  <b-input-group class="form-control-wrapper border border-color-gray-600 rounded-2 transition-base">
                    <template #prepend>
                      <b-input-group-text class="bg-white border-0">
                        <b-icon icon="search" />
                      </b-input-group-text>
                    </template>
                    <b-form-input v-model="searchText"
                                  name="q"
                                  :placeholder="partsUrl.placeholder"
                                  :disabled="inEditorEdit"
                                  class="border-0 pl-0 border-color-gray-600"
                                  @keydown.enter="ga4click"
                    />
                  </b-input-group>
                </div>
                <div class="col-12 col-md-auto">
                  <b-button variant="primary"
                            type="submit"
                            class="btn-mobile d-inline-flex align-items-center justify-content-center"
                            data-test-id="parts-finder-form__button"
                            :disabled="inEditorEdit"
                            @click="ga4click"
                  >
                    {{ partsUrl.label }}
                    <b-icon icon="external" class="p-1 link-icon" />
                  </b-button>
                </div>
              </div>
            </form>

            <div v-else class="col-12" data-test-id="parts-finder-link">
              <a :href="inEditorEdit ? null : partsUrl.url"
                 :target="partsUrl.target === '_blank_follow' ? '_blank' : partsUrl.target"
                 :rel="partsUrl.target === '_blank' ? 'noopener nofollow' : null"
                 :disabled="inEditorEdit"
                 class="text-primary font-weight-bold d-inline-flex align-items-center justify-content-center"
              >
                {{ partsUrl.label }}
                <b-icon icon="external" class="ml-2 small" />
              </a>
            </div>
          </div>
        </div>
      </div>
    </LazyHydrate>
  </aside>
</template>

<script>
import {ref, useContext} from '@nuxtjs/composition-api'
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
      default: 'h3'
    },
    subtitle: {
      type: String
    },
    htagSubtitle: {
      type: String,
      default: 'h6'
    },
    bgImage: {
      type: String
    },
    variant: {
      type: String,
      default: 'gray'
    },
    partsUrl: {
      type: Object,
      required: true
    },
    showForm: {
      type: String,
      default: 'true'
    }
  },
  setup(props) {
    const {bgImage, showForm} = props
    const searchText = ref('')
    const {$utils, $ga4} = useContext()
    const backgroundUrl = $utils.getCloudinaryAsset(bgImage, true, {crop: 'fit', width: 1440, height: 200})
    const useForm = showForm === 'true'
    useMounted(props.nodeId)

    const ga4click = (event) => {
      $ga4?.events?.view_search_results(event, searchText.value, undefined, 'parts', 'parts-finder')
      $ga4?.events?.search(event, searchText.value, 'parts', 'parts-finder')
    }

    return {
      useForm,
      backgroundUrl,
      inEditorEdit,
      ga4click,
      searchText
    }
  }
}
</script>

<style lang="scss" scoped>
@import '~bootstrap/scss/functions';
@import '~/assets/sass/variables';

.parts-finder {
  &__wrapper {
    background-size: cover;
    background-repeat: no-repeat;
    background-position: center right;

    .bg-white & {
      background-size: contain;
    }

    .input-group input.form-control {
      height: calc(#{$line-height-base}rem + #{$input-btn-padding-x} + 5px);
    }

    .link-icon {
      font-size: .75rem
    }
  }
}
</style>
