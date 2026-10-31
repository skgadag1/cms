<template>
  <component :is="areaType"
             v-if="nodes"
             ref="element"
             data-test-id="area"
             :data-test-name="`area-${content['@name']}`"
             :role="areaType === 'main' ? 'main' : ''"
             class="clearfix"
  >
    <LazyHydrate v-for="(node, i) in nodes"
                 :key="`editable-component__${i}`"
                 :trigger-hydration="hasAnchor || node.shouldPreload"
                 :when-visible="{ rootMargin: node.shouldPreload ? '1000%' : '350px' }"
                 when-idle
    >
      <EditableComponent :node="node"
                         :preload="node.shouldPreload"
                         :critical="node.shouldPreload"
      />
    </LazyHydrate>
  </component>
</template>

<script>
import {ref, computed, useContext, inject, onMounted} from '@nuxtjs/composition-api'
import {ARTICLE_PAGE_TEMPLATE_NAME} from '~/constants/pageTemplates'
import {inEditorEdit, getProps, insertComments, inMgnlPreview} from '~/helpers/vue-editor'

const COMPONENTS_TO_PRELOAD = 4 // value set by experience

export default {
  props: ['content', 'parentTemplateId', 'hasAnchor'],
  setup(props) {
    const element = ref(null)
    const pageTemplateId = inject('pageTemplateId')
    const templateAnnotations = inject('templateAnnotations')
    const {content, parentTemplateId} = props
    const {$isBot} = useContext()
    const isArticle = computed(() => ((parentTemplateId || pageTemplateId) === ARTICLE_PAGE_TEMPLATE_NAME) && content['@name'] === 'main')
    const nodes = computed(() => {
      return content?.areaNodes?.map((node, i) => {
        return {
          ...(typeof node === 'string' ? {id: node} : node),
          shouldPreload: $isBot || (!inEditorEdit && (node.preloaded === 'true' || i < (isArticle ? COMPONENTS_TO_PRELOAD + 2 : COMPONENTS_TO_PRELOAD)))
        }
      }) || []
    })

    const areaType = computed(() => {
      if (isArticle.value) { return 'article' }
      if (content['@name'] === 'main') { return 'main' }
      return 'div'
    })

    const componentProps = computed(() => {
      return getProps(content)
    })

    onMounted(() => {
      if (inEditorEdit && !inMgnlPreview) {
        const $el = element.value?.$el || element.value
        insertComments($el, templateAnnotations[content['@path']], '/cms:area')
      }
    })

    return {
      element,
      COMPONENTS_TO_PRELOAD,
      inEditorEdit,
      areaType,
      nodes,
      componentProps
    }
  }
}
</script>
