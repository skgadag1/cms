import {ref, computed, inject, provide, getCurrentInstance, type InjectionKey} from 'vue'
import {useStore} from 'vuex'
import {contentService} from '@/services'
import {translate as formatLabel, translateLocale, updateUrlParam} from '@/composables/useFormatters'
import type {LabelValues, SerialError, UseLabelsReturn} from '@/types/useLabels'

type PageConfig = Record<string, unknown> | undefined
const PAGE_CONFIG_KEY: InjectionKey<() => PageConfig> = Symbol('pageConfig')
type TranslationRow = {key: string; value: string}

const resolvePageConfig = (instance: NonNullable<ReturnType<typeof getCurrentInstance>>): () => PageConfig => {
  const isPageComponent = !instance.parent || instance.parent === instance.root
  if (isPageComponent) {
    const getter = () => (instance.proxy as typeof instance.proxy & {componentConfig?: PageConfig}).componentConfig
    provide(PAGE_CONFIG_KEY, getter)
    return getter
  }

  return inject(PAGE_CONFIG_KEY, () => {
    const page = instance.proxy.$root.$children && instance.proxy.$root.$children[0]
    return page ? page.componentConfig : undefined
  })
}

export function useLabels(conceptNames?: string | string[] | null): UseLabelsReturn {
  const instance = getCurrentInstance()
  if (!instance) throw new Error('useLabels() must be called from a component setup() function')
  const componentConceptNames = (instance.type as unknown as {
    conceptName?: string | string[] | null;
  }).conceptName
  const conceptsToLoad = conceptNames === undefined ? componentConceptNames : conceptNames
  const store = useStore()
  const pageConfig = resolvePageConfig(instance)
  const componentLabels = ref<Record<string, string | Record<string, string>>>({})

  const labels = computed(() => {
    if (store && typeof store.state.showConceptNames !== 'undefined' && store.state.showConceptNames) return {'show': 'concepts'}
    return {...componentLabels.value, config: pageConfig() as Record<string, string> | undefined} as LabelValues
  })

  const translate = (conceptKey: string, conceptPath: string | null = null): string => formatLabel(conceptKey, labels.value, conceptPath)
  const translatedConfig = (conceptKey: string): string => formatLabel(conceptKey, labels.value, 'config')

  const getConceptNameValues = (cn: string, append = false): void => {
    let _l: Record<string, string> = {}

    contentService.fetchTranslations('components/' + cn).then(r => {
      if (r.data && r.data.length) {
        _l = r.data.reduce((accum: Record<string, string>, currentVal: TranslationRow) => {
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
      if (append) componentLabels.value = {...componentLabels.value, [cn]: _l}
      else componentLabels.value = _l
    })
  }

  const getSerialErrorMessage = (error: SerialError, sn: string): string => {
    let message = translate('defaultError', 'CommonErrors')
    if (error.error_code) switch (error.error_code) {
    case '400':
      message = translate('wrongSerialNumberFormat', 'AddSerialControl')
      break
    case '403':
      message = translate('notAuthorisedSN', 'CommonErrors').replace('_sn_', sn)
      if (/stolen/.test(error.error_message)) message = translate('stolenMachine', 'AddSerialControl')
      if (/organization is invalid/.test(error.error_message)) message = translate('invalidOrganization', 'AddSerialControl')
      break
    case '404':
      message = (/^Organization with id\s\w+\snot found.$/.test(error.error_message))
        ? translate('notAuthorisedSN', 'CommonErrors').replace('_sn_', sn)
        : translate('noSerialFound', 'CommonErrors')
      break
    }
    return message
  }

  if (Array.isArray(conceptsToLoad)) conceptsToLoad.forEach(cn => getConceptNameValues(cn, true))
  else if (conceptsToLoad) getConceptNameValues(conceptsToLoad)

  const result = {labels, translate, translatedConfig, getSerialErrorMessage, updateUrlParam, translateLocale}
  return result
}

export default useLabels
