<template>
  <div class="mb-5 flex-grow-1 flex-shrink-0 position-relative">
    <div class="banner-main no-navigation h-100" :style="'background-image: url(' + translatedConfig('bannerUrl') + ')'" />

    <MachineSearchComponent />
  </div>
</template>

<script>
import {mapGetters} from 'vuex'
import MachineSearchComponent from '@/components/common/MachineSearch/MachineSearchComponent'
import {useLabels} from '@/composables/useLabels'

export default {
  setup() {
    return useLabels()
  },
  components: {
    MachineSearchComponent
  },
  data() {
    return {
      componentConfig: this.config
    }
  },
  computed: {
    ...mapGetters([
      'isEmpty'
    ])
  }
}
</script>

<style lang="scss" scoped>
@import '../../scss/grid';

.banner-main {
  background-size: cover;
  background-position: 35% 50%;

  @include media-breakpoint-up('md') {
    background-position: 20% 50%;
  }

  @include media-breakpoint-up('xl') {
    background-position: 15% 50%;
  }

  &:after {
    display: none;
  }
}
</style>
