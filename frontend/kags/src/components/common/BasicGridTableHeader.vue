<template>
  <div class="d-flex align-items-center" :class="[ 'header__' + name, sortable ? 'sortable' : '' ]" @click="$emit('click', $event)">
    <span><slot /></span>
    <span v-if="sortable" class="carret" :class="{'carret-up': !sortingActive || sortingDir === '+', 'carret-down': !sortingActive || sortingDir === '-'}" />
  </div>
</template>

<script>
export default {
  emits: ['click'],
  props: {
    name: {
      type: String,
      default: '-',
      required: true
    },
    sortable: {
      type: Boolean,
      default: false,
      required: false
    },
    sortingActive: {
      type: Boolean,
      default: false,
      required: false
    },
    sortingDir: {
      type: String,
      default: '-',
      validator(value) {
        return ['-', '+'].indexOf(value) !== -1
      },
      required: false
    }
  }
}
</script>
