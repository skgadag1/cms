const vendorStylesheets = [
  './node_modules/vue-phone-number-input/dist/vue-phone-number-input.css',
  './node_modules/lite-youtube-embed/src/lite-yt-embed.css'
]

const htmlElements = [
  'a', 'abbr', 'address', 'area', 'article', 'aside', 'audio', 'b', 'base', 'bdi', 'bdo',
  'blockquote', 'body', 'br', 'button', 'canvas', 'caption', 'cite', 'code', 'col', 'colgroup',
  'data', 'datalist', 'dd', 'del', 'details', 'dfn', 'dialog', 'div', 'dl', 'dt', 'em', 'embed',
  'fieldset', 'figcaption', 'figure', 'footer', 'form', 'h1', 'h2', 'h3', 'h4', 'h5', 'h6',
  'head', 'header', 'hgroup', 'hr', 'html', 'i', 'iframe', 'img', 'input', 'ins', 'kbd', 'label',
  'legend', 'li', 'link', 'main', 'map', 'mark', 'menu', 'meta', 'meter', 'nav', 'noscript',
  'object', 'ol', 'optgroup', 'option', 'output', 'p', 'param', 'picture', 'pre', 'progress',
  'q', 'rp', 'rt', 'ruby', 's', 'samp', 'script', 'search', 'section', 'select', 'slot', 'small',
  'source', 'span', 'strong', 'style', 'sub', 'summary', 'sup', 'svg', 'table', 'tbody', 'td',
  'template', 'textarea', 'tfoot', 'th', 'thead', 'time', 'title', 'tr', 'track', 'u', 'ul',
  'var', 'video', 'wbr'
]

const attributeValues = [
  'checkbox', 'radio', 'button', 'submit', 'reset', 'file', 'range', 'color', 'search',
  'email', 'password', 'number', 'tel', 'url', 'date', 'time', 'datetime-local', 'month',
  'week', 'text', 'hidden', 'image', 'disabled', 'readonly', 'required', 'checked', 'multiple',
  'data-original-title', 'table-responsive-sm', 'table-responsive-md', 'table-responsive-lg',
  'table-responsive-xl', 'table-responsive-xxl'
]

const dynamicallyBuiltClasses = [
  /^alert-/,
  /^evolution-table__cell--/,
  /^height-[0-9]/,
  /^infographic-table-element-content-/,
  /^pointer-event/,
  /^specification-compare-table__(container|header)/,
  /^transparent-stroke$/,
  /^z-index-/,
  /^bg-/,
  /^cards-/,
  /^category-/,
  /^col-lg-/,
  /^col-md-/,
  /^color-/,
  /^evolution-table__color-/,
  /^evolution-table__wrap--/,
  /^gradient-/,
  /^infographic-table-elements-/,
  /^item-/,
  /^opacity-/,
  /^order-/,
  /^overlay-/,
  /^result-/,
  /^specification-compare-table__items-/,
  /^text-/,
  /^transition-delay-/,
  /^bobcat-icon/
]

const vueTransitions = [
  /-(enter|leave)($|-)/,
  /-move$/
]

const bootstrapVueRendered = [
  /^b-/,
  /^bs-(popover|tooltip)/,
  /^container-/,
  /^drop(up|left|right)$/,
  /^focused$/,
  /^has-validation$/,
  /^mark$/,
  /^noninteractive$/,
  /^pre-scrollable$/,
  /^readonly$/,
  /^tab-/,
  /^thead-/,
  /^(top|bottom)-row$/,
  /^bv-/,
  /^alert/,
  /^arrow$/,
  /^badge/,
  /^breadcrumb/,
  /^btn/,
  /^card/,
  /^carousel/,
  /^close$/,
  /^col-form-/,
  /^custom-(control|checkbox|radio|select|switch|file|range)/,
  /^dropdown/,
  /^embed-responsive/,
  /^figure/,
  /^form-/,
  /^img-(fluid|thumbnail)/,
  /^input-group/,
  /^jumbotron/,
  /^list-group/,
  /^media/,
  /^modal/,
  /^nav$/,
  /^nav-/,
  /^navbar/,
  /^page-(item|link)/,
  /^pagination/,
  /^popover/,
  /^progress/,
  /^spinner-/,
  /^sr-only/,
  /^table/,
  /^toast/,
  /^tooltip/
]

const toggledAtRuntime = [
  /^active$/,
  /^collapse$/,
  /^collapsing$/,
  /^disabled$/,
  /^fade$/,
  /^focus$/,
  /^hover$/,
  /^is-(in)?valid$/,
  /^(in)?valid-(feedback|tooltip)$/,
  /^modal-backdrop$/,
  /^modal-open$/,
  /^no-transition$/,
  /^open$/,
  /^selected$/,
  /^show$/,
  /^showing$/,
  /^slide$/,
  /^sliding$/,
  /^was-validated$/
]

const authorTypedUtilities = [
  /^(m|p)(t|r|b|l|x|y)?-((sm|md|lg|xl|xxl)-)?(n?[0-9]+|auto)$/,
  /^col($|-)/,
  /^row$/,
  /^row-cols-/,
  /^lh-/,
  /^user-select-/,
  /^no-gutters$/,
  /^offset-/,
  /^container($|-fluid)/,
  /^d-/,
  /^flex($|-)/,
  /^justify-/,
  /^align-(items|self|content)-/,
  /^align-(baseline|top|middle|bottom|text)/,
  /^font-/,
  /^border/,
  /^rounded/,
  /^shadow/,
  /^w-/,
  /^h-/,
  /^mw-/,
  /^mh-/,
  /^float-/,
  /^position-/,
  /^fixed-/,
  /^sticky-/,
  /^overflow-/,
  /^clearfix$/,
  /^list-(unstyled|inline)/,
  /^visible$/,
  /^invisible$/,
  /^stretched-link$/,
  /^text-truncate$/,
  /^lead$/,
  /^display-[1-4]$/,
  /^blockquote/,
  /^h[1-6]$/,
  /^small$/,
  /^initialism$/
]

const cmsAuthoredClasses = [
  /^search-attachments$/
]

const thirdPartyRuntimeMarkup = [
  /^atomic-/,
  /^cludo/,
  /^coveo/,
  /^country-selector/,
  /^input-tel/,
  /^iti-/,
  /^lite-youtube/,
  /^lyt-/,
  /^optanon/,
  /^onetrust/,
  /^ot-/,
  /^pac-/,
  /^vue-slider/
]

module.exports = {
  content: [
    './components/**/*.vue',
    './helpers/**/*.vue',
    './layouts/**/*.vue',
    './static-pages/*.html',
    './constants/**/*.js',
    './utils/**/*.js',
    './store/**/*.js',
    './modules/**/*.js',
    './plugins/**/*.js',
    './middleware/**/*.js',
    ...vendorStylesheets,
    {raw: attributeValues.join(' '), extension: 'html'}
  ],
  safelist: {
    standard: htmlElements,
    greedy: [
      ...dynamicallyBuiltClasses,
      ...vueTransitions,
      ...bootstrapVueRendered,
      ...toggledAtRuntime,
      ...authorTypedUtilities,
      ...cmsAuthoredClasses,
      ...thirdPartyRuntimeMarkup
    ]
  }
}
