/*
  GA4 event plugin, use as $ga4 object in components.
  Note:
   please, try to keep all logic as generic functions here in the plugin.
   This way we will be able to easily remove/update this if needed.
 */

import * as common from './common'
import events from './events'

export default (context, inject) => {
  if (context.__hasGa4__ !== true) {
    const pushWrapper = (name, data) => {
      const obj = {event: name, ...data}
      return context.$gtm.push(obj)
    }

    try {
      const commonGaFunctions = {
        // eslint-disable-next-line no-console
        ...(context.$gtm ? {push: pushWrapper} : {push: () => { console.warn('[Ga4 Plugin] $gtm not set') }}),
        ...(common?.default && {...common?.default})
      }

      const ga4 = {
        ...commonGaFunctions,
        events: {
          ...events(commonGaFunctions)
        }
      }

      context.$ga4 = ga4
      inject('ga4', ga4)
      context.__hasGa4__ = true
    } catch (e) {
      // eslint-disable-next-line no-console
      console.warn('[Ga4 Plugin] Unable to inject GA4', e)
    }
  }
}
