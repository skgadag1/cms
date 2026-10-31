<template>
  <Nuxt :key="$route.fullPath" />
</template>

<script>
export default {
  head() {
    return process.server
      ? {
        link: [
          // korean fonts preloaded
          ...(this?.$regions?.current?.name === 'kr'
            ? [
              {rel: 'preload', as: 'font', type: 'font/otf', crossorigin: 'anonymous', href: `${require('~/assets/fonts/noto-sans/NotoSansKR-Regular.otf')}`, fetchpriority: 'highest', once: true},
              {rel: 'preload', as: 'font', type: 'font/otf', crossorigin: 'anonymous', href: `${require('~/assets/fonts/noto-sans/NotoSansKR-Bold.otf')}`, fetchpriority: 'highest', once: true}
            ]
            : []
          ),
          // chinese fonts preloaded
          ...(this?.$regions?.current?.name === 'cn'
            ? [
              {rel: 'preload', as: 'font', type: 'font/otf', crossorigin: 'anonymous', href: `${require('~/assets/fonts/noto-sans/NotoSansSC-Regular.otf')}`, fetchpriority: 'highest', once: true},
              {rel: 'preload', as: 'font', type: 'font/otf', crossorigin: 'anonymous', href: `${require('~/assets/fonts/noto-sans/NotoSansSC-Bold.otf')}`, fetchpriority: 'highest', once: true}
            ]
            : []
          ),
          // default fonts preloaded
          ...(!['cn', 'kr'].includes(this?.$regions?.current?.name?.toLowerCase())
            ? [
              {rel: 'preload', as: 'font', type: 'font/woff2', crossorigin: 'anonymous', href: `${require('~/assets/fonts/helvetica/HelveticaNeue.woff2')}`, fetchpriority: 'highest', once: true},
              {rel: 'preload', as: 'font', type: 'font/woff2', crossorigin: 'anonymous', href: `${require('~/assets/fonts/helvetica/HelveticaNeue-CondensedBold.woff2')}`, fetchpriority: 'highest', once: true}
            ]
            : []
          ),
          {rel: 'preload', as: 'font', type: 'font/woff2', crossorigin: 'anonymous', href: `${require('~/external/bobcat-icons-plugin/public/BobcatIcons.woff2')}`, fetchpriority: 'high', once: true}
        ]
      }
      : {}
  }
}
</script>
