import cloudinaryUtils from './cloudinaryUtils'
import coveoUtils from './coveoUtils'
import devicesUtils from './devices'
import downloadUtils from './download'
import linkUtils from './linkUtils'
import pathUtils from './path'
import productUtils from './productUtils'
import {DEFAULT_COMPONENT_HEIGHT_PX} from '@/components/AnchorLinksAlternativeNavigation'

export default (context, inject) => {
  const _utils = {
    getCloudinaryAsset: (src, isImg = true, options = {}, returnSVG = false) => cloudinaryUtils.getCloudinaryAsset.call(context, src, isImg, options, returnSVG),
    getCloudinaryDocument: (src, options = {}) => cloudinaryUtils.getCloudinaryDocument.call(context, src, options),
    adjustDomain: url => cloudinaryUtils.adjustDomain.call(context, url),
    getCloudinaryAssetMetadata: cloudinaryUtils.getCloudinaryAssetMetadata,
    getCloudinaryAssetMediaType: cloudinaryUtils.getCloudinaryAssetMediaType,
    getLinkField: (node, lang) => linkUtils.getLinkField.call(context, node, lang),
    getLinks: (node, lang) => linkUtils.getLinks.call(context, node, lang),
    isExternalLink: anchor => linkUtils.isExternalLink.call(anchor),
    initializeCoveo: (orgId, tokenUri, locationMarkers, regionInfo, addMarker, setMapCenter, dictContext, sortLocations) => coveoUtils.init.call(context, orgId, tokenUri, locationMarkers, regionInfo, addMarker, setMapCenter, dictContext, sortLocations),
    setUserMetadataStorage: (state, productFacetField, industryFacetField, servicesFacetField) => coveoUtils.setUserMetadataStorage.call(context, state, productFacetField, industryFacetField, servicesFacetField),
    getFixedPath: path => pathUtils.pathFix.call(context, path),
    getPageUrl: (itm, lang) => pathUtils.getPageUrl.call(context, itm, lang),
    isCurrentActivePage: (url, path) => pathUtils.isCurrentActivePage.call(context, url, path),
    pathWithoutBaseName: path => pathUtils.pathWithoutBaseName.call(context, path),
    injectLanguageToPath: (path, lang) => pathUtils.injectLanguageToPath.call(context, path, lang),
    downloadFile: (blob, filename, isBlob, targetBlank) => downloadUtils.downloadFile.call(context, blob, filename, isBlob, targetBlank),
    products: productUtils,
    isSmallMobile: () => devicesUtils.isSmallMobile.call(context),
    isMobile: () => devicesUtils.isMobile.call(context),
    isTablet: () => devicesUtils.isTablet.call(context),
    isDesktopLG: () => devicesUtils.isDesktopLG.call(context),
    isDesktopXL: () => devicesUtils.isDesktopXL.call(context),
    extractInheritancePaths: path => pathUtils.extractInheritancePaths.call(context, path),
    scrollToElement: (_id, selector = null) => {
      let y = window.pageYOffset

      const el = selector || document.getElementById(_id)
      if (el) { y += el?.getBoundingClientRect().top }

      const mainHeader = document.querySelector('header[data-test-id="main-header"]')
      if (mainHeader) { y -= (mainHeader?.clientHeight - 1 || 0) }

      const anchorLinksNav = document.querySelector('[data-test-id="anchor-alternative-nav-wrapper"]')
      if (anchorLinksNav) {
        if (
          !anchorLinksNav.classList.contains('position-fixed') &&
          anchorLinksNav.clientHeight > DEFAULT_COMPONENT_HEIGHT_PX
        ) { y -= anchorLinksNav.clientHeight - DEFAULT_COMPONENT_HEIGHT_PX } else if (anchorLinksNav.classList.contains('position-fixed') &&
        anchorLinksNav.clientHeight > DEFAULT_COMPONENT_HEIGHT_PX) {
          y -= DEFAULT_COMPONENT_HEIGHT_PX
        }
      }

      if (y) {
        window.scrollTo({behavior: 'smooth', top: y})
      }
    },
    axiosWithTimeout: async (
      requestConfig,
      opts = {timeout: 0, onSsrOnly: false}
    ) => {
      const timeout = opts?.timeout || context.$config.API_TIMEOUT_MILLIS || 10000
      const response = await context.$axios({
        timeout: (process.server || !opts?.onSsrOnly) ? timeout : 0,
        ...requestConfig
      })

      if (response.success === false) {
        // probably timeout
        const msg = `Request timed out after ${timeout}ms`
        // eslint-disable-next-line no-console
        console.debug(`[utils] ${msg}`)
        throw new Error(msg)
      }

      return response
    }
  }

  context.$utils = _utils
  inject('utils', _utils)
}
