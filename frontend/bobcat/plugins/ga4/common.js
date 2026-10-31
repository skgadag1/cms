// common helper methods for GA4 plugin
const capitalizeFirstLetter = (str) => {
  return str?.length ? str.toLowerCase().split(/(?:-| )+/).map(s => s.charAt(0).toUpperCase() + s.slice(1)).join(' ') : str
}

const isProductPage = () => {
  if (document) {
    return document.querySelector('body[data-page-type="product"]')
  }
}

const getPageTitle = () => {
  if (document) {
    const title = isProductPage() ? document.querySelector('[data-test-id="model-pricing-subtitle__desktop"]')?.innerText : document.querySelector('h1')?.innerText
    return title || document.title
  }
  return null
}

const normalizeValue = val => val?.length ? val.substring(0, 98) : val

const normalizeObject = object =>
  Object.fromEntries(
    Object.entries(object).map(
      ([k, v], i) => [k, normalizeValue(v)]
    )
  )

const getModels = (models) => {
  return models['@nodes']?.filter(id => !(['images', 'videos'].includes(id)))?.map(modelName => models[modelName]?.id)?.join(', ')
}

// values for specific keys

const extractCategoryFromSources = (model, extractFn) => {
  const sources = [
    window?.location?.pathname,
    model?.link?.url,
    model?.productTypeLabel
  ]

  for (const path of sources) {
    const raw = extractFn(path)
    if (raw) {
      return normalizeValue(capitalizeFirstLetter(raw))
    }
  }

  return undefined
}

const getProductCategory = (model) => {
  // keys: 'item_category', 'item_category_id'
  // returns (based on url!):
  // (1) Equipment category on equipment pages.
  // (2) Equipment subcategory (if present) or category on model pages.
  // (3) category/subcategory from model on another pages (i.e. dealer-inventory)
  return extractCategoryFromSources(model, (path) => {
    const parts = path?.split('/equipment/')[1]?.split('/')
    return parts?.[Math.min(parts.length - 1, 1)]
  })
}

const getProductMainCategory = (model) => {
  // keys: 'item_category'
  // returns based on url Equipment category on equipment pages.
  return extractCategoryFromSources(model, (path) => {
    return path?.split('/equipment/')[1]?.split('/')?.[0]
  })
}

const getProductCategoryWithoutModel = () => {
  // keys: 'item_category'
  const urlEquipment = window?.location?.pathname?.split('/equipment/')
  const urlModel = urlEquipment?.[1]?.split('/')
  const urlCategory = urlModel?.[Math.min(urlModel?.length - 1, 1)]

  const category = urlCategory || urlModel?.[0]
  return normalizeValue(capitalizeFirstLetter(category))
}

const getProductName = (model, checkIsProductPage = true) => {
  // keys: 'item_name'
  if (checkIsProductPage) {
    return isProductPage() ? normalizeValue(model?.title || model?.['@name'] || model?.name) : undefined
  } else {
    return normalizeValue(model?.title || model?.['@name'] || model?.name)
  }
}

const getPromoText = (element, promoSelector) => {
  const promoContent = element.closest('[data-test-id="promotional-content"]')
  return normalizeValue(promoContent?.querySelector?.(`[data-test-id="${promoSelector}"]`)?.innerText) || undefined
}

const getPreviousOrNextPromoText = (element, promoSelector) => {
  // grab value only if direct element's sibling is promotional component (using 'data-tech-key' => works only on SSR!)
  const promoContentText = document?.querySelector(`[data-test-id="${promoSelector}"]`)
  const promoFetchKey = promoContentText?.closest('div[data-fetch-key]')?.getAttribute('data-fetch-key')
  const promoContentComponentNo = promoFetchKey?.split(':')?.[1] || promoFetchKey
  const currentComponentFetchKey = element?.closest('div[data-fetch-key]')?.getAttribute('data-fetch-key')
  const currentComponentNo = currentComponentFetchKey?.split(':')?.[1] || currentComponentFetchKey

  const parsedPCCNo = parseInt(promoContentComponentNo)
  const parsedCNo = parseInt(currentComponentNo)
  const normalizedText = normalizeValue(promoContentText?.innerText)

  if (parsedPCCNo === parsedCNo - 1 || parsedPCCNo === parsedCNo + 1) {
    return normalizedText
  }

  return undefined
}

const getPromoBannerTitle = (element, current = false) => {
  // keys: 'headline'
  return current
    ? getPromoText(element, 'promotional-content-title')
    : getPreviousOrNextPromoText(element, 'promotional-content-title')
}

const getPromoBannerSubtitle = (element, current = false) => {
  // keys: 'subhead'
  return current
    ? getPromoText(element, 'promotional-content-paragraph')
    : getPreviousOrNextPromoText(element, 'promotional-content-paragraph')
}

const getPromoBannerLocation = (element) => {
  // keys: 'click_location'
  return normalizeValue(element?.closest('[data-fetch-key]')?.attributes['data-fetch-key']?.value)
}

