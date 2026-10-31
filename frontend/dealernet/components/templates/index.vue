<template>
    <main class="d-flex flex-column min-vh-100">
        <EditableArea v-if="header" :content="header" elementType="section" />
        <EditableArea v-if="main" :content="main" elementType="section" class="d-flex flex-column" :class="{'flex-grow-1' : !inEditorMode}" />
        <StaticTemplate v-if="header || main || footer"/>
        <EditableArea v-if="footer" :content="footer" elementType="section" />
    </main>
</template>

<script setup>
import { EditableArea, EditorContextHelper } from '@magnolia/vue-editor'
let inEditorMode = false

const props = defineProps({
  title: String,
  metadata: Object,
  header: Object,
  main: Object,
  footer: Object
})

onMounted(() => {
  inEditorMode = EditorContextHelper.inIframe()
})
</script>

<style lang="scss">
html, body {
    min-height: 100vh;
}
</style>