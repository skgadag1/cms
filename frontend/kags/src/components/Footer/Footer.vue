<template>
  <footer class="pt-4 pb-0 bg-gray-800 text-white flex-shrink-0">
    <b-container>
      <b-row class="pt-4">
        <b-col cols="12"
               md="6"
               class="mb-5"
        >
          <b-img :src="assetServer + '/assets/img/ota-logo.png'"
                 alt="One Though Animal"
                 width="214"
          />
        </b-col>

        <b-col cols="12" md="6" lg="3" offset-lg="1" data-test-id="footer-contactus">
          <div class="border border-gray-700 p-3 w-100">
            <b-row>
              <b-col align-v="center">
                <h4 class="text-uppercase">{{ translate('contactTitle', 'Footer') }}</h4>
                <a :href="config.contactUrl" class="font-condensed text-gray-600 cursor-pointer" data-test-id="footer-contact-link">{{ translate('contactSubtitle', 'Footer') }}</a>
              </b-col>
              <b-col cols="auto"
                     md
                     lg="auto"
                     class="ps-0"
                     align-v="center"
              >
                <b-icon icon="info-circle" class="display-2 text-gray-700" />
              </b-col>
            </b-row>
          </div>
          <div class="text-center p-3">
            <a class="font-condensed text-gray-600"
               data-test-id="footer-privacy-link"
               :href="config.privacyPolicyUrl"
            >{{ translate('privacyPolicy', 'Footer') }}</a>
            <span class="p-2 text-gray-600 small">I</span>
            <a class="font-condensed text-gray-600"
               data-test-id="footer-termsofuse-link"
               :href="config.termOfUseUrl"
            >{{ translate('termsOfUse', 'Footer') }}</a>
          </div>
        </b-col>
      </b-row>
    </b-container>
    <b-container fluid class="bg-white">
      <b-container>
        <b-row class="mt-3 py-4" data-test-id="footer-disclaimer">
          <b-col cols="12"
                 sm="9"
                 lg="10"
                 class="small text-black ps-2"
                 data-test-id="footer-copy-text"
                 v-html="translate('disclaimer', 'Footer').replace('[current-year]', new Date().getFullYear())"
          />
        </b-row>
      </b-container>
    </b-container>
  </footer>
</template>

<script>
import { useLabels } from '@/composables/useLabels';

export default {
  conceptName: ['Footer'],
  setup() {
    return useLabels()
  },
  data() {
    return {
      contactInfo: []
    }
  }
}
</script>

<style lang="scss" scoped>
@import '../../scss/colors';
@import '../../scss/z-index';

footer {
  z-index: $zindex-fixed;

  li > a {
  color: $gray-600;

  &:hover {
    color: $white;
    }
  }

  .logo {
    max-height: 1.5rem;
  }
}
</style>
