<template>
  <section class="container-fluid py-4 d-flex flex-column justify-content-center align-items-center position-relative bg-black promo-bg-img z-index-0"
           :style="`background-image: url('${backgroundImage}');`"
           :class="{'mb-5': spacingBottom === 'true'}"
           data-test-id="promotional-content"
  >
    <div v-if="currentVariant && currentVariant.overlay" data-test-id="promotional-content-overlay" class="w-100 h-100 position-absolute overlay z-index-1" :class="`overlay-${currentVariant.overlay} opacity-${opacity}`" />
    <div class="container p-0 z-index-1">
      <div v-if="currentVariant" class="w-100 row mx-0 justify-content-center">
        <div class="w-100 d-flex justify-content-center flex-column text-white" :class="currentVariant.content">
          <component :is="htag" data-test-id="promotional-content-title" class="w-100" :class="[currentVariant.text, htag]">{{ title }}</component>
          <p data-test-id="promotional-content-paragraph" class="w-100" :class="currentVariant.text">{{ paragraph }}</p>
          <b-avatar v-if="avatarImage"
                    data-test-id="promotional-content-avatar"
                    variant="info"
                    :src="avatarImage"
                    :alt="avatar.imageAlt"
                    size="2.5rem"
          />
          <a
            v-if="linkField && linkField.label"
            :href="linkField.url"
            :target="linkField.target === '_blank_follow' ? '_blank' : linkField.target"
            :rel="linkField.target === '_blank' ? 'noopener nofollow' : null"
            class="d-lg-flex justify-content-center px-3 py-2 my-3 btn btn-primary"
            data-test-id="promotional-content-button"
            @click="$ga4.events.promotional_banner_cta($event, nodeId)"
          >
            {{ linkField.label }}
          </a>
        </div>
      </div>
    </div>
  </section>
</template>

<script>
import {ref, computed, useContext} from '@nuxtjs/composition-api'
import useMounted from '~/helpers/composables/useMounted'

export default {
  props: ['title', 'paragraph', 'link', 'avatar', 'backgroundImg', 'variant', 'opacity', 'spacingBottom', 'htag', 'nodeId'],
  setup(props) {
    const {$utils, $regions} = useContext()
    const {link, backgroundImg, avatar, variant} = props
    const backgroundImage = ref($utils.getCloudinaryAsset(backgroundImg, true, {crop: 'fill', width: 1440, height: 692}))
    const avatarImage = ref($utils.getCloudinaryAsset(avatar?.src, true, {crop: 'fill', width: 40, height: 40}))
    const linkField = ref($utils.getLinkField(link, $regions.current.language.name))
    useMounted(props.nodeId)

    const currentVariant = computed(() => {
      if (variant === 'left') {
        return {
          text: 'text-left',
          content: 'align-items-start',
          overlay: 'l'
        }
      } else if (variant === 'right') {
        return {
          text: 'text-right',
          content: 'align-items-end',
          overlay: 'r'
        }
      } else {
        return {
          text: 'text-center',
          content: 'align-items-center',
          overlay: 't'
        }
      }
    })

    return {
      backgroundImage,
      avatarImage,
      linkField,
      currentVariant
    }
  }
}
</script>

<style lang="scss">
@import '~bootstrap/scss/functions';
@import '~/assets/sass/variables';
@import '~bootstrap/scss/mixins';

.promo-bg-img {
  background: no-repeat fixed center center/cover;
  @include media-breakpoint-down(md) {
    background-attachment: initial;
  }
}

 .overlay {
    top: 0;
    left: 0;

    &-l {
      background: linear-gradient(-270deg, #000000 0%, rgba(0, 0, 0, 0) 100%);
    }

    &-r {
      background: linear-gradient(270deg, #000000 0%, rgba(0, 0, 0, 0) 100%);
    }

    &-t {
      background: linear-gradient(180deg, #000000 0%, rgba(0, 0, 0, 0) 100%);
    }
  }
</style>
