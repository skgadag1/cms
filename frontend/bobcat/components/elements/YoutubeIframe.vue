<template>
  <div v-if="hasOneTrust"
       :data-src="src"
       data-test-id="youtube-iframe"
       class="position-relative h-100 w-100 d-flex align-items-center"
  >
    <div class="yt-iframe-warning position-absolute text-center w-100">
      <ClientOnly>
        <div class="px-3 mb-3">
          <WarningAlert>
            {{ labels.cookieBlockedContentMessage || 'This content requires Targeting Cookies to be enabled.' }}
          </WarningAlert>
        </div>
        <button class="btn btn-outline-primary d-inline-block"
                @click.stop="updateConsent"
        >
          {{ labels.cookieConsentButton || 'Enable Targeting Cookies' }}
        </button>
      </ClientOnly>
      <script type="text/plain" class="optanon-category-C0004">
        const els = document.getElementsByClassName('yt-iframe-warning');
        Array.from(els).forEach(function(el) {
        el.style.display = 'none';
        });
      </script>
    </div>
    <iframe
      class="w-100 optanon-category-C0004"
      :class="{'h-100':fullHeight}"
      :data-src="src"
      :height="height"
      frameborder="0"
      allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
      allowfullscreen
      modestbranding="1"
      rel="0"
      loading="lazy"
    />
  </div>
  <iframe
    v-else
    class="w-100"
    :class="{'h-100':fullHeight}"
    :src="src"
    :height="height"
    frameborder="0"
    allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
    allowfullscreen
    modestbranding="1"
    rel="0"
    loading="lazy"
  />
</template>

<script>
import {onBeforeMount, onMounted, ref, useContext, useFetch} from '@nuxtjs/composition-api'

export default {
  props: {
    src: {
      type: String,
      required: true
    },
    height: {
      type: Number
    },
    fullHeight: {
      type: Boolean
    }
  },
  setup() {
    const {$services} = useContext()
    const labels = ref({})
    const hasOneTrust = ref(true)

    if (process.client) {
      onBeforeMount(() => {
        const groupsString = window?.OnetrustActiveGroups
        if (groupsString && groupsString.includes('C0004')) { hasOneTrust.value = false } // group consent, don't need iframe-warning
      })
      onMounted(() => {
        if (hasOneTrust.value) { hasOneTrust.value = typeof window?.OptanonWrapper !== 'undefined' }
      })
    }

    useFetch(async () => {
      labels.value = await $services.bobcatapi.getConcepts(['/common/labels', '/%region%/common/labels'])
    })

    const updateConsent = () => {
      window?.OneTrust?.UpdateConsent('Category', 'C0004:1')
    }

    return {
      labels,
      hasOneTrust,
      updateConsent
    }
  }
}
</script>
