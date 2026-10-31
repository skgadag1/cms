<template>
  <b-form :id="formId"
          class="search-wrapper w-100 position-relative search-panel"
          :class="[visible ? 'd-flex' : 'd-none']"
          :action="searchUrl"
          method="get"
  >
    <div
      class="d-none d-flex z-index-1 form-control-wrapper align-items-center justify-content-between border border-color-gray-600 rounded-2 transition-base bg-white w-100 height-4"
    >
      <div class="input-icon d-flex z-index-1 align-items-center pr-0 pl-2 ml-1">
        <b-icon icon="search" />
      </div>
      <div class="col form-control-parent p-0">
        <input v-model="searchText"
               class="border-0 text-dark form-control"
               type="search"
               name="q"
               :placeholder="labels.searchPlaceholder || 'Search: bobcat, doosan, loader'"
               :aria-label="labels.search"
               @keydown.enter="ga4click"
        >
      </div>
      <div class="col-auto submit-search transition-base pr-2 pl-0">
        <button class="d-flex align-items-center text-dark border-0 bg-transparent p-0"
                type="submit"
                aria-label="Submit search"
                @click="ga4click"
        >
          <b-icon icon="arrow-right" />
        </button>
      </div>
    </div>
    <div class="overlay d-lg-none" :class="visible ? 'd-flex' : 'd-none'" />
  </b-form>
</template>

<script>
import {computed, onMounted, ref, useContext, useFetch} from '@nuxtjs/composition-api'

export default {
  props: {
    formId: {
      type: String,
      default: ''
    },
    visible: {
      type: Boolean,
      default: true
    },
    searchPageUrl: {
      type: String,
      default: ''
    }
  },
  setup(props) {
    const searchText = ref('')
    const {$utils, $services, $regions, $ga4} = useContext()
    const labels = ref({})

    useFetch(async () => {
      labels.value = await $services.bobcatapi.getConcepts()
    })

    const searchUrl = computed({
      get: () => $utils?.getPageUrl(props.searchPageUrl, $regions.current.language.name) || '',
      set: () => {
      }
    })

    const ga4click = (event) => {
      $ga4?.events?.view_search_results(event, searchText.value, undefined, 'site', 'main-header-search')
      $ga4?.events?.search(event, searchText.value, 'site', 'main-header-search')
    }

    if (process.client) {
      onMounted(() => {
        document.dispatchEvent(new Event('searchLoaded')) // required for cludo search
      })
    }

    return {
      searchText,
      searchUrl,
      labels,
      ga4click
    }
  }
}
</script>

<style lang="scss" scoped>
@import '~bootstrap/scss/functions';
@import '~/assets/sass/variables';
@import '~/assets/sass/utils';
@import '~bootstrap/scss/mixins';
@import '~/assets/sass/colors';

.search-wrapper {
  z-index: initial;

  @include media-breakpoint-down(md) {
    .form-control-wrapper {
      height: $height-5 !important;
    }
  }

  .input-icon {
    transform: translateY(0);
  }

  .input-icon:has(~ .form-control-parent:focus-within) {
    color: $gray-900;
  }

  .submit-search {
    opacity: 0;
  }

  .form-control-parent:focus-within ~ .submit-search {
    opacity: 1;
  }
}

@media (min-width: 992px) {
  .search-panel {
    margin-left: 1rem;
    padding-left: 3rem;
  }
}

</style>
