<template>
  <LazyHydrate>
    <b-card
      v-if="promoCampaign"
      border-variant="transparent"
      class="overflow-hidden h-100 transition-base position-relative promo-model"
      :class="{'product-listing' : isProductListing}"
      no-body
      footer-tag="footer"
      data-test-id="promo-model"
    >
      <div class="w-100 h-100 position-absolute">
        <div class="w-100 h-100 promo-overlay position-absolute" />
        <b-img-lazy v-if="promoCampaign.image && promoCampaign.image.src"
                    class="w-100 h-100"
                    :src="promoCampaign.image.src"
                    :alt="promoCampaign.image.alt"
                    :title="promoCampaign.image.title"
                    decoding="async"
        />
      </div>
      <div class="z-index-1 text-white p-3 d-flex flex-column align-items-center justify-content-end h-100">
        <a v-if="promoCampaign.link && promoCampaign.link.url"
           :href="promoCampaign.link.url"
           :target="promoCampaign.link.target === '_blank_follow' ? '_blank' : promoCampaign.link.target || '_self'"
           class="stretched-link"
           :rel="promoCampaign.link.target === '_blank' ? 'noopener nofollow' : null"
           @click="ga4click"
        />

        <h5 v-if="promoCampaign.title" class="w-100">{{ promoCampaign.title }}</h5>
        <p v-if="promoCampaign.description" class="w-100">{{ promoCampaign.description }}</p>
        <a v-if="promoCampaign.link && promoCampaign.link.url"
           :href="promoCampaign.link.url"
           :target="promoCampaign.link.target === '_blank_follow' ? '_blank' : promoCampaign.link.target || '_self'"
           class="d-lg-flex justify-content-center px-5 py-2 btn btn-primary w-100 text-capitalize"
           :rel="promoCampaign.link.target === '_blank' ? 'noopener nofollow' : null"
           @click="ga4click"
        >
          {{ promoCampaign.link.label }}
        </a>
      </div>
    </b-card>
  </LazyHydrate>
</template>

<script>
import {useContext} from '@nuxtjs/composition-api'

export default {
  props: {
    promoCampaign: {
      type: Object
    },
    isProductListing: {
      type: Boolean,
      default: false
    }
  },
  setup(props) {
    const {promoCampaign, isProductListing} = props
    const {$ga4} = useContext()

    const ga4click = (event) => {
      if (isProductListing) {
        $ga4.events.campaign_card_cta(event, promoCampaign)
      }
    }

    return {
      ga4click
    }
  }
}
</script>

<style lang="scss" scoped>
@import '~bootstrap/scss/functions';
@import '~/assets/sass/variables';
@import '~/assets/sass/colors';

.promo-model {

  &.product-listing {
    min-height: 30rem;
  }

  img {
    object-fit: cover;
  }

  .promo-overlay {
    background: linear-gradient(180deg, rgba(0, 0, 0, 0) 0%, $black 100%);
  }

}
</style>