const getClosestWrapperLocation = (event) => {
  // keys: 'click_location'
  return event?.target?.parentNode?.closest('[data-test-id]')?.attributes['data-test-id']?.value
}

const getClosestAnchor = (event) => {
  // keys: navigation_target
  return event?.target?.closest('a')
}

const getInnerTextFromEvent = (event) => {
  // keys: 'cta_text'
  return event?.target?.innerText || undefined
}
const getCtaType = (event) => {
  // keys: 'cta_type'
  return event.target.classList.contains('btn') ? 'button' : 'link'
}

const getNavigationAction = (event) => {
  // keys: 'navigation_action'
  if (event?.target?.closest('[aria-expanded]')?.attributes['aria-expanded'].value === 'false') {
    return 'expand'
  } else if (event?.target?.closest('[aria-expanded]')?.attributes['aria-expanded'].value === 'true') {
    return 'collapse'
  } else {
    return 'click'
  }
}

const getAccordionAction = (event) => {
  // keys: 'navigation_action'
  if (event?.target?.closest('section .collapse') === null &&
    event?.target?.closest('section .accordion-wrapper') === null) {
    // does not have accordion as an ancestor, it's Expand all or Collapse all
    return (event?.target?.attributes['aria-expanded']?.value === 'true') ? 'collapse all' : 'expand all'
  } else if (event?.target?.closest('section > [aria-expanded]')?.attributes['aria-expanded'].value === 'false') {
    return 'expand'
  } else if (event?.target?.closest('section > [aria-expanded]')?.attributes['aria-expanded'].value === 'true') {
    return 'collapse'
  } else {
    return 'click'
  }
}

const getAccordionLabel = (event) => {
  // keys: 'navigation_label'
  if ((event?.target?.closest('section .collapse') === null &&
    event?.target?.closest('section .accordion-wrapper') === null) ||
    !event?.target?.hasAttribute('data-test-id')) {
    // does not have accordion as an ancestor, it's Expand all or Collapse all
    return event?.target?.innerText
  } else {
    return event?.target?.closest('section [data-test-id="accordion"]')?.querySelector('.accordion-title')?.innerText
  }
}

const getInnerTextFromSelector = (event, dataTestID, selector) => {
  // keys: 'headline or subheadline'
  return event?.target?.closest(`[data-test-id='${dataTestID}']`)?.querySelector(selector)?.innerText.slice(0, 98)
}

const getArticlesCount = () => {
  // keys: 'article_count'
  return document.querySelector?.('div[data-test-id="facet-checkboxGroupFacets-pageType.keyword"] .bv-no-focus-ring')?.children[0]?.querySelector('label.custom-control-label')?.innerText?.split(' ')?.[1]?.slice(1, -1)
}

const getListingCount = (event, wrapperSelector, countSelector) => {
  // keys: 'subhead'
  return event?.target?.closest(wrapperSelector)?.querySelector(countSelector)?.innerText.slice(0, 98)
}

const determineCarouselAction = (event) => {
  const target = event?.target
  if (!target) {
    return undefined
  }

  const dataTestId = target.getAttribute('data-test-id')
  if (['product-carousel-dot', 'content-carousel-dot', 'evolution-content-carousel-dot'].includes(dataTestId)) {
    return 'dot click'
  }

  if (['product-carousel-arrow-left', 'content-carousel-arrow-left', 'evolution-content-carousel-arrow-left'].includes(dataTestId)) {
    return 'left arrow click'
  }

  if (['product-carousel-arrow-right', 'content-carousel-arrow-right', 'evolution-content-carousel-arrow-right'].includes(dataTestId)) {
    return 'right arrow click'
  }

  if (['content-carousel-item-link'].includes(dataTestId)) {
    return 'card click'
  }

  if (target.tagName === 'A' || ['product-listing-item-link'].includes(dataTestId)) {
    return 'link click'
  }

  return undefined
}

const getNavigationActionType = (event) => {
  // keys: 'navigation_action_type'
  const navigationAction = getNavigationAction(event)

  if (navigationAction === 'click') {
    const href = event?.target?.getAttribute('href')
    if (href.startsWith('#')) { return 'anchor click' } else if (href.startsWith('http')) {
      const url = new URL(href)
      if (url.origin + url.pathname === window.location.origin + window.location.pathname) { return 'anchor click' }
    } else { return 'link click' }
  }
  return navigationAction
}

export default {
  normalizeValue,
  normalizeObject,
  getModels,
  getClosestAnchor,
  getNavigationAction,
  getPageTitle,
  getProductCategory,
  getProductMainCategory,
  getProductName,
  getPromoBannerSubtitle,
  getPromoBannerTitle,
  getPromoBannerLocation,
  getClosestWrapperLocation,
  getInnerTextFromEvent,
  getAccordionAction,
  getAccordionLabel,
  getCtaType,
  getInnerTextFromSelector,
  getArticlesCount,
  determineCarouselAction,
  getProductCategoryWithoutModel,
  getNavigationActionType,
  getListingCount
}
