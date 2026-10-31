<template>
  <div>
    <b-badge
      v-if="Object.keys(selectedType).length > 0 && !defaultProductType"
      pill
      variant="dark"
      class="mr-2 mb-3 cursor-pointer"
      @click.prevent="removeType"
    >
      <span class="d-flex align-items-center">
        {{ labels.productType || 'Product Type' }}: {{ selectedType.label }}
        <b-icon icon="remove" class="ml-2" />
      </span>
    </b-badge>
    <b-badge v-for="(chip, index) in selectedModels"
             :key="`item-chips__${chip.value}_${index}`"
             pill
             variant="dark cursor-pointer"
             class="mr-2 mb-3"
             @click.prevent="removeModel(selectedModels, chip)"
    >
      <span class="d-flex align-items-center">
        {{ labels.model || 'Model' }}: {{ chip.label }}
        <b-icon icon="remove" class="close-icon ml-2" />
      </span>
    </b-badge>
  </div>
</template>

<script>
export default {
  props: {
    selectedType: Object,
    selectedModels: Array,
    defaultProductType: Boolean,
    labels: Object
  },
  emits: ['removeType', 'removeModel'],
  setup(props, {emit}) {
    const removeType = () => {
      emit('removeType')
    }

    const removeModel = (models, chip) => {
      emit('removeModel', models, chip)
    }

    return {
      removeType,
      removeModel
    }
  }
}
</script>
