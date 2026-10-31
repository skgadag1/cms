export default function (context) {
  if (context.__gtmInjected___ !== true) {
    let gtmRegionId
    try {
      gtmRegionId = context.$gtmConfig?.[context.$regions?.current?.name]?.id || context.$gtmConfig?.id
      if (gtmRegionId?.length) { context.$gtm.init(gtmRegionId) }
      // eslint-disable-next-line no-console
      console.log(`[GTM] plugin initialized for id ${gtmRegionId}`)
      context.__gtmInjected___ = true
    } catch (e) {
      // eslint-disable-next-line no-console
      console.warn(`[GTM] plugin unable to init gtm for id ${gtmRegionId}, ${e}`)
    }
  }
}
