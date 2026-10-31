<template>
    <EditablePage
            v-if="content"
            :content="content"
            :config="config"
            :templateAnnotations="templateAnnotations"
    />
</template>

<script setup>
import { EditablePage, EditorContextHelper } from '@magnolia/vue-editor'
import config from '~/magnolia.config.js'

const inEditorMode = EditorContextHelper.inEditorPreview() || EditorContextHelper.inIframe()

function getCurrentLanguage(url, languages) {
  for (let i = 0; i < languages.length; i++) {
    const language = languages[i]

    if (url.indexOf("/" + language) > -1) return language
  }

  return languages[0]
}

const templateAnnotations = ref({})
const runtimeConfig = useRuntimeConfig()
const fullPath = useRoute().path

// Load paths, see .env and nuxt.config.js files
const nodeName = runtimeConfig.NUXT_APP_MGNL_SITE_PATH
const languages = runtimeConfig.NUXT_APP_MGNL_LANGUAGES?.split(" ")
const pagesApi = runtimeConfig.isLocal ? runtimeConfig.MGNL_API_PAGES : runtimeConfig.DEALERNET_API_PAGES
const templateAnnotationsApi = runtimeConfig.MGNL_API_TEMPLATES

const currentLanguage = getCurrentLanguage(fullPath, languages)
const isDefaultLanguage = currentLanguage === languages[0]
let pagePath = nodeName + fullPath.replace(new RegExp(".*" + nodeName), "")

if (!isDefaultLanguage) {
  pagePath = pagePath.replace("/" + currentLanguage, "")
}

const content = ref(null)

try {
  const data = await
  $fetch(pagesApi.replace('%PATH%', pagePath), {
    params: {
      lang: currentLanguage,
      ...(inEditorMode && {editMode: true}),
      ...usePersonalized().getParams()
    }
  })

  content.value = data
} catch {
  throw createError({
    statusCode: 404,
    message: 'No page found'
  })
}

if (!(content.value)) {
  throw createError({
    statusCode: 404,
    message: 'No page found'
  })
}

if (process.client) {
  onMounted(async () => {
    if (inEditorMode) {
      const templateAnnotationsRes = await fetch(templateAnnotationsApi + pagePath)
      templateAnnotations.value = await templateAnnotationsRes.json()
    }
  })
}
</script>