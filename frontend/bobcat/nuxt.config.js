/*
 ATTENTION!
 this config will be copied to /aws and used by lambda SRR function.
 will overwrite /aws/nuxt.config.js (which is used for local development)
 */
/* eslint-disable nuxt/no-cjs-in-config */
const path = require('path')
const useCache = require('./helpers/composables/useCache')
const hasMocks = process.env.MOCK_MAGNOLIA > 0
const isLocal = process.env.IS_LOCAL === true || process.env.IS_LOCAL === 'true'
const {cacheLRU} = useCache()

const enableIndexing = process?.env?.ENABLE_INDEXING === 'true' // NGB-3375

const ssrHtmlCacheTtl = parseInt(process.env.SSR_HTML_CACHE_TTL || '600', 10)
const ssrHtmlStaleIfErrorTtl = parseInt(process.env.SSR_HTML_STALE_IF_ERROR_TTL || '86400', 10)

const _isBuild = process.argv.includes('build') || process.argv.includes('generate')
const _useNative = process.argv.includes('--native')
const _isAuthor = process.argv.includes('--author') || process.env.IS_AUTHOR === 'true'
const _isSsr = (process.argv.includes('--ssr') || (!process.argv.includes('--target=static') && !_useNative)) && !_isAuthor

const _useLegacyI18nStructure = process.env.USE_LEGACY_I18N_STRUCTURE !== 'false'

const staticClientPath = (() => {
  if (process.env.NODE_ENV === 'production') {
    return _isAuthor ? '/bobcat_nuxt/' : process.env.CLOUDFRONT_STATIC?.replace('%INSTANCE%', 'public')
  }
  return '/_nuxt/'
})()

const escapedStaticPath = (staticClientPath || '').replace(/[/\-\\^$*+?.()|[\]{}]/g, '\\$&')
const autoblockRegex = new RegExp(`(<script( [^>]*?)? src=\\"${escapedStaticPath})(.*?)(><\\/script>)`, 'g')

const beastiesOptions = _isSsr
  ? {
    path: path.resolve(__dirname, '.nuxt/dist/client'),
    publicPath: staticClientPath || '/_nuxt/',
    preload: 'swap',
    pruneSource: false,
    reduceInlineStyles: false,
    inlineFonts: true,
    preloadFonts: false,
    noscriptFallback: true,
    logLevel: 'warn',
    allowRules: [/^\.bobcat-icon/]
  }
  : null

const getBeasties = async () => {
  const {default: Beasties} = await import('beasties')
  return new Beasties(beastiesOptions)
}

