<template>
  <b-img
    :alt="alt"
    :title="title"
    :blank-src="placeholderImage"
    :src="img"
    :srcset="variants"
    decoding="async"
    v-bind="options"
    :loading="imageLoading"
    :fetchpriority="imageFetchPriority"
    :width="imgWidth || options.width || ((options.fluid || options['fluid-grow']) && 300)"
    :height="imgHeight || options.height || ((options.fluid || options['fluid-grow']) && 200)"
    @click="$emit('click')"
  />
</template>

<script>
import {computed, useContext, toRefs} from '@nuxtjs/composition-api'
import useCloudinary from '~/helpers/composables/useCloudinary'
import useSchema from '~/helpers/composables/useSchema'

export default {
  props: {
    src: {
      type: String,
      default: ''
    },
    isImg: {
      type: Boolean,
      default: true
    },
    title: {
      type: String,
      default: ''
    },
    alt: {
      type: String,
      default: ''
    },
    sizes: {
      type: Array,
      default: () => {
        return [160, 320, 480, 640, 720, 1140, 2048]
      }
    },
    aspectRatio: {
      type: [Number, String],
      default: 3 / 2
    },
    cloudinaryOptions: {
      type: Object,
      default: () => { return {} }
    },
    options: {
      type: Object,
      default: () => { return {'fluid-grow': true} }
    },
    imgWidth: {
      type: [Number, String],
      default: null
    },
    imgHeight: {
      type: [Number, String],
      default: null
    },
    expectedWidth: {
      type: Number,
      default: 480
    },
    preload: {
      type: Boolean,
      default: false
    },
    maintainAspectRatio: {
      type: Boolean,
      default: true
    }
  },
  setup(props) {
    const {src, sizes, aspectRatio, cloudinaryOptions, isImg, expectedWidth, title} = toRefs(props)
    const {$utils, $regions, $config} = useContext()
    const {createImageObject} = useSchema({}, $utils, $regions, $config)
    const srcWithFallback = computed(() => src.value || $config.PLACEHOLDER_IMAGE)
    const jsonLdSchema = createImageObject({src: srcWithFallback.value, name: title.value || ''}, expectedWidth.value, isNaN(aspectRatio.value) || !aspectRatio.value ? 'auto' : expectedWidth.value / aspectRatio.value)
    const cropFromProps = props.maintainAspectRatio ? 'fill' : 'pad'
    const needsGravity = ['fill', 'thumb', 'crop'].includes(cropFromProps)
    const gravityFromProps = needsGravity ? 'auto' : undefined
    const hasAspectRatio = aspectRatio.value !== null && aspectRatio.value !== undefined && aspectRatio.value !== '' && !isNaN(Number(aspectRatio.value))

    const imageOptions = {
      ...(sizes.value.length && {width: expectedWidth.value}),
      ...(hasAspectRatio && {aspect_ratio: Number(aspectRatio.value)}),
      fetch_format: 'auto',
      crop: cropFromProps,
      ...(gravityFromProps && {gravity: gravityFromProps}),
      ...cloudinaryOptions.value,
      type: 'upload'
    }

    const {placeholderImage, getImageSizeVariants} = useCloudinary(imageOptions)
    const img = computed(() => srcWithFallback.value && srcWithFallback.value.startsWith('cloudinary:') ? $utils.getCloudinaryAsset(srcWithFallback.value, isImg.value, imageOptions) : (srcWithFallback.value || placeholderImage))
    const variants = computed(() => srcWithFallback.value ? getImageSizeVariants(srcWithFallback.value, isImg.value, sizes.value, imageOptions) : [])
    const isEagerImage = computed(() => props.preload || props.options?.loading === 'eager')
    const imageLoading = computed(() => isEagerImage.value ? 'eager' : (props.options?.loading || 'lazy'))
    const imageFetchPriority = computed(() => {
      if (props.options?.fetchpriority) {
        return props.options.fetchpriority
      }
      return isEagerImage.value ? 'high' : null
    })

    return {
      img,
      variants,
      placeholderImage,
      imageOptions,
      jsonLdSchema,
      imageLoading,
      imageFetchPriority
    }
  },
  head() {
    return this?.preload
      ? {
        link: [
          {
            rel: 'preload',
            as: 'image',
            fetchpriority: 'high',
            href: this?.img
          }
        ]
      }
      : {}
  },
  jsonld() {
    return [this.jsonLdSchema].filter(x => x)
  }
}
</script>
