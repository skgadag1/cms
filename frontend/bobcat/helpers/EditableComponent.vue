<template>
  <b-overlay :id="id" ref="element" :show="(!preload || inEditorEdit) && $fetchState.pending" :class="[$fetchState.pending ? 'min-h300' : '', componentName]">
    <Component :is="config.componentMappings[componentTemplateId] || 'PlaceholderComponent'"
               v-if="componentTemplateId"
               v-bind="componentProps"
               :node-id="node.id"
               :ssr-current-region="currentRegion"
               :data-noindex="componentProps.noIndex === 'true'"
               class="clearfix"
               @component-name="handleComponentName"
    />
  </b-overlay>
</template>

<script>
import {ref, inject, useContext, useFetch, onMounted, provide, nextTick} from '@nuxtjs/composition-api'
import {getProps, inEditorEdit, insertComments, inMgnlPreview} from '~/helpers/vue-editor'

export default {
  inject: ['config', 'templateAnnotations', 'pageTemplateId'],
  props: ['node', 'preload'],
  setup(props) {
    const content = ref(null)
    const element = ref(null)
    const componentName = ref(null)
    const {$services, $regions, $config} = useContext()
    const {node, preload} = props
    const componentTemplateId = ref(null)
    const componentProps = ref(null)
    const templateAnnotations = inject('templateAnnotations')
    provide('shouldPreload', node.shouldPreload)
    const id = ref(null)

    const {$fetch} = useFetch(async () => {
      if ((preload || process.client) && !componentProps.value) {
        const currentNode = typeof node === 'string' ? {id: node} : node
        const {data} = await $services.content.fetchComponentDetails(currentNode.id)
        const contentTmp = {...data, editable: currentNode.editable}
        componentTemplateId.value = contentTmp['mgnl:template']
        componentProps.value = getProps(contentTmp)
        id.value = componentProps.value?.anchorID
        content.value = contentTmp

        if (id.value && process.client) {
          nextTick(() => { // wait for ID to be injected
            const loadedEvent = new Event('componentAnchor', {bubbles: true})
            element.value?.$el.dispatchEvent(loadedEvent)
          })
        }
      }
    })

    onMounted(async () => {
      await $fetch()

      if (inMgnlPreview || !(inEditorEdit && (content.value?.editable === 'true' || $config.USE_NATIVE_MAGNOLIA))) {
        return
      }

      const $el = element.value?.$el || element.value
      if (!$el || !content?.value?.['@path']) {
        // eslint-disable-next-line no-console
        console.warn('Missing item', content.value)
        return
      }

      const {EditorContextHelper} = await import('@magnolia/template-annotations')
      insertComments($el, templateAnnotations[content.value['@path']] || 'cms:component content="" dialog="" label="Placeholder" activationStatus="2"', '/cms:component')
      EditorContextHelper.onFrameReady()
    })

    const handleComponentName = (e) => {
      componentName.value = e
    }

    return {
      componentTemplateId,
      currentRegion: $regions.current,
      componentProps,
      inEditorEdit,
      element,
      id,
      componentName,
      handleComponentName
    }
  }
}
</script>

<style>
.min-h300 {
  min-height: 300px;
}
</style>
