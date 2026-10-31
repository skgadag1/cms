<template>
  <div class="compare-item d-flex flex-column shadow rounded small position-relative transition-base"
       data-test-id="compare-item"
       :class="[compare ? 'p-2' : 'compared mx-sm-3', dark && empty ? 'bg-dark' : 'bg-white']"
  >
    <div v-if="showRemove && isRemovable"
         class="icon-remove p-1 position-absolute cursor-pointer d-inline-flex align-items-center justify-content-center rounded-circle border border-white shadow bg-dark text-white"
         data-test-id="compare-item-remove"
         @click.prevent="$emit('remove', model['@id'])"
    >
      <b-icon icon="trash" />
    </div>
    <template v-if="!empty">
      <div class="mx-1 mb-1 d-flex flex-column flex-grow-1">
        <h6 class="small text-truncate" :class="[compare ? 'm-1' : 'my-1']" data-test-id="compare-item-title">{{ model.title || model['@name'] }}</h6>
        <div class="text-center flex-grow-1" :class="{'m-1': compare}" data-test-id="compare-item-img">
          <b-img-lazy v-if="productImage && productImage.src"
                      :src="productImage.src"
                      :alt="productImage.alt"
                      :title="productImage.title"
                      decoding="async"
                      fluid
          />
        </div>
        <div class="text-truncate" v-html="subtitle" />
      </div>
      <div v-if="compare"
           class="h6 d-flex align-items-center justify-content-end small mb-0 mx-n2 mb-n2 p-2 bg-gray-lighter"
           :class="{'cursor-pointer': isRemovable && (selected || (!selected && !disabled))}"
           data-test-id="compare-item-compare-button"
           :title="generateConceptHint('compare')"
           @click.prevent="() => { if(isRemovable && (!disabled || (disabled && selected))) selected ? $emit('remove', model['@id']) : $emit('add', model)}"
      >
        {{ compareLabel || 'Compare' }} <b-form-checkbox :checked="[selected]" :disabled="disabled || !isRemovable" class="d-inline-block mr-n1 ml-1 custom-control-input__dark transition-base" />
      </div>
    </template>
    <div v-else
         data-test-id="compare-item-empty"
         class="d-flex align-items-center justify-content-center border-custom__dashed cursor-pointer h-100 w-100"
         :class="{'border-custom__gray' : disabled, 'border-custom__white' : dark}"
         @click.prevent="$emit(disabled ? 'activate' : 'empty-trigger')"
    >
      <b-icon icon="plus" :class="[disabled ? 'text-muted' : (dark ? 'text-white' : 'text-primary')]" />
    </div>
    <slot />
  </div>
</template>

<script>
import {ref, useContext} from '@nuxtjs/composition-api'
import useConcepts from '~/helpers/composables/useConcepts'
import useCloudinary from '~/helpers/composables/useCloudinary'

export default {
  props: {
    model: {
      type: Object,
      required: true
    },
    compare: {
      type: Boolean,
      required: false,
      default: true
    },
    showRemove: {
      type: Boolean,
      default: false
    },
    selected: {
      type: Boolean,
      default: false
    },
    disabled: {
      type: Boolean,
      default: false
    },
    isRemovable: {
      type: Boolean,
      default: true
    },
    compareLabel: {
      type: String
    },
    dark: {
      type: Boolean
    }
  },
  setup(props) {
    const {compare, model} = props
    const {$utils} = useContext()
    const {generateConceptHint} = useConcepts()

    const imgWidth = compare ? 140 : 80
    const imgHeight = compare ? 120 : 42
    const {placeholderImage} = useCloudinary({width: imgWidth, height: imgHeight, crop: 'lpad'})

    const firstModelImage = $utils.products.getFirstModelImage(model)
    const productImage = {
      src: $utils.getCloudinaryAsset(firstModelImage?.img?.src, true, {crop: 'lpad', width: imgWidth, height: imgHeight}) || placeholderImage,
      alt: firstModelImage?.img?.imageAlt,
      title: firstModelImage?.img?.imageTitle
    }
    const empty = ref(model ? !Object.keys(model).length : true) // empty model object determine empty item

    const subtitle = model?.properties?.[0]?.value || ''

    return {
      empty,
      subtitle,
      imgHeight,
      imgWidth,
      productImage,
      generateConceptHint
    }
  }
}
</script>

<style lang="scss">
@import '~bootstrap/scss/functions';
@import '~/assets/sass/variables';
@import '{.h-100} from ~bootstrap/scss/utilities/sizing';

.compare-item {
  &:not(.compared) {
    @extend .h-100;
  }

  &.compared {
    width: 5.5rem;
    height: 5.5rem;
  }

  .icon-remove {
    top: -.25rem;
    right: -.25rem;
  }
}
</style>