module.exports = {
  // Global page headers: https://go.nuxtjs.dev/config-head
  vue: {
    compilerOptions: {
      isCustomElement: tag => tag.startsWith('atomic-')
    }
  },
  head: {
    htmlAttrs: {
      lang: process.env.NUXT_DEFAULT_LANGUAGE
    },
    meta: [
      {charset: 'utf-8'},
      {name: 'viewport', content: 'width=device-width, initial-scale=1'},
      {name: 'application-name', content: '&nbsp;'},
      {name: 'msapplication-TileColor', content: '#FFFFFF'},
      {name: 'msapplication-TileImage', content: 'mstile-144x144'},
      {name: 'msapplication-square70x70logo', content: 'mstile-70x70'},
      {name: 'msapplication-square150x150logo', content: 'mstile-150x150'},
      {name: 'msapplication-wide310x150logo', content: 'mstile-310x150'},
      {name: 'msapplication-square310x310logo', content: 'mstile-310x310'},
      !enableIndexing && {
        name: 'robots',
        content: 'noindex,nofollow,noarchive,nositelinkssearchbox,nosnippet,noimageindex',
        hid: 'robots'
      }
    ].filter(x => x),
    link: [
      {rel: 'apple-touch-icon-precomposed', sizes: '57x57', href: '/favicons/ios/apple-touch-icon-57x57.ico'},
      {rel: 'apple-touch-icon-precomposed', sizes: '114x114', href: '/favicons/ios/apple-touch-icon-114x114.ico'},
      {rel: 'apple-touch-icon-precomposed', sizes: '72x72', href: '/favicons/ios/apple-touch-icon-72x72.ico'},
      {rel: 'apple-touch-icon-precomposed', sizes: '144x144', href: '/favicons/ios/apple-touch-icon-144x144.ico'},
      {rel: 'apple-touch-icon-precomposed', sizes: '60x60', href: '/favicons/ios/apple-touch-icon-60x60.ico'},
      {rel: 'apple-touch-icon-precomposed', sizes: '120x120', href: '/favicons/ios/apple-touch-icon-120x120.ico'},
      {rel: 'apple-touch-icon-precomposed', sizes: '76x76', href: '/favicons/ios/apple-touch-icon-76x76.ico'},
      {rel: 'apple-touch-icon-precomposed', sizes: '152x152', href: '/favicons/ios/apple-touch-icon-152x152.ico'},
      {rel: 'apple-touch-icon-precomposed', sizes: '167x167', href: '/favicons/ios/apple-touch-icon-167x167.ico'},
      {rel: 'apple-touch-icon-precomposed', sizes: '180x180', href: '/favicons/ios/apple-touch-icon-180x180.ico'},
      {rel: 'icon', sizes: '196x196', href: '/favicons/web/favicon-196x196.ico'},
      {rel: 'icon', sizes: '96x96', href: '/favicons/web/favicon-96x96.ico'},
      {rel: 'icon', sizes: '32x32', href: '/favicons/web/favicon-32x32.ico'},
      {rel: 'icon', sizes: '16x16', href: '/favicons/web/favicon-16x16.ico'},
      {rel: 'icon', sizes: '128x128', href: '/favicons/web/favicon-128x128.ico'},
      {rel: 'icon', sizes: '48x48', href: '/favicons/web/favicon-48x48.ico'},
      {rel: 'icon', sizes: '240x240', href: '/favicons/web/favicon-240x240.ico'}
    ]
  },

  render: {
    asyncScripts: true,
    http2: {
      push: true
    },
    static: {
      setHeaders(res, path) {
        const faviconRegex = /.*\/static\/favicons\/(.*)$/
        if (faviconRegex.test(path)) {
          res.setHeader('Cache-Control', 'public, max-age=31536000, immutable')
        }
      }
    },
    resourceHints: true,
    prefetchLinks: false,
    ...(_isSsr
      ? {
        bundleRenderer: {
          cache: cacheLRU
        },
        compressor: {
          threshold: 9
        }
      }
      : {})
  },

  telemetry: false,

  loading: false,

  // Global CSS: https://go.nuxtjs.dev/config-css
  css: [
    '~/assets/sass/custom'
  ],

  // Plugins to run before rendering page: https://go.nuxtjs.dev/config-plugins
  plugins: [
    ...(_isSsr
      ? [
        {src: '~/plugins/axiosCache.js'}
      ]
      : []),
    {src: '~/plugins/autoBlockCompatibility.js', mode: 'client'},
    '~/plugins/crawlerDetect.js',
    '~/plugins/jsonld.js',
    '~/plugins/lazyHydrationPlugin',
    '~/plugins/utils',
    '~/plugins/api.js',
    '~/plugins/router.js',
    '~/plugins/icons.js',
    {src: '~/plugins/gtm.js', mode: 'client'},
    {src: '~/plugins/ga4', mode: 'client'},
    {src: '~/plugins/directives.js', mode: 'client'},
    {src: '~/plugins/anchor-scroll.client.js', mode: 'client'}
  ],

  // Auto import components: https://go.nuxtjs.dev/config-components
  components: {
    loader: true,
    dirs: [
      {path: '~/helpers', extensions: ['vue']},
      '~/components',
      '~/components/Compare',
      '~/components/Specifications',
      '~/components/elements',
      '~/components/elements/Form',
      '~/components/elements/Facets',
      '~/components/elements/Compare',
      '~/components/elements/Features',
      '~/components/elements/Attachments',
      '~/components/elements/Navigation'
    ]
  },

  // Modules for dev and build (recommended): https://go.nuxtjs.dev/config-modules
  buildModules: [
    // https://go.nuxtjs.dev/eslint
    '@nuxtjs/eslint-module',
    '@nuxtjs/router',
    '@nuxtjs/composition-api/module',
    'bootstrap-vue/nuxt',
    '@nuxtjs/cloudinary',
    '@nuxtjs/gtm'
  ],

  // Modules: https://go.nuxtjs.dev/config-modules
  modules: [
    ...(_isSsr && !_useNative ? ['nuxt-speedkit'] : []),
    // https://go.nuxtjs.dev/axios
    '@nuxtjs/axios',
    '~/modules/cache',
    '@nuxtjs/cloudinary',
    ['~/modules/errorPageConfig', {filePath: 'static/errorPageConfig.json'}],
    // https://go.nuxtjs.dev/bootstrap
    'bootstrap-vue/nuxt',
    '~/modules/regions',
    '~/modules/gtmConfig',
    'cookie-universal-nuxt'
  ].filter(x => x),

  bootstrapVue: {
    bootstrapCSS: false,
    icons: false,
    componentPlugins: [
      'LayoutPlugin',
      'FormPlugin',
      'NavbarPlugin',
      'CardPlugin',
      'ModalPlugin'
    ],
    directives: [
      'v-b-tooltip',
      'v-b-modal'
    ],
    components: [
      'BBadge',
      'BListGroup',
      'BListGroupItem',
      'BAvatar',
      'BEmbed',
      'BTable',
      'BImg',
      'BImgLazy',
      'BOverlay',
      'BButton',
      'BSpinner',
      'BButtonGroup',
      'BInputGroup',
      'BInputGroupText',
      'BFormSelect',
      'BFormSelectOption',
      'BFormInput',
      'BFormGroup',
      'BFormRadioGroup',
      'BFormRadio',
      'BFormCheckbox',
      'BFormCheckboxGroup',
      'BFormTextarea',
      'BSkeleton',
      'BSkeletonImg',
      'BCarousel',
      'BCarouselSlide',
      'BModal'
    ]
  },

  router: {
    middleware: ['trailingRedirect', 'regions', 'breakpoints']
  },

  // Axios module configuration: https://go.nuxtjs.dev/config-axios
  axios: {
    // retry: { retries: 1 },
    // debug: process.env.NODE_ENV !== 'production'
  },

  // Build Configuration: https://go.nuxtjs.dev/config-build
  build: {
    transpile: ['axios', '@coveo/atomic'],
    corejs: 3,
    processConditionalComments: false,
    publicPath: staticClientPath,
    babel: {
      plugins: [['@babel/plugin-proposal-private-methods', {loose: true}]]
    },
    optimizeCSS: true,
    extractCSS: {ignoreOrder: true},
    postcss: {
      postcssOptions: {
        plugins: {
          ...(_isBuild && process.env.NODE_ENV === 'production'
            ? {'@fullhuman/postcss-purgecss': require('./purgecss.config')}
            : {})
        }
      }
    },
    optimization: {
      runtimeChunk: 'single',
      moduleIds: 'total-size', // makes module ids more stable between build (for cache)
      splitChunks: {
        chunks: 'all',
        maxInitialRequests: Infinity,
        minSize: 10000,
        cacheGroups: {
          coveoHeadless: {
            priority: 20,
            reuseExistingChunk: true,
            test: /[\\/]node_modules[\\/]@coveo[\\/]headless[\\/]/,
            name: 'npm.coveo-headless'
          },
          coveoAtomicShared: {
            priority: 21,
            reuseExistingChunk: true,
            test: (module) => {
              const resource = module.resource || ''
              return /[\\/]node_modules[\\/]@coveo[\\/]atomic[\\/]/.test(resource) && !resource.endsWith('.entry.js')
            },
            name(module) {
              const file = module.resource.split(/[\\/]/).pop().replace(/\.m?js$/, '')
              return `npm.coveo-atomic.${file}`
            }
          },
          vendor: {
            test: module => /[\\/]node_modules[\\/]/.test(module.context || '') &&
              !/[\\/]node_modules[\\/]@coveo[\\/]atomic[\\/]/.test(module.context || ''),
            name(module) {
              const packageName = module.context.match(/[\\/]node_modules[\\/](.*?)([\\/]|$)/)[1]
              return `npm.${packageName?.replace('@', '')}`
            }
          },
          bicons: {
            priority: 10,
            reuseExistingChunk: false,
            test: /[\\/]node_modules[\\/]bootstrap-vue[\\/]src[\\/]icons[\\/]/,
            name: 'b-icons'
          },
          bootstrapvue: {
            priority: -10,
            reuseExistingChunk: false,
            test: /[\\/]node_modules[\\/]bootstrap-vue[\\/]/,
            name: 'bootstrap-vue'
          },
          forms: {
            priority: 10,
            reuseExistingChunk: false,
            test: /[\\/](node_modules[\\/]bootstrap-vue[\\/]src[\\/](components[\\/]form|mixins[\\/]form)(.*))|(components[\\/]elements[\\/]Form)|(components[\\/]Form)[\\/]/,
            name: 'b-forms'
          },
          btable: {
            priority: 11,
            reuseExistingChunk: false,
            test: /[\\/](node_modules[\\/]bootstrap-vue[\\/]src[\\/]components[\\/]table)[\\/]/,
            name: 'b-table'
          },
          bcarousel: {
            priority: 11,
            reuseExistingChunk: false,
            test: /[\\/](node_modules[\\/]bootstrap-vue[\\/]src[\\/]components[\\/]carousel)|(components[\\/]ContentCarousel)[\\/]/,
            name: 'b-carousel'
          },
          bcarouselslide: {
            priority: 11,
            reuseExistingChunk: false,
            test: /[\\/](node_modules[\\/]bootstrap-vue[\\/]src[\\/]components[\\/]carousel)|(components[\\/]ContentCarousel)[\\/]/,
            name: 'b-carousel-slide'
          }
        }
      }
    },
    terser: {
      terserOptions: {
        parallel: true,
        cache: true,
        compress: {
          drop_console: _isSsr,
          passes: isLocal ? 1 : 3,
          unsafe: !isLocal
        },
        output: {
          comments: /^\**!|@preserve|@license|@cc_on/
        }
      }
    },
    loaders: {
      sass: {
        sassOptions: {
          importer: () => require('node-sass-magic-importer'),
          quietDeps: true,
          silenceDeprecations: ['slash-div']
        }
      }
    },
    extend(config, {loaders, isModern}) {
      // disable miragejs when not needed
      if (!isLocal && !hasMocks) {
        config.module.rules.push({
          test: path.resolve(__dirname, 'node_modules/miragejs/dist/mirage-esm.js'),
          loader: 'null-loader'
        })
      }

      config.module.rules.push({
        /* eslint-disable-next-line no-useless-escape */
        test: /^(.*\@coveo[\\\/]atomic[\\\/]dist[\\\/]esm[\\\/].*\.m?js)|(.*\@coveo[\\\/]headless[\\\/]dist[\\\/]browser[\\\/].*\.m?js)$/,
        use: {
          loader: 'babel-loader',
          // AV-3209: modern bundle targets esmodules to keep Coveo modern; legacy
          // uses browserslist defaults. The explicit syntax plugins are required
          // regardless of target because webpack 4's parser cannot handle optional
          // chaining / nullish coalescing / class fields left in Coveo's dist code.
          options: {
            presets: [['@babel/preset-env', {targets: isModern ? {esmodules: true} : {}}]],
            plugins: [
              '@babel/plugin-proposal-optional-chaining',
              '@babel/plugin-proposal-nullish-coalescing-operator',
              '@babel/plugin-proposal-class-properties'
            ]
          }
        }
      })
      if (loaders?.fontUrl) {
        loaders.fontUrl.name = name => `fonts/[name]${name.includes('BobcatIcons') ? '.[contenthash:7]' : ''}.[ext]`
      }
    }
  },

  buildDir: process.argv.includes('preview') ? 'dist-preview' : '.nuxt',

  babel: {
    exclude: [
      '~/plugins/cloudinary/cloudinaryGallery.js'
    ]
  },

  cloudinary: {
    cloudName: process.env.CLOUDINARY_CLOUDNAME,
    apiSecret: process.env.CLOUDINARY_SECRET
  },

  env: {
    IS_LOCAL: process.env.IS_LOCAL,
    IS_DEV: process.env.IS_DEV,
    MOCK_MAGNOLIA: process.env.MOCK_MAGNOLIA,
    MAGNOLIA_SERVER: process.env.MAGNOLIA_SERVER,
    NODE_ENV: process.env.NODE_ENV,
    VUE_APP_SITE_BASENAME: process.env.VUE_APP_SITE_BASENAME,
    NUXT_DEFAULT_LANGUAGE: process.env.NUXT_DEFAULT_LANGUAGE,
    NUXT_DEFAULT_REGION: process.env.NUXT_DEFAULT_REGION,
    NUXT_ENV_DEVALUE_LOG_LEVEL: process.env.NUXT_ENV_DEVALUE_LOG_LEVEL,
    CLOUDFRONT_STATIC: process.env.CLOUDFRONT_STATIC,
    ENABLE_INDEXING: process.env.ENABLE_INDEXING,
    CLOUDINARY_DOMAIN_CN: process.env.CLOUDINARY_DOMAIN_CN,
    BUILDQUOTE_API_SERVER: process.env.BUILDQUOTE_API_SERVER
  },

  publicRuntimeConfig: {
    // No undefined values allowed because keys need to be present for override in lambda
    PUBLIC_PATH: process.env.PUBLIC_PATH || '',
    PUBLIC_HOST: process.env.PUBLIC_HOST || 'https://www.bobcat.com',
    IS_AUTHOR: _isAuthor,
    USE_NATIVE_MAGNOLIA: _useNative,
    PLACEHOLDER_IMAGE: process.env.PLACEHOLDER_IMAGE || '',
    CLOUDINARY_CLOUDNAME: process.env.CLOUDINARY_CLOUDNAME || '',
    MAGNOLIA_CONTENT_API_SERVER: process.env.MAGNOLIA_CONTENT_API_SERVER || '',
    MAGNOLIA_TEMPLATE_DEFINITIONS: process.env.MAGNOLIA_TEMPLATE_DEFINITIONS || '',
    CONTENT_API_PAGES: process.env.CONTENT_API_PAGES || '',
    BOBCAT_API_SERVER: process.env.BOBCAT_API_SERVER || '',
    BOBCAT_API_PAGEDETAILS: process.env.BOBCAT_API_PAGEDETAILS || '',
    BOBCAT_API_PRODUCT: process.env.BOBCAT_API_PRODUCT || '',
    BOBCAT_API_PRODUCT_PDF: process.env.BOBCAT_API_PRODUCT_PDF || '',
    BOBCAT_API_PRODUCTS: process.env.BOBCAT_API_PRODUCTS || '',
    INSTANCE_NAME: _isAuthor ? 'author' : 'public',
    BOBCAT_API_TAGS: process.env.BOBCAT_API_TAGS || '',
    BOBCAT_API_CONCEPTS: process.env.BOBCAT_API_CONCEPTS || '',
    BOBCAT_API_TEMPLATE_ANNOTATIONS: process.env.BOBCAT_API_TEMPLATE_ANNOTATIONS || '',
    BOBCAT_API_COMPONENTS: process.env.BOBCAT_API_COMPONENTS || '',
    BOBCAT_API_NAVIGATION: process.env.BOBCAT_API_NAVIGATION || '',
    CONTENT_API_SERVER: process.env.CONTENT_API_SERVER || '',
    CONTENT_API_LOCATION_DETECTED: process.env.CONTENT_API_LOCATION_DETECTED || '',
    CONTENT_API_LOCATION: process.env.CONTENT_API_LOCATION || '',
    BOBCAT_API_KEY: process.env.BOBCAT_API_KEY || '',
    BUILD_API_SERVER: process.env.BUILD_API_SERVER || '',
    BOBCAT_API_REGIONS: process.env.BOBCAT_API_REGIONS || '',
    BOBCAT_API_CONFIG: process.env.BOBCAT_API_CONFIG || '',
    CLOUDINARY_CLOUD_NAME: process.env.CLOUDINARY_CLOUD_NAME || '',
    DEALER_LOCATOR_API: process.env.DEALER_LOCATOR_API || '/dealer-locator',
    BOBCAT_API_LOCATION: process.env.BOBCAT_API_LOCATION || '/location',
    BOBCAT_API_CATEGORIES: process.env.BOBCAT_API_CATEGORIES || '',
    BOBCAT_API_FAULT_CODE: process.env.BOBCAT_API_FAULT_CODE || '',
    BOBCAT_API_SPARROW_IMAGE: process.env.BOBCAT_API_SPARROW_IMAGE || '',
    BOBCAT_API_FORM: process.env.BOBCAT_API_FORM || '',
    BOBCAT_API_FORM_CONFIG: process.env.BOBCAT_API_FORM_CONFIG || '',
    BOBCAT_API_TOKEN: process.env.BOBCAT_API_TOKEN || '',
    BOBCAT_API_UPLOADER: process.env.BOBCAT_API_UPLOADER || '',
    BOBCAT_API_SEARCH: process.env.BOBCAT_API_SEARCH || '',
    BUILD_QUOTE_API_SEARCH_PRODUCT_OPTIONS: process.env.BUILD_QUOTE_API_SEARCH_PRODUCT_OPTIONS || '',
    BOBCAT_API_ATTACHMENTS: process.env.BOBCAT_API_ATTACHMENTS || '',
    PRODUCTS_API: process.env.PRODUCTS_API || '',
    BOBCAT_API_CAMPAIGNS: process.env.BOBCAT_API_CAMPAIGNS || '',
    BOBCAT_API_FACETS: process.env.BOBCAT_API_FACETS || '',
    DEALER_INVENTORY_API: process.env.DEALER_INVENTORY_API || 'dealer-inventory',
    DEALER_INVENTORY_API_V2: process.env.DEALER_INVENTORY_API_V2 || 'v2/dealer-inventory',
    BOBCAT_PAYMENT_GATEWAY: process.env.BOBCAT_PAYMENT_GATEWAY || '',
    BOBCAT_PAYMENT_GATEWAY_INVENTORY: process.env.BOBCAT_PAYMENT_GATEWAY_INVENTORY || 'add-product',
    BOBCAT_API_MARKETING_TAGS: process.env.BOBCAT_API_MARKETING_TAGS || '',
    NUXT_LABELS_DEFAULT_PATH: process.env.NUXT_LABELS_DEFAULT_PATH || '',
    PARAMS_ANCESTOR: process.env.PARAMS_ANCESTOR || '',
    PARAMS_NAME: process.env.PARAMS_NAME || '',
    API_TIMEOUT_MILLIS: process.env.API_TIMEOUT_MILLIS || '',
    CONTENT_API_PUBLIC: process.env.CONTENT_API_PUBLIC || '',
    USE_LEGACY_I18N_STRUCTURE: _useLegacyI18nStructure
  },

  generate: {
    routes: [
      '/'
    ],
    dir: process.env.npm_lifecycle_script && process.env.npm_lifecycle_script.includes('preview') ? 'dist-preview' : 'dist',
    target: 'static',
    interval: 1000
  },

  ssr: _isSsr,
  modern: _isSsr ? 'server' : 'client',

  eslint: {
    overrideConfig: {
      globals: {
        $nuxt: 'readonly'
      }
    },
    fix: true,
    exclude: ['~/plugins/cloudinary/cloudinaryGallery.js']
  },

  gtm: {
    enabled: _isSsr && !_isAuthor,
    debug: process.env.NODE_ENV !== 'production',
    respectDoNotTrack: false
  },

  speedkit: {
    optimizePreloads: false,
    disableNuxtImage: true,
    detection: {
      performance: false,
      browserSupport: false
    }
  },

  hooks: {
    'build:templates': ({templatesFiles}) => {
      const componentsPlugin = templatesFiles.find(t => t.dst === 'components/plugin.js')
      if (componentsPlugin) {
        componentsPlugin.src = path.resolve(__dirname, 'componentsPlugin.template.js')
      }
    },
    render: {
      route: async (_url, result, context) => {
        // JAR-1991: Prevent CookiePro autoblock from blocking our scripts
        if (result && result.html) {
          result.html = result.html.replaceAll(autoblockRegex, '$1$3 data-ot-ignore$4')
        }
        if (beastiesOptions && result?.html) {
          try {
            result.html = await (await getBeasties()).process(result.html)
          } catch (e) {
            console.error('critical CSS inlining failed, serving unprocessed HTML', e)
          }
        }
        if (_isSsr && context.res && context.res.statusCode === 200 && result?.html && !context.res.getHeader('Cache-Control')) {
          context.res.setHeader(
            'Cache-Control',
            `public, max-age=${ssrHtmlCacheTtl}, stale-while-revalidate=${ssrHtmlCacheTtl}, stale-if-error=${ssrHtmlStaleIfErrorTtl}`
          )
        }
      }
    }
  }
}
