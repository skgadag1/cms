/* eslint-disable no-console */
// classify device and serve proper content upfront

export default function (context) {
  context.$breakpoints = context?.$breakpoints || {
    mobile: process.server ? context?.req?.headers?.['cloudfront-is-mobile-viewer'] === 'true' : context?.$utils?.isMobile() || context?.$utils?.isSmallMobile(),
    tablet: process.server ? context?.req?.headers?.['cloudfront-is-tablet-viewer'] === 'true' : context?.$utils?.isTablet(),
    desktop: process.server
      ? context?.req?.headers?.['cloudfront-is-desktop-viewer'] === 'true' || !(context?.req?.headers?.['cloudfront-is-mobile-viewer'] || context?.req?.headers?.['cloudfront-is-tablet-viewer'])
      : !(context?.$utils?.isSmallMobile() || context?.$utils?.isMobile() || context?.$utils?.isTablet())
  }
  try {
    console.debug(`Breakpoints detected: ${JSON.stringify(context?.$breakpoints)}`)
    console.debug(`Headers received: ${JSON.stringify(context?.req?.headers)}`)
  } catch (e) {
    console.error(`Unable to stringify object in breakpoints module ${e}`)
  }
}
