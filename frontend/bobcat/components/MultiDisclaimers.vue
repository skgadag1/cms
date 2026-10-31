<template>
  <section data-test-id="disclaimers">
    <LazyHydrate never>
      <div class="container">
        <div v-for="(disclaimer, i) in disclaimersContent" :key="i" class="py-5 section-text">
          <div :class="{'h4 m-0' : disclaimer?.value === 'PlainText'}" v-html="disclaimer" />
        </div>
      </div>
    </LazyHydrate>
  </section>
</template>

<script>
import {ref} from '@nuxtjs/composition-api'
import useMounted from '~/helpers/composables/useMounted'

export default {
  props: {
    nodeId: {
      type: String
    },
    disclaimerConcept: {
      type: Array
    }
  },
  setup(props) {
    useMounted(props.nodeId)
    const disclaimersContent = ref([])

    const processNode = (node) => {
      if (node?.['@nodeType'] === 'mgnl:content') {
        disclaimersContent.value.push(node?.['value' + node?.value])
      } else if (node?.['@nodeType'] === 'mgnl:folder' && node?.['@nodes']) {
        node['@nodes'].forEach((childNodeName) => {
          const childNode = node?.[childNodeName]
          if (Array.isArray(childNode)) {
            childNode?.forEach(processNode)
          } else {
            processNode(childNode)
          }
        })
      }
    }

    props?.disclaimerConcept?.forEach(processNode)

    return {disclaimersContent}
  }
}
</script>
