<template>
  <aside class="container shocial-share" data-test-id="socialshare">
    <LazyHydrate never>
      <div class="row">
        <div class="col-12 position-relative mb-4"
             :class="{ 'col-sm-8 col-lg-6 offset-sm-2 offset-lg-3' : centeredColumn === 'true' }"
             data-test-id="socialshare-buttons-wrapper"
        >
          <div class="sharethis-inline-share-buttons d-block text-left" data-test-id="socialshare-buttons" />
          <div v-if="showSpacer === 'true'" class="separator w-100 my-4 bg-gray-light" />
        </div>
      </div>
    </LazyHydrate>

    <WarningAlert v-if="inEditorEdit && !(shareThisId && shareThisId.length)" data-test-id="socialshare-error" v-html="errorMsg" />
  </aside>
</template>

<script>
import {ref, useContext, useFetch, onMounted} from '@nuxtjs/composition-api'
import {inEditorEdit} from '~/helpers/vue-editor'
import useMounted from '~/helpers/composables/useMounted'
const ST_CONFIG_NAME = 'shareThisConfig' // used only here so not extracting this outside this file

export default {
  props: {
    nodeId: {
      type: String
    },
    centeredColumn: {
      type: String,
      default: 'false'
    },
    showSpacer: {
      type: String,
      default: 'false'
    }
  },
  setup(props) {
    const shareThisId = ref(null)
    const errorMsg = 'Unable to get <i>shareThisId</i>. Please check configuration in <i>config/modules/bobcat/commonConfig/shareThisConfig</i> if the proper key:value pair exists for domain.'
    useMounted(props.nodeId)
    const {$services, $regions, req} = useContext()

    const injectShareScript = () => {
      let s = document.querySelector('#sharethisscript')
      if (!s && shareThisId.value) {
        s = document.createElement('script')
        s.setAttribute('id', 'sharethisscript')
        s.setAttribute('src', `https://platform-api.sharethis.com/js/sharethis.js#property=${shareThisId.value}&product=sop`)
        document.body.appendChild(s)
      }
    }

    useFetch(async () => {
      try {
        const currentHost = (process.client && window) ? window.location.host : req.headers.host
        const {data} = await $services.bobcatapi.getConfig(ST_CONFIG_NAME, false)
        shareThisId.value = data[$regions?.current?.name] ? data[$regions.current.name][currentHost.split(':')[0]] || data[currentHost.split(':')[0]] : data[currentHost.split(':')[0]]
        if (process.client) { injectShareScript() }
      } catch (e) {
        // eslint-disable-next-line no-console
        console.error('[SocialShare] Unable to set shareThis id', errorMsg, e)
      }
    })

    onMounted(() => {
      if (shareThisId.value) {
        injectShareScript()
      }
    })

    return {
      shareThisId,
      inEditorEdit,
      errorMsg
    }
  }
}
</script>

<style lang="scss">
@import '~bootstrap/scss/functions';
@import '~/assets/sass/variables';
@import '~/assets/sass/utils';
@import '~bootstrap/scss/mixins';

.sharethis-inline-share-buttons {
  &.st-inline-share-buttons {
    @include media-breakpoint-up('md') {
      .st-btn {
        margin-right: 1px !important;
      }
    }
  }
}

.shocial-share {
  .separator {
    height: .5rem;
  }
}
</style>
