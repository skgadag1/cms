<template>
  <section data-test-id="hero-spot-v2" class="hero-spot-v2">
    <div class="hero-spot-v2-overlay position-absolute w-100 h-100" />
    <video
      v-if="assetMapped.video"
      class="hero-spot-v2-asset"
      autoplay
      muted
      loop
      preload="metadata"
      playsinline
      fetchpriority="high"
      :poster="assetMapped.poster"
      :width="assetMapped.width || null"
      :height="assetMapped.height || null"
    >
      <source :src="assetMapped.video" type="video/webm">
      <source :src="assetMapped.video" type="video/mp4">
      Your browser does not support HTML5 video.
    </video>
    <ImageSet
      v-else-if="assetMapped.img"
      class="hero-spot-v2-asset"
      :preload="true"
      :src="assetMapped.img"
      :alt="assetMapped.alt"
      :title="assetMapped.title"
      :img-width="assetMapped.imgWidth"
      :img-height="assetMapped.imgHeight"
      :options="{loading: 'eager', fetchpriority: 'high'}"
      crop="limit"
      :sizes="[]"
      :aspect-ratio="null"
    />
    <div class="hero-spot-v2-content container text-center">
      <div class="hero-spot-v2-head w-100 text-uppercase align-content-end text-white">
        <div class="hero-spot-v2-title mb-3" v-html="title" />
        <div class="hero-spot-v2-description font-weight-bold" v-html="description" />
      </div>
      <div class="hero-spot-v2-ctas d-flex flex-column flex-sm-row align-items-center justify-content-center mt-3">
        <DynamicButton
          v-for="(button, index) in ctas"
          :key="`hero-spot-cta-button-${index}`"
          :variant="button.variant"
          :color="button.color"
          :href="button.link"
          :target="button.target === '_blank_follow' ? '_blank' : button.target"
          class="mr-0 mr-sm-2 ml-sm-2 mb-sm-0 btn-only-mobile-block text-uppercase"
          :class="index < ctas.length - 1 ? 'mb-3' : ''"
          data-test-id="cta-button"
          @click="ga4click"
        >
          {{ button.title }}
        </DynamicButton>
      </div>
    </div>
  </section>
</template>

<script lang="js">
import {computed, inject, useContext} from '@nuxtjs/composition-api'
import {convertToPlain} from '../helpers/functional/AppHelpers'
import {resolveLegacyListSource} from '~/helpers/i18nCompat'

export default {
  props: {
    title: {
      type: String
    },
    description: {
      type: String
    },
    asset: {
      type: Object
    },
    cta: {
      type: Object,
      default: () => {
        return {}
      }
    }
  },
  setup(props, context) {
    const {cta, asset} = props
    const {$utils, $regions, $ga4, $config} = useContext()
    const model = inject('model')

    const ctaSource = resolveLegacyListSource(
      cta, context.attrs, 'cta', $regions.current.language.name, $config.USE_LEGACY_I18N_STRUCTURE
    )

    const heroImageDeliveryOptions = {
      fetch_format: 'auto',
      crop: 'fill',
      gravity: 'auto',
      type: 'upload'
    }

    const assetMapped = computed(() => {
      if (asset?.img?.src) {
        const obj = {}
        const {publicId, metadata} = $utils.getCloudinaryAssetMetadata(asset?.img?.src)
        Object.assign(obj, {
          file_name: publicId,
          img: asset?.img?.src,
          imgUrl: $utils.getCloudinaryAsset(asset.img.src, true, {...heroImageDeliveryOptions}),
          title: asset?.img?.imageTitle || metadata?.context?.caption || metadata?.title,
          alt: asset?.img?.imageAlt || metadata?.context?.alt || metadata?.description || metadata?.['alt_' + $regions.current.language.name],
          imgWidth: asset?.img?.imageWidth || null,
          imgHeight: asset?.img?.imageHeight || null,
          ...metadata
        })
        return obj
      }

      if (asset?.video?.src) {
        const {publicId, metadata} = $utils.getCloudinaryAssetMetadata(asset?.video?.src)
        // Authored still only — never fall back to Cloudinary e_preview (large, wrong format).
        // 960w + q_auto:low keeps transfer ≤ 100 KB on busy stills; crop to the video AR avoids CLS.
        const posterOptions = {
          fetch_format: 'auto',
          quality: 'auto:low',
          width: 960,
          dpr: 1,
          crop: 'fill',
          gravity: 'auto',
          ...(metadata?.aspect_ratio && {aspect_ratio: metadata.aspect_ratio})
        }

        return {
          ...metadata,
          file_name: publicId,
          video: $utils.getCloudinaryAsset(asset?.video?.src, false),
          poster: $utils.getCloudinaryAsset(asset?.video?.poster, true, posterOptions),
          title: metadata?.context?.caption || metadata?.title,
          alt: metadata?.context?.alt || metadata?.description
        }
      }

      return {}
    })

    const ga4click = (event) => {
      $ga4.events.hero_spot_cta(
        event,
        {
          asset_id: assetMapped.value.file_name,
          header: convertToPlain(props.title),
          text: convertToPlain(props.description)
        },
        model)
    }

    const ctas = computed({
      set: () => {
      },
      get: () => {
        return ctaSource?.['@nodes']
          ? ctaSource['@nodes'].map((x) => {
            const ctaButton = ctaSource[x].link
            const linkField = $utils.getLinkField(ctaButton, $regions.current.language.name)
            return {
              variant: ctaSource[x]?.field,
              color: ctaSource[x]?.color,
              link: linkField?.url || '',
              title: linkField?.label || '',
              target: linkField?.target || '_self',
              config: {
                class: ctaSource[x]?.variant || 'bg-primary text-white'
              }
            }
          })
          : []
      }
    })

    return {assetMapped, ctas, ga4click}
  },
  head() {
    const links = []
    if (this.assetMapped?.imgUrl) {
      links.push({
        rel: 'preload',
        as: 'image',
        fetchpriority: 'high',
        href: this.assetMapped.imgUrl
      })
    }
    if (this.assetMapped?.poster) {
      links.push({
        rel: 'preload',
        as: 'image',
        fetchpriority: 'high',
        href: this.assetMapped.poster
      })
    }
    if (this.assetMapped?.video) {
      links.push({
        rel: 'preload',
        as: 'video',
        href: this.assetMapped.video,
        fetchpriority: 'high'
      })
    }
    return {link: links}
  }
}

