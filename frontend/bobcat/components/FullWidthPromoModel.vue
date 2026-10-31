<template>
  <LazyHydrate when-visible>
    <section
      v-if="campaign"
      data-test-id="full-width-promo-model"
      class="position-relative w-100 promo-model text-white d-flex justify-content-end align-items-center"
    >
      <div class="w-100 h-100 position-absolute">
        <div class="w-100 h-100 promo-overlay position-absolute" />
        <ImageSet
          v-if="campaign.image && campaign.image.src"
          class="w-100 h-100"
          :src="campaign.image.src"
          :alt="campaign.image.alt"
          :title="campaign.image.title"
          :img-width="campaign.image.width"
          :img-height="campaign.image.height"
        />
      </div>
      <div class="container">
        <div class="row d-flex justify-content-end align-items-center">
          <div class="col-md-6 col-12">
            <component :is="htag" v-if="campaign.title" :class="htag">
              {{ campaign.title }}
            </component>
            <p v-if="campaign.description">
              {{ campaign.description }}
            </p>
            <div class="d-flex justify-content-start">
              <a
                v-if="campaign.link && campaign.link.url"
                :href="campaign.link.url"
                :target="campaign.link.target === '_blank_follow' ? '_blank' : campaign.link.target"
                :rel="campaign.link.target === '_blank' ? 'noopener nofollow' : null"
                class="d-lg-flex justify-content-center btn-mobile px-4 py-2 mb-3 rounded-2 text-capitalize align-self-start btn btn-primary"
              >
                {{ campaign.link.label }}
              </a>
            </div>
          </div>
        </div>
      </div>
    </section>
  </LazyHydrate>
</template>

<script>
import {useContext, ref} from '@nuxtjs/composition-api'
import useMounted from '~/helpers/composables/useMounted'

export default {
  props: {
    nodeId: {
      type: String
    },
    htag: {
      type: String,
      required: true
    },
    marketingCampaigns: {
      type: Object,
      required: true
    }
  },
  setup(props) {
    const {marketingCampaigns} = props
    const {$utils, $regions} = useContext()
    useMounted(props.nodeId)
    const c = marketingCampaigns && marketingCampaigns['@nodeType'] !== 'mgnl:folder'
      ? {
        ...marketingCampaigns,
        image: {
          src: marketingCampaigns?.image?.src,
          alt: marketingCampaigns?.image?.imageAlt,
          title: marketingCampaigns?.image?.imageTitle,
          width: marketingCampaigns?.image?.imageWidth || null,
          height: marketingCampaigns?.image?.imageHeight || null
        },
        link: $utils.getLinkField(marketingCampaigns?.link, $regions.current.language.name)
      }
      : null

    const campaign = ref(c)

    return {campaign}
  }
}
</script>

<style lang="scss" scoped>
@import '~bootstrap/scss/functions';
@import '~/assets/sass/variables';
@import '~/assets/sass/utils';
@import '~bootstrap/scss/mixins';

.promo-model {
  min-height: 30rem;

  img {
    object-fit: cover;
  }

  .promo-overlay {
    background: linear-gradient(270deg, $black 0%, rgba(0, 0, 0, 0) 100%);

    @include media-breakpoint-down(sm) {
      background: linear-gradient(0, $black 0%, rgba(0, 0, 0, 0) 100%);
    }
  }
}
</style>
