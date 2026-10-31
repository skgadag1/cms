import {cacheAdapterEnhancer} from 'axios-extensions'
import {getAdapter} from 'axios'

export default function (context) {
  if (context.__hasCachePluginEnabled__ !== true) {
    const defaultAdapter = getAdapter(context.$axios.defaults.adapter)
    context.$axios.defaults.adapter = cacheAdapterEnhancer(defaultAdapter, {
      enabledByDefault: false,
      cacheFlag: 'useCache',
      ...(context.ssrContext?.$cacheLRU && {defaultCache: context.ssrContext?.$cacheLRU})
    })

    context.__hasCachePluginEnabled__ = true
  }
}