</script>

<style lang="scss" scoped>
@import '~bootstrap/scss/functions';
@import '~/assets/sass/variables';
@import '~bootstrap/scss/mixins';
@import '~/assets/sass/utils';

.hero-spot-v2 {
  height: 720px;
  width: 100%;

  &-overlay {
    position: absolute;
    top:0;
    left: 0;
    z-index: 1;
    background: linear-gradient(to bottom, rgba(0, 0, 0, 0) 64%, rgba(0, 0, 0, 1) 80%);
  }

  &-asset {
    position: absolute;
    width: 100%;
    height: 100%;
    object-fit: cover;
    object-position: 50% 60%;
  }

  @include media-breakpoint-down(lg) {
    height: auto;
    aspect-ratio: 16 / 9;

    &-overlay {
      background: linear-gradient(to bottom, rgba(0, 0, 0, 0) 57%, rgba(0, 0, 0, 1) 75%);
    }

    &-asset {
      object-position: 50% 75%;
    }
  }

  @include media-breakpoint-down(md) {
    height: clamp(380px, 75vh, 720px);
    max-height: 720px;

    &-overlay {
      background: linear-gradient(to bottom, rgba(0, 0, 0, 0) 45%, rgba(0, 0, 0, 1) 70%);
    }

    &-asset {
      object-position: 50% 80%;
    }
  }

  @include media-breakpoint-down(sm) {
    height: clamp(340px, 75vh, 600px);
    max-height: 600px;

    &-overlay {
      background: linear-gradient(to bottom, rgba(0, 0, 0, 0) 70%, rgba(0, 0, 0, 1) 85%);
    }

    &-asset {
      object-position: 50% 80%;
    }
  }

  &-title,
  &-description {
    :deep(h1), :deep(h2), :deep(h3), :deep(h4), :deep(h5), :deep(h6), :deep(p) {
      margin-bottom: 0;
    }

    // Fix line-height when CMS injects inline font-size via spans.
    // The outer heading tag keeps its own line-height; collapse it to match
    // the inner span's visual size.
    :deep(h1), :deep(h2), :deep(h3), :deep(h4), :deep(h5), :deep(h6) {
      line-height: 1.1;

      span[style*="font-size:"] {
        line-height: 1.1;
      }
    }
  }

  @include media-breakpoint-down(xs) {
    height: auto;
    aspect-ratio: 5 / 7;

    &-overlay {
      background: linear-gradient(to bottom, rgba(0, 0, 0, 0) 30%, rgba(0, 0, 0, 1) 50%);
    }

    &-asset {
      top: -10rem;
    }

    &-title,
    &-description {
      $scale: 0.75;
      :deep(h4) { font-size: $h4-font-size * $scale; }
      :deep(h5) { font-size: $h5-font-size * $scale; }
      :deep(h6) { font-size: $h6-font-size * $scale; }
      :deep(p) { font-size: $font-size-base * $scale; }
    }
  }

  &-content {
    display: grid;
    grid-template-rows: 1fr auto;
    height: 100%;
    padding-top: 2rem !important;   /* 32px */
    padding-bottom: 2rem !important;/* 32px */
  }

  &-ctas {
    z-index: 10;
  }

  &-head {
    display: flex;
    flex-direction: column;
    justify-content: flex-end;
    // gap: 1rem;
    z-index: 10;
  }

}
</style>
