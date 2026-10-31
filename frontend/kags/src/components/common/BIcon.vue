<template>
  <span class="d-inline-block" @click="$emit('click', $event)">
    <span v-if="icon === 'customicon-fieldmods'" class="fa-layers fa-fw">
      <font-awesome-icon icon="search" />
      <font-awesome-icon
        icon="exclamation"
        transform="shrink-11 up-1.3 left-1.6"
      />
    </span>
    <font-awesome-icon
      v-else-if="isFontAwesomeIcon"
      :fixed-width="fixedWidth"
      :rotation="rotation"
      :flip="flip"
      :icon="icon"
    />
    <bicon v-else :class="'bobcaticon-' + icon" />
  </span>
</template>

<script>
import {FontAwesomeIcon} from '@fortawesome/vue-fontawesome';

export default {
  components: {
    FontAwesomeIcon,
  },
  emits: ['click'],
  props: {
    icon: {
      required: true,
    },
    fixedWidth: {
      required: false,
      default: false,
    },
    flip: {
      type: String,
      required: false,
    },
    rotation: {
      type: String,
      required: false,
    },
  },
  computed: {
    isFontAwesomeIcon() {
      const customIcons = [
        "clone",
        "loader",
        "service-analyzer",
        "file",
        "ev",
        "sparkles",
        "fieldmods",
        "arrow-up",
        "plus",
        "attachment",
        "bin",
        "close",
        "info",
        "filecopy",
        "warning"
      ];
      return !customIcons.includes(this.icon);
    },
  },
};
</script>
