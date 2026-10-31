// https://nuxt.com/docs/api/configuration/nuxt-config
const isLocal = process.env.MGNL_HOST.includes('localhost')

export default defineNuxtConfig({
    runtimeConfig: {
        public: {
            isLocal,
            MGNL_HOST: process.env.MGNL_HOST,
            MGNL_API_PAGES: process.env.MGNL_API_PAGES,
            MGNL_API_TEMPLATES: process.env.MGNL_API_TEMPLATES,
            NUXT_APP_MGNL_SITE_PATH: process.env.NUXT_APP_MGNL_SITE_PATH,
            MGNL_API_NAVIGATION: process.env.MGNL_API_NAVIGATION,
            NUXT_APP_MGNL_LANGUAGES: process.env.NUXT_APP_MGNL_LANGUAGES,
            DEALERNET_API_HOST: process.env.DEALERNET_API_HOST,
            DEALERNET_API_PAGES: process.env.DEALERNET_API_PAGES,
            CLOUDINARY_BASEURL: `https://res.cloudinary.com/${process.env.CLOUDINARY_SPACE}/image/upload/`
        }
    },

    css: ['~/assets/sass/main.scss'],

    modules: [
        '@pinia/nuxt',
        '@pinia-plugin-persistedstate/nuxt'
    ],

    // Transpiling libraries: https://v3.nuxtjs.org/guide/going-further/esm#transpiling-libraries
    build: {
        transpile: ['@magnolia/vue-editor', '@magnolia/template-annotations'],
    },

    alias: {
        '@magnolia/vue-editor': '@magnolia/vue-editor/src/main.js',
        '@magnolia/template-annotations': '@magnolia/template-annotations/src/index.js',
    },

    imports: {
        dirs: ['./stores'],
    },

    pinia: {
        autoImports: ['defineStore', 'acceptHMRUpdate', 'storeToRefs'],
    },

    // nitro: {
    //     preset: 'aws-lambda'
    // },

    ssr: false
})
