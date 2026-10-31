import http from '@/utils'
import api from '@/api/apiUrls'
import qs from 'qs'
import {contentService} from '@/services'
import OverlayWrapper from '@/components/common/OverlayWrapper'
import langs from 'langs'

export const getNavigatorLanguage = () => (navigator.languages && navigator.languages.length) ? navigator.languages[0] : navigator.userLanguage || navigator.language || navigator.browserLanguage || 'en-US'
const getUserTimeZone = () => Intl.DateTimeFormat().resolvedOptions().timeZone

const _testMixin = function (Vue) {
  // bootstrap-vue-next is registered per app in each main.ts (registerBootstrapVueNext)
  Vue.component('overlay-wrapper', OverlayWrapper)

  Vue.config.ignoredElements = ['bicon', 'b-icon']
  Vue.prototype.$http = http
  Vue.prototype.$api = api

  Vue.filter('trunc', (str, len = 55, clamp = '...') => {
    return str && str.length ? str.slice(0, len) + (len < str.length ? clamp : '') : str
  })

  Vue.filter('formatDate', val => {
    const ts = new Date(val)
    const options = {day: 'numeric', month: 'short', year: 'numeric'}
    return ts.toLocaleDateString(getNavigatorLanguage(), options)
  })

  Vue.filter('formatTime', val => {
    const ts = new Date(val)
    const options = {hour: 'numeric', minute: 'numeric'}
    return ts.toLocaleTimeString(getNavigatorLanguage(), options) + ' ' + getUserTimeZone()
  })

  Vue.filter('replace', (value, keyword, replacement) => {
    return value.replace(keyword, replacement)
  })

  Vue.filter('capitalize', function (value) {
    if (!value) return ''
    value = value.toString()
    return value.charAt(0).toUpperCase() + value.slice(1)
  })

  // languageMixin
  Vue.mixin({
    filters: {
      translate(value, labels, key = null) {
        if ((labels && Object.keys(labels).length === 0) || typeof labels === 'undefined') return ''
        if (!key) {
          if (labels[value]) {
            return labels[value]
          } else if (labels.config && labels.config[value]) {
            return labels.config[value]
          } else {
            return `{ config.${value} }`
          }
        }
        return labels[key] && labels[key][value] ? labels[key][value] : `{ ${key}.${value} }`
      }
    },
    data() {
      return {
        componentLabels: {}
      }
    },
    computed: {
      labels() {
        if (this.$store && typeof this.$store.state.showConceptNames !== 'undefined' && this.$store.state.showConceptNames) return {'show': 'concepts'}
        return this.componentLabels
      },
      translatedLabel() {
        return (conceptKey, conceptPath) => this.$options.filters.translate(conceptKey, this.labels, conceptPath)
      },
      translatedConfig() {
        return (conceptKey) => this.$options.filters.translate(conceptKey, this.labels, 'config')
      }
    },
    created() {
      const componentConceptNames = this.$options.conceptName

      if (Array.isArray(componentConceptNames)) {
        componentConceptNames.forEach(cn => {
          this.getConceptNameValues(cn, true)
        })
      } else if (componentConceptNames) {
        this.getConceptNameValues(componentConceptNames)
      } else {
        this.loadLabelsFromConfig()
      }
    },
    methods: {
      updateUrlParam(keyword, value) {
        if (window.history && history.replaceState) {
          let _newAddr = location.origin + location.pathname

          if (location.href.indexOf('?') > 0) {
            let items = qs.parse(location.search.substr(1))
            if (value) items[keyword] = value; else delete items[keyword]
            if (Object.entries(items).length > 0) _newAddr += '?' + qs.stringify(items)
          } else if (value !== '') _newAddr += '?' + keyword + '=' + value

          history.replaceState('', '', _newAddr)
        }
      },
      loadLabelsFromConfig() {
        const appConfig = this.$.appContext.config.globalProperties.config
        if (appConfig) this.componentLabels = {...this.componentLabels, config: appConfig}
      },
      getConceptNameValues(cn, append = false) {
        let _l = {}

        contentService.fetchTranslations('components/' + cn).then(r => {
          if (r.data && r.data.length) {
            _l = r.data.reduce((accum, currentVal) => {
              accum[currentVal.key] = currentVal.value
              return accum
            }, {})
          } else {
            _l = {error: 'No labels for the given concept: ' + cn}
          }
          _l.conceptName = cn
        }).catch(() => {
          _l = {
            error: 'Error while fetching translations',
            conceptName: cn
          }
        }).finally(() => {
          if (append) this.componentLabels[cn] = _l
          else this.componentLabels = _l
          this.loadLabelsFromConfig()
        })
      },
      getSerialErrorMessage(error, sn) {
        let message = this.$options.filters.translate('defaultError', this.labels, 'CommonErrors')
        if (error.error_code) switch (error.error_code) {
        case '400':
          message = this.$options.filters.translate('wrongSerialNumberFormat', this.labels, 'AddSerialControl')
          break
        case '403':
          message = this.$options.filters.translate('notAuthorisedSN', this.labels, 'CommonErrors').replace('_sn_', sn)
          if (/stolen/.test(error.error_message)) message = this.$options.filters.translate('stolenMachine', this.labels, 'AddSerialControl')
          if (/organization is invalid/.test(error.error_message)) message = this.$options.filters.translate('invalidOrganization', this.labels, 'AddSerialControl')
          break
        case '404':
          message = (/^Organization with id\s\w+\snot found.$/.test(error.error_message))
            ? this.$options.filters.translate('notAuthorisedSN', this.labels, 'CommonErrors').replace('_sn_', sn)
            : this.$options.filters.translate('noSerialFound', this.labels, 'CommonErrors')
          break
        }
        return message
      },
      translateLocale(facet) {
        if (!facet) return ''
        return langs.where('1', facet.substr(0, 2)) ? (langs.where('1', facet.substr(0, 2)).local + ' (' + facet.substr(2, 2) + ')') : facet
      }
    }
  })

  // Vue 3: there is no vnode.context / binding.expression any more -- the bound handler itself
  // arrives as binding.value (v-click-outside="hideDropdown" -> the hideDropdown method).
  Vue.directive('click-outside', {
    beforeMount(el, binding) {
      el.clickOutsideEvent = (event) => {
        // composedPath() is captured at dispatch time, so a click on something the date picker
        // re-renders (and detaches) during the same click still counts as inside.
        const path = event.composedPath ? event.composedPath() : []
        const _isOutsideEvent = !(el === event.target || el.contains(event.target) || path.includes(el))
        const _isNotFromDatePicker = !path.some(node => node.classList && node.classList.contains('dp__menu'))

        if (_isOutsideEvent && _isNotFromDatePicker && typeof binding.value === 'function') binding.value(event)
      }
      document.body.addEventListener('click', el.clickOutsideEvent)
    },
    unmounted(el) {
      document.body.removeEventListener('click', el.clickOutsideEvent)
    }
  })

  return {}
}

export default _testMixin
