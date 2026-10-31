<template>
  <section class="container mb-4">
    <div class="box">
      <div class="row">
        <div class="col-12 mb-2">
          <h2>{{ componentConfig.title }}</h2>
          <div class="my-3" v-html="componentConfig.content" />
          <b-notification v-if="componentConfig.notice" class="border-gray-400">
            {{ componentConfig.notice }}
          </b-notification>
        </div>
        <div class="col-12 col-sm-auto order-1 order-sm-0 mb-3">
          <b-button variant="outline-primary"
                 size="lg"
                 class="font-condensed component-button"
                 data-test-id="retrieve-password-button"
                 @click="openModal('retrievePasswordModal')"
          >
            {{ translate("retrievePasswordButton", 'SoftwareRegistration') }}
          </b-button>
        </div><div class="col-12 col-sm-6 order-0 mb-3">
          <b-button variant="primary"
                 size="lg"
                 class="font-condensed component-button"
                 data-test-id="register-software-button"
                 @click="openModal('registrationModal')"
          >
            {{ translate("registerSoftwareButton", 'SoftwareRegistration') }}
          </b-button>
        </div>
        <div class="col-12 order-2" v-html="componentConfig.footer" />
      </div>
      <RegistrationModal ref="registrationModal" :footer="componentConfig.footer" :software="componentConfig.software" />
      <RetrievePasswordModal ref="retrievePasswordModal" :software="componentConfig.software" />
    </div>
  </section>
</template>

<script>
import { useLabels } from '@/composables/useLabels';
import BNotification from '@/components/common/BNotification';
import RegistrationModal from '@/components/SoftwareRegistration/RegistrationModal';
import RetrievePasswordModal from '@/components/SoftwareRegistration/RetrievePasswordModal';

export default {
  conceptName: ['CommonErrors', 'SoftwareRegistration'],
  setup() { return useLabels() },
  components: {BNotification, RegistrationModal, RetrievePasswordModal},
  data() {
    return {
      componentConfig: this.config,
      registrationModalShow: false,
      retrievePasswordModalShow: false
    }
  },
  created() {
    this.$store.dispatch('fetchConcepts', 'en')
  },
  methods: {
    openModal(ref) {
      this.$refs[ref].openModal()
    }
  }
}
</script>
<style lang="scss" scoped>
@import '../../scss/grid';

.component-button {
  width: 100%;
  @include media-breakpoint-up('sm') {
    width: auto;
  }
}
</style>
