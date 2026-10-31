<template>
  <section class="container py-3">
    <WarningAlert v-if="inEditorEdit">This component has been renamed or doesn't exist.</WarningAlert>
  </section>
</template>

<script>
import {inEditorEdit} from '~/helpers/vue-editor'

export default {
  setup() {
    return {
      inEditorEdit
    }
  }
}
</script>
