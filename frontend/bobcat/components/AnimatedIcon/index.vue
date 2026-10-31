<template>
  <div>
    <component :is="dynamicIcon" v-if="dynamicIcon" :variant="variant" />
  </div>
</template>

<script>
import {computed} from '@nuxtjs/composition-api'
import MachineIcon from './MachineIcon.vue'
import WarrantyIcon from './WarrantyIcon.vue'
import ServiceIcon from './ServiceIcon.vue'
import NetworkIcon from './NetworkIcon.vue'

export default {
  components: {
    MachineIcon,
    WarrantyIcon,
    ServiceIcon,
    NetworkIcon
  },
  props: {
    name: {
      type: String
    },
    variant: {
      type: String,
      default: 'light'
    }
  },
  setup(props) {
    const dynamicIcon = computed(() => {
      if (props.name === 'warrantyIcon') { return WarrantyIcon }
      if (props.name === 'machineIcon') { return MachineIcon }
      if (props.name === 'networkIcon') { return NetworkIcon }
      if (props.name === 'serviceIcon') { return ServiceIcon }
      return null
    })

    return {
      dynamicIcon
    }
  }
}
</script>
