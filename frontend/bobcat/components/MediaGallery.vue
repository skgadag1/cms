<template>
  <section class="container py-4 media-gallery">
    <LazyHydrate never :trigger-hydration="images.length > 0">
      <div v-if="images.length" class="row">
        <div class="col-12" :class="{'col-sm-8 col-lg-6 offset-sm-2 offset-lg-3' : articleColumn === 'true'}">
          <CloudinaryGallery :images="images" :title="title" :class="{'cld-gallery-full' : articleColumn !== 'true'}" :config-overrides="layoutConfig" />
        </div>
      </div>
    </LazyHydrate>
  </section>
</template>

<script>
import {computed} from '@nuxtjs/composition-api'
import VERTICAL_GALLERY_OVERRIDES from '~/constants/galleryConfigurationVertical'
import useMounted from '~/helpers/composables/useMounted'

export default {
  props: {
    nodeId: {
      type: String
    },
    galleryImages: {
      type: Object,
      required: true
    },
    title: {
      type: String
    },
    articleColumn: {
      required: false,
      type: String,
      default: 'false'
    },
    layout: {
      required: false,
      type: String,
      default: 'bottom'
    }
  },
  setup(props) {
    const {galleryImages, layout} = props
    const images = computed(() => {
      if (galleryImages && galleryImages['@nodes']) {
        // gallery v1
        const images = galleryImages['@nodes'].filter(y => galleryImages[y].image?.src).map(x => galleryImages[x].image) || []
        // gallery v2
        const items = galleryImages['@nodes'].filter(y =>
          (galleryImages[y]?.image?.field === 'asset' && galleryImages[y]?.image?.asset?.src) ||
          (galleryImages[y]?.image?.field === 'tag' && galleryImages[y]?.image?.tag) ||
          (galleryImages[y]?.image?.field === 'spinset' && galleryImages[y]?.image?.tag) ||
          null
        ).map(x =>
          (galleryImages[x]?.image.field === 'asset' && galleryImages[x]?.image?.asset) ||
          (galleryImages[x]?.image.field === 'tag' && {field: 'image', tag: galleryImages[x]?.image?.tag}) ||
          (galleryImages[x]?.image.field === 'spinset' && {field: 'spin', tag: galleryImages[x]?.image?.tag}) ||
          null
        ).filter(z => z) || []
        return items.concat(images)
      } else {
        return []
      }
    })
    useMounted(props.nodeId)

    let layoutConfig = null

    if (layout === 'right') {
      layoutConfig = VERTICAL_GALLERY_OVERRIDES
    }

    return {
      layoutConfig,
      images
    }
  }
}
</script>
