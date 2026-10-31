<template>
  <div
    ref="contentWrapper"
    data-test-id="collapsible-content"
    class="bottom-dropdown z-index-5 w-100"
    :class="isExpanded ? 'fixed-bottom' : 'position-sticky'"
  >
    <div class="content-wrapper">
      <transition name="fade">
        <slot v-if="isExpanded" />
      </transition>
    </div>
    <b-button variant="outline-primary"
              class="menu-button w-100 border-0 d-flex align-items-center justify-content-center shadow text-large"
              @click="toggleContent"
    >
      <div>{{ buttonLabel }}</div>
      <span class="ml-2 transition-base" :style="!isExpanded ? 'transform: rotate(180deg)' : null"><b-icon icon="sort-down" class="dropdown" /></span>
    </b-button>
  </div>
</template>

<script>
import {ref} from '@nuxtjs/composition-api'

export default {
  props: {
    buttonLabel: {
      type: String
    }
  },
  setup() {
    const isExpanded = ref(false)

    const toggleContent = () => {
      isExpanded.value = !isExpanded.value
    }

    const hide = () => {
      isExpanded.value = false
    }

    return {
      isExpanded,
      hide,
      toggleContent
    }
  }
}
</script>

<style lang="scss" scoped>
.bottom-dropdown {
  bottom: -1px;
}
.content-wrapper > * {
  max-height: 70vh; // limit
}

.menu-button {
  height: 3rem;
}
</style>
