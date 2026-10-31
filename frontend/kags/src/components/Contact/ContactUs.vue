<template>
  <section class="mb-5 position-relative flex-grow-1 flex-shrink-0">
    <div class="banner-main no-navigation" :style="'background-image: url(' + translatedConfig('bannerUrl') + ')'" />

    <b-container class="pt-2">
      <b-row>
        <b-col cols="12" class="pt-4">
          <h1 class="py-4 mt-4 mb-5 mb-lg-4 text-white d-flex align-items-center justify-content-center flex-column flex-sm-row"
              data-test-id="component-title"
          >
            {{ translate("componentTitle", 'ContactUs') }}
          </h1>
        </b-col>
      </b-row>

      <b-row>
        <b-col cols="12" class="mt-md-1 mt-lg-4 pt-lg-1">
          <BasicBreadcrumb />
        </b-col>
      </b-row>

      <div v-if="loading" class="d-flex w-100 align-items-center justify-content-center py-5 my-5">
        <b-spinner variant="primary" class="mb-5" />
      </div>

      <template v-else>
        <b-row>
          <b-col cols="12" class="mb-3">
            <h2>{{ translate("headerTitle", 'ContactUs') }}</h2>
            <div class="font-condensed" v-html="translate('headerSubtitle', 'ContactUs')" />
          </b-col>
        </b-row>

        <b-row align-content="stretch">
          <b-col cols="12" md="4" class="mb-3">
            <div class="box mb-3 h-100 d-flex flex-column">
              <div class="flex-grow-1">
                <h2>{{ translate("serviceSupportTitle", 'ContactUs') }}</h2>
                <p>{{ translate("serviceSupportSubtitle", 'ContactUs') }}</p>
              </div>
              <div>
                <a :href="'tel:'+extractPhone(translate('serviceSupportPhoneNumber', 'ContactUs'))"
                   class="font-condensed text-black mb-3 d-block"
                >
                  <b-icon icon="phone-alt" class="me-2" />
                  {{ translate("serviceSupportPhoneNumber", 'ContactUs') }}
                </a>
                <p class="font-condensed mb-3">
                  <b-icon icon="clock" class="me-2" />
                  {{ translate("serviceSupportBusinessHours", 'ContactUs') }}
                </p>
              </div>
            </div>
          </b-col>

          <b-col cols="12" md="4" class="mb-3">
            <div class="box mb-3 h-100 d-flex flex-column">
              <div class="flex-grow-1">
                <h2>{{ translate("partsSupportTitle", 'ContactUs') }}</h2>
                <p>{{ translate("partsSupportSubtitle", 'ContactUs') }}</p>
              </div>
              <div>
                <a :href="'tel:'+extractPhone(translate('partsSupportPhoneNumber', 'ContactUs'))"
                   class="font-condensed text-black mb-3 d-block"
                >
                  <b-icon icon="phone-alt" class="me-2" />
                  {{ translate("partsSupportPhoneNumber", 'ContactUs') }}
                </a>
                <p class="font-condensed mb-3">
                  <b-icon icon="clock" class="me-2" />
                  {{ translate("partsSupportBusinessHours", 'ContactUs') }}
                </p>
                <a :href="'mailto:'+translate('partsSupportEmail', 'ContactUs')"
                   class="font-condensed text-black mb-3 d-block"
                >
                  <b-icon icon="paper-plane" class="me-2" />
                  {{ translate("partsSupportEmail", 'ContactUs') }}
                </a>
              </div>
            </div>
          </b-col>

          <b-col cols="12" md="4" class="mb-3">
            <div class="box mb-3 h-100 d-flex flex-column">
              <div class="flex-grow-1">
                <h2>{{ translate("ITSupportTitle", 'ContactUs') }}</h2>
                <p>{{ translate("ITSupportSubtitle", 'ContactUs') }}</p>
              </div>
              <div>
                <a :href="'tel:'+extractPhone(translate('ITSupportPhoneNumber', 'ContactUs'))"
                   class="font-condensed text-black mb-3 d-block"
                >
                  <b-icon icon="phone-alt" class="me-2" />
                  {{ translate("ITSupportPhoneNumber", 'ContactUs') }}
                </a>
                <b-button variant="primary"
                       target="_blank"
                       class="mb-3"
                       :href="componentConfig.chatUrl"
                >
                  <b-icon icon="comments" class="me-2" />
                  {{ translate("ITSupportButton", 'ContactUs') }}
                </b-button>
              </div>
            </div>
          </b-col>
        </b-row>
      </template>
    </b-container>
  </section>
</template>

<script>
import { useLabels } from '@/composables/useLabels';
import BasicBreadcrumb from '@/components/common/BasicBreadcrumb';

export default {
  conceptName: ['ContactUs'],
  setup() { return useLabels() },
  components: {
    BasicBreadcrumb
  },
  data() {
    return {
      componentConfig: this.config
    }
  },
  computed: {
    loading() {
      return !Object.keys(this.labels).length
    }
  },
  methods: {
    extractPhone(tel) {
      return tel ? tel.replace('-', '').replace('(', '').replace(')', '').replace(' ', '') : null
    }
  }
}
</script>
