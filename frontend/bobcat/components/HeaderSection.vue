<template>
  <LazyHydrate never>
    <header class="container pt-4 pb-2" data-test-id="article-header-section">
      <div class="row justify-content-center">
        <div class="col-12 justify-content-center flex-column" :class="{'col-sm-8 col-lg-7' : !fullWidth}">
          <component :is="htag" class="mb-4" :class="htag" data-test-id="header-section-title">{{ title }}</component>
          <p v-if="formatedDate" class="small text-gray-darker mb-4" data-test-id="header-section-post-date" :data-iso-date="formatedISODate">{{ formatedDate }}</p>
          <div v-if="description" data-test-id="header-section-description" v-html="description" />
        </div>
      </div>
    </header>
  </LazyHydrate>
</template>

<script>
import {ref, useContext} from '@nuxtjs/composition-api'
import useMounted from '~/helpers/composables/useMounted'

export default {
  props: {
    nodeId: {
      type: String
    },
    title: {
      type: String
    },
    postDate: {
      type: String
    },
    beforePostDate: {
      type: String
    },
    description: {
      type: String
    },
    htag: {
      type: String,
      default: 'h1'
    },
    showFullWidth: {
      type: String,
      default: 'false'
    }
  },
  setup(props) {
    const {postDate, beforePostDate, showFullWidth} = props
    const {$regions} = useContext()
    const formatedDate = ref('')
    const formatedISODate = ref('')
    const fullWidth = showFullWidth === 'true'
    useMounted(props.nodeId)

    if (postDate || beforePostDate) {
      if (postDate) {
        formatedISODate.value = new Date(postDate).toISOString().split('T')[0]
        const isoLangParam = $regions?.current?.locale || `${$regions?.current?.language.name}-us`
        formatedDate.value = new Date(postDate)?.toLocaleDateString(isoLangParam, {year: 'numeric', month: 'long', day: 'numeric'}) || formatedISODate.value
      }
      if (beforePostDate) { formatedDate.value = `${beforePostDate} ${formatedDate.value}` }
    }

    return {
      fullWidth,
      formatedDate,
      formatedISODate
    }
  }
}
</script>
