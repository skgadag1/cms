<template>
  <div v-show="isActive">
    <slot />
  </div>
</template>

<script>
import {ref} from '@nuxtjs/composition-api'

export default {
  props: {
    active: {
      type: Boolean,
      default: () => { return false }
    }
  },
  setup(props) {
    const isActive = ref(props.active)
    const setActive = (val) => {
      isActive.value = val
    }
    return {isActive, setActive}
  }
}
</script>
