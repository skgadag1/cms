/*
  GA4 event's functions.
  We map ga event name to function, then we call it from plugin level, i.e.:

  $ga4.events.mid_page_cta(event, {cta_type: 'button'})

  The parameters needed for event in GA can be found in the documentation.
  Try to keep all parameter values as functions inside this plugin to separate
  logic from the components. Only those parameters which has to be calculated
  on the component's level should be passed inside config object (i.e. model
  related).
 */

import {convertToPlain} from '../../helpers/functional/AppHelpers'

export default function ($ga4) {
  const midPageCta = (event, model, config = {}) => {
    // mid_page_cta click event
    $ga4.push('mid_page_cta', {
      item_category: model ? $ga4.getProductCategory(model) : $ga4.getProductCategoryWithoutModel(),
      item_name: model ? $ga4.getProductName(model) : undefined,
      cta_text: $ga4.normalizeValue(event?.target?.innerText),
      ...$ga4.normalizeObject(config)
    })
  }

  const navigationClick = (event, config = {}) => {
    $ga4.push('navigation_click', {
      navigation_action: $ga4.getNavigationAction(event),
      navigation_label: $ga4.getInnerTextFromEvent(event),
      navigation_target: $ga4.normalizeValue(event?.target?.attributes?.href?.value) || $ga4.getClosestAnchor(event)?.href,
      ...$ga4.normalizeObject(config)
    })
  }

  const clickToCall = (event, model, config = {}) => {
    // click_to_call event
    $ga4.push('click_to_call', {
      item_category: $ga4.getProductCategory(model),
      item_name: $ga4.getProductName(model, false),
      store_id: $ga4.normalizeValue(model?.dealer?.arNumber),
      store_location: $ga4.normalizeValue(model?.dealer?.name?.replace(/,([\w])/g, ', $1')),
      ...$ga4.normalizeObject(config)
    })
  }

  const formSubmit = (event, formType = 'form_submit', formName = '') => {
    // generic event for all forms
    $ga4.push(formType, {
      form_name: formName
    })
  }

  const formSubmissionFailure = (event, error = 'Unable to send form', formName = '') => {
    // generic event for all forms
    $ga4.push('form_submission_failure', {
      failure_reason: error,
      form_name: formName
    })
  }

  const newsletterSignupFailure = (event, error = 'Unable to send form', formName = '') => {
    // newsletter_signup_failure event
    $ga4.push('newsletter_signup_failure', {
      failure_reason: error,
      form_name: formName
    })
  }

  const formSubmissionComplete = (event, selections = '', formName = '') => {
    // form_submission_complete event
    $ga4.push('form_submission_complete', {
      form_selections: selections,
      form_name: formName
    })
  }

  const newsletterSignupComplete = (event, selections = '', formName = '') => {
    // newsletter_signup_complete event
    $ga4.push('newsletter_signup_complete', {
      form_selections: selections,
      form_name: formName
    })
  }

  const formSubmissionAttempt = (formName = '') => {
    // form_submission_attempt event
    $ga4.push('form_submission_attempt', {
      form_name: formName
    })
  }

  const formStep = (formName, formStepName, formStepSelections, formStepNumber = '1') => {
    // form_step event
    $ga4.push('form_step', {
      form_name: formName,
      form_step_name: formStepName,
      form_step_number: formStepNumber,
      form_step_selections: formStepSelections
    })
  }

  const productClick = (event, model, config = {}) => {
    // product_click event
    $ga4.push('product_click', {
      item_category: $ga4.getProductCategory(model),
      item_name: $ga4.getProductName(model, false),
      click_location: $ga4.getClosestWrapperLocation(event),
      click_text: $ga4.getInnerTextFromEvent(event),
      item_id: $ga4.normalizeValue(model?.id),
      ...$ga4.normalizeObject(config)
    })
  }

  const evolutionModelListingClick = (event, model, headline, count, config = {}) => {
    // model_listing_click event
    $ga4.push('model_listing_click', {
      cta_text: $ga4.getInnerTextFromEvent(event),
      headline: $ga4.normalizeValue(headline),
      subhead: $ga4.normalizeValue(count),
      item_category: $ga4.getProductCategory(model),
      item_name: $ga4.getProductName(model, false),
      item_id: $ga4.normalizeValue(model?.id),
      ...$ga4.normalizeObject(config)
    })
  }

  const productCompareClick = (model, config = {}) => {
    const ga4Obj = {
      item_category: $ga4.getProductMainCategory(model),
      item_category_id: $ga4.getProductCategory(model),
      item_id: model?.id ? $ga4.normalizeValue(model.id) : $ga4.getModels(model),
      item_name: $ga4.getProductName(model, false),
      ...$ga4.normalizeObject(config)
    }

    if (model?.suggestedPrice) {
      Object.assign(ga4Obj, {headline: model?.suggestedPrice})
    }

    $ga4.push('product_compare_click', ga4Obj)
  }

  const productCompareBuildAndPriceClick = (model, config = {}) => {
    const ga4Obj = {
      item_category: $ga4.getProductMainCategory(model),
      item_category_id: $ga4.getProductCategory(model),
      item_id: model?.id ? $ga4.normalizeValue(model.id) : $ga4.getModels(model),
      item_name: $ga4.getProductName(model, false),
      ...$ga4.normalizeObject(config)
    }

    if (model?.suggestedPrice) {
      Object.assign(ga4Obj, {headline: model?.suggestedPrice})
    }

    $ga4.push('product_compare_buildAndPrice_click', ga4Obj)
  }

  const productNavigationClick = (model, ctaText, config = {}) => {
    const ga4Obj = {
      cta_text: $ga4.normalizeValue(ctaText),
      click_location: $ga4.getClosestWrapperLocation(event),
      item_category: $ga4.getProductCategory(model),
      item_name: $ga4.getProductName(model, false),
      ...$ga4.normalizeObject(config)
    }

    $ga4.push('product_navigation_click', ga4Obj)
  }

  const productListingFilter = (sortBy, action, section, range, selection, config = {}) => {
    const payload = {
      sort_by: sortBy,
      filter_action: action,
      filter_section: section,
      filter_range: range,
      filter_selection: selection
    }

    const filteredPayload = Object.fromEntries(
      Object.entries(payload)
        .filter(([_, value]) => value !== undefined && value !== null)
        .map(([key, value]) => [key, $ga4.normalizeValue(value)])
    )

    $ga4.push('product_listing_filter', {
      ...filteredPayload,
      ...$ga4.normalizeObject(config)
    })
  }

  const productPageCta = (event, model, config = {}) => {
    // product_page_cta event
    $ga4.push('product_page_cta', {
      item_category: $ga4.getProductCategory(model),
      item_name: $ga4.getProductName(model, false),
      click_location: $ga4.getClosestWrapperLocation(event),
      item_id: model?.id ? $ga4.normalizeValue(model.id) : $ga4.getModels(model),
      cta_text: $ga4.getInnerTextFromEvent(event),
      cta_type: $ga4.getCtaType(event),
      store_location: model?.dealer?.name ? $ga4.normalizeValue(model?.dealer?.name?.replace(/,([\w])/g, ', $1')) : undefined,
      ...$ga4.normalizeObject(config)
    })
  }

  const productFeaturesClick = (event, model, config = {}) => {
    $ga4.push('product_features_click', {
      item_category: $ga4.getProductCategory(model),
      item_name: $ga4.getProductName(model),
      item_id: model?.id ? $ga4.normalizeValue(model.id) : $ga4.getModels(model),
      navigation_action: $ga4.getAccordionAction(event),
      navigation_label: $ga4.getAccordionLabel(event),
      navigation_location: $ga4.getClosestWrapperLocation(event),
      navigation_target: $ga4.normalizeValue(event?.target?.attributes?.href?.value),
      ...$ga4.normalizeObject(config)
    })
  }

  const accordionGroupClick = (event, model, headline, config = {}) => {
    $ga4.push('accordion_group_click', {
      item_category: $ga4.getProductCategory(model),
      item_name: $ga4.getProductName(model),
      headline: $ga4.normalizeValue(convertToPlain(headline)),
      navigation_action: $ga4.getAccordionAction(event),
      navigation_label: $ga4.getAccordionLabel(event),
      ...$ga4.normalizeObject(config)
    })
  }

  const searchInventory = (itemType, models, distance, zipCode, config = {}) => {
    $ga4.push('search_inventory', {
      item_type: $ga4.normalizeValue(itemType),
      model: models ? $ga4.normalizeValue(models) : undefined,
      distance: $ga4.normalizeValue(distance),
      search_location: $ga4.normalizeValue(zipCode),
      ...$ga4.normalizeObject(config)
    })
  }

  const promotionalBannerCTA = (event, nodeId, config = {}) => {
    $ga4.push('promotional_banner_cta', {
      headline: $ga4.getPromoBannerTitle(event?.target, true),
      subhead: $ga4.getPromoBannerSubtitle(event?.target, true),
      cta_text: $ga4.normalizeValue(event?.target?.innerText),
      click_location: $ga4.getPromoBannerLocation(event?.target),
      creative_id: nodeId,
      ...$ga4.normalizeObject(config)
    })
  }

  const heroSpotCta = (event, slide = {}, model, config = {}) => {
    // hero_spot_cta event
    config.item_name = model ? $ga4.getProductName(model) : undefined
    config.item_category = model ? $ga4.getProductCategory(model) : $ga4.getProductCategoryWithoutModel()
    $ga4.push('hero_spot_cta', {
      creative_id: slide?.asset_id,
      cta_text: $ga4.getInnerTextFromEvent(event),
      headline: $ga4.normalizeValue(slide?.header),
      subhead: $ga4.normalizeValue(slide?.text),
      ...$ga4.normalizeObject(config)
    })
  }

  const viewSearchResults = (event, query, total, searchType = 'site', location, config = {}) => {
    // viewSearchResults event
    const ac = $ga4.getArticlesCount()
    const itemCount = total && ac ? total - parseInt(ac) : undefined
    $ga4.push('view_search_results', {
      search_term: query,
      num_search_results: total,
      search_location: location || $ga4.getClosestWrapperLocation(event),
      item_count: itemCount,
      article_count: ac ? parseInt(ac) : undefined,
      search_type: searchType,
      ...$ga4.normalizeObject(config)
    })
  }

  const carouselClick = (event, model, config = {}) => {
    const headline = event?.target?.closest('div.card') ? event?.target?.innerText : event?.target?.closest('article')?.children?.[0]?.querySelector('[data-test-id$="carousel-title"]')?.innerText
    config.item_name = model ? $ga4.getProductName(model) : undefined
    config.item_category = model ? $ga4.getProductCategory(model) : $ga4.getProductCategoryWithoutModel()
    // carousel_click event
    $ga4.push('carousel_click', {
      headline,
      cta_text: $ga4.getInnerTextFromEvent(event),
      carousel_action: $ga4.determineCarouselAction(event),
      ...$ga4.normalizeObject(config)
    })
  }

  const campaignCardCta = (event, campaign, config = {}) => {
    // campaign_card_cta event
    $ga4.push('campaign_card_cta', {
      headline: campaign?.title,
      subhead: campaign?.description,
      cta_text: campaign?.link?.label,
      item_category: $ga4.getProductCategoryWithoutModel(),
      creative_id: campaign?.['@id'] || '',
      ...$ga4.normalizeObject(config)
    })
  }

  const search = (event, query, searchType = 'site', location, sortBy, filter, config = {}) => {
    // search event
    $ga4.push('search', {
      search_term: query,
      search_type: searchType,
      search_filter: filter,
      search_location: location || $ga4.getClosestWrapperLocation(event),
      sort_by: sortBy,
      ...$ga4.normalizeObject(config)
    })
  }

  const searchResultSelected = (event, query, itm, searchType = 'site', config = {}) => {
    $ga4.push('search_result_selected', {
      search_term: query,
      search_type: searchType,
      search_result: itm?.title || itm?.question || itm,
      ...$ga4.normalizeObject(config)
    })
  }

  const mediaClick = (event, item, config = {}) => {
    // media_click event
    const ga4Obj = {
      creative_id: item?.asset_id || '',
      headline: item?.title || '',
      ...$ga4.normalizeObject(config)
    }

    const productCategory = $ga4.getProductCategoryWithoutModel()

    if (productCategory) {
      Object.assign(ga4Obj, {item_category: productCategory})
    }

    $ga4.push('media_click', ga4Obj)
  }

  const browse = (event, anchor, config = {}) => {
    // browse event
    $ga4.push('browse', {
      click_text: anchor?.innerText || anchor?.textContent || undefined,
      click_location: $ga4.getClosestWrapperLocation(event) || anchor?.getAttribute('data-test-id'),
      click_class: anchor?.className || undefined,
      ...$ga4.normalizeObject(config)
    })
  }

  const productCompareCta = (models, config = {}) => {
    // product_compare_cta event
    const options = {}
    for (let i = 0; i < 3; i++) {
      options[`item_category_${i + 1}`] = $ga4.getProductMainCategory(models?.[i])
      options[`item_category_id_${i + 1}`] = $ga4.getProductCategory(models?.[i])
      options[`item_name_${i + 1}`] = $ga4.getProductName(models?.[i], false)
      options[`item_id_${i + 1}`] = $ga4.normalizeValue(models?.[i]?.id)
      if (models?.[i]?.suggestedPrice) {
        options[`headline_${i + 1}`] = models?.[i].suggestedPrice
      }
    }

    $ga4.push('product_compare_cta', {...options, ...$ga4.normalizeObject(config)})
  }

  const errorGeneral = (details = {}, errorType = 'page error') => {
    // error_general event
    $ga4.push('error_general', {
      error_type: errorType,
      ...details
    })
  }

  const mediaSpotlightCtaClick = (title, subtitle, cta, config = {}) => {
    // media_spotlight_cta_click event
    $ga4.push('media_spotlight_cta_click', {
      item_category: $ga4.getProductCategoryWithoutModel(),
      headline: title?.substring(0, 98),
      subhead: subtitle?.substring(0, 98),
      cta_text: cta?.substring(0, 98),
      ...$ga4.normalizeObject(config)
    })
  }

  const compatibleAttachmentsClick = (model, headline, subhead, cta, config = {}) => {
    config.item_name = model ? $ga4.getProductName(model) : undefined
    config.item_category = model ? $ga4.getProductCategory(model) : $ga4.getProductCategoryWithoutModel()
    // compatible_attachments_click event
    $ga4.push('compatible_attachments_click', {
      headline: $ga4.normalizeValue(convertToPlain(headline)),
      subhead: $ga4.normalizeValue(convertToPlain(subhead)),
      cta_text: $ga4.normalizeValue(cta),
      ...$ga4.normalizeObject(config)
    })
  }

  const offersBannerClick = (headline, cta, action, config = {}) => {
    // cta_banner_click event
    $ga4.push('offers_banner_click', {
      headline: $ga4.normalizeValue(convertToPlain(headline)),
      cta_text: $ga4.normalizeValue(cta),
      event_action: action,
      ...$ga4.normalizeObject(config)
    })
  }

  const ctaBannerClick = (title, paragraph, cta, config = {}) => {
    // cta_banner_click event
    $ga4.push('cta_banner_click', {
      item_category: $ga4.getProductCategoryWithoutModel(),
      headline: title?.substring(0, 98),
      subhead: paragraph?.substring(0, 98),
      cta_text: cta?.substring(0, 98),
      ...$ga4.normalizeObject(config)
    })
  }

  const sparrowQrCode = (code, description, model, category, serialNu) => {
    // sparrow_qr_code click event
    $ga4.push('sparrow_qr_code', {
      headline: code,
      subhead: description,
      item_name: model,
      item_category: category,
      item_id: serialNu
    })
  }

  const imageGalleryGridClick = (model, headline, subhead, creativeId, action, config = {}) => {
    // image_gallery_grid_click click event
    config.item_name = model ? $ga4.getProductName(model) : undefined
    config.item_category = model ? $ga4.getProductCategory(model) : $ga4.getProductCategoryWithoutModel()
    $ga4.push('image_gallery_grid_click', {
      headline: $ga4.normalizeValue(convertToPlain(headline)),
      subhead: $ga4.normalizeValue(convertToPlain(subhead)),
      creative_id: $ga4.normalizeValue(creativeId),
      event_action: action,
      ...$ga4.normalizeObject(config)
    })
  }

  const sideNavGalleryClick = (event, model, headline, action, config = {}) => {
    // side_nav_gallery_click click event
    config.item_name = model ? $ga4.getProductName(model) : undefined
    config.item_category = model ? $ga4.getProductCategory(model) : $ga4.getProductCategoryWithoutModel()
    $ga4.push('side_nav_gallery_click', {
      headline: $ga4.normalizeValue(convertToPlain(headline)),
      cta_text: $ga4.getInnerTextFromEvent(event),
      event_action: action,
      ...$ga4.normalizeObject(config)
    })
  }

  const categorizedCaptionedImageClick = (headline, tabTitle) => {
    // captioned_image_click click event
    const ga4Obj = {
      headline: headline || '',
      click_text: tabTitle || ''
    }

    const productCategory = $ga4.getProductCategoryWithoutModel()

    if (productCategory) {
      Object.assign(ga4Obj, {item_category: productCategory})
    }

    $ga4.push('captioned_image_click', ga4Obj)
  }

  const flexibleListingCardClick = (config = {}) => {
    // flexible_listing_click click event
    $ga4.push('flexible_listing_click', {
      item_category: $ga4.normalizeValue($ga4.getProductCategoryWithoutModel()),
      ...$ga4.normalizeObject(config)
    })
  }

  const dealerPageClick = (ctaType, ctaText, title) => {
    // dealer_page_event click event
    $ga4.push('dealer_page_event', {
      cta_type: $ga4.normalizeValue(ctaType),
      cta_text: $ga4.normalizeValue(ctaText),
      store_location: $ga4.normalizeValue(title)
    })
  }

  const coveoAnalyticsEvents = (payload) => {
    $ga4.push('dealer_locator_event', payload)
  }

  const dealerNotFoundEvent = (location, config = {}) => {
    $ga4.push('dealer_not_found', {
      search_location: $ga4.normalizeValue(location),
      ...$ga4.normalizeObject(config)
    })
  }

  const dealerInventoryEvent = (category, subcategory, config = {}) => {
    const ga4Obj = {
      item_name: category,
      event_action: subcategory ? 'subcategory click' : 'category click',
      ...$ga4.normalizeObject(config)
    }
    if (subcategory) {
      Object.assign(ga4Obj, {model: subcategory})
    }
    $ga4.push('dealer_inventory_event', ga4Obj)
  }

  const dealerInventoryProductClick = (event, category, model, url, price, config = {}) => {
    // new dealer inventory product_click event
    $ga4.push('product_click', {
      click_location: $ga4.getClosestWrapperLocation(event),
      item_category: $ga4.normalizeValue(category),
      item_id: $ga4.normalizeValue(model?.id),
      item_name: $ga4.normalizeValue(model?.name || model?.label),
      click_text: $ga4.normalizeValue(model?.dealer?.name),
      click_url: url,
      headline: $ga4.normalizeValue(`$${price}`),
      ...$ga4.normalizeObject(config)
    })
  }

  const dealerPageProductClick = (event, dealerName, model, url, ctaText, headline, config = {}) => {
    // dealer landing page inventory card product_click event
    $ga4.push('product_click', {
      click_location: $ga4.getClosestWrapperLocation(event),
      item_category: $ga4.normalizeValue(dealerName),
      item_id: $ga4.normalizeValue(model?.id),
      item_name: $ga4.normalizeValue(model?.name || model?.label),
      click_text: $ga4.normalizeValue(ctaText),
      click_url: $ga4.normalizeValue(url),
      headline: $ga4.normalizeValue(headline),
      ...$ga4.normalizeObject(config)
    })
  }

  const tableClick = (event, model, headline, action, label, config = {}) => {
    // table_click event (Evolution Table Component on Equipment Model Pages)
    $ga4.push('table_click', {
      item_category: model ? $ga4.getProductCategory(model) : $ga4.getProductCategoryWithoutModel(),
      item_name: $ga4.getProductName(model),
      headline: $ga4.normalizeValue(headline),
      navigation_action: $ga4.normalizeValue(action),
      navigation_label: $ga4.normalizeValue(label),
      navigation_location: $ga4.getClosestWrapperLocation(event),
      ...$ga4.normalizeObject(config)
    })
  }

  const anchorLinkNavigationClick = (event, model, title) => {
    $ga4.push('anchor_link_navigation', {
      headline: title,
      item_name: model ? $ga4.getProductName(model) : undefined,
      item_category: model ? $ga4.getProductCategory(model) : $ga4.getProductCategoryWithoutModel(),
      item_id: model ? $ga4.normalizeValue(model?.id) : undefined,
      navigation_label: $ga4.getInnerTextFromEvent(event),
      navigation_action: $ga4.getNavigationActionType(event)
    })
  }

  return {
    accordion_group_click: accordionGroupClick,
    mid_page_cta: midPageCta,
    navigation_click: navigationClick,
    media_spotlight_cta_click: mediaSpotlightCtaClick,
    form_submit: formSubmit,
    form_step: formStep,
    newsletter_signup_failure: newsletterSignupFailure,
    form_submission_complete: formSubmissionComplete,
    newsletter_signup_complete: newsletterSignupComplete,
    form_submission_failure: formSubmissionFailure,
    form_submission_attempt: formSubmissionAttempt,
    click_to_call: clickToCall,
    compatible_attachments_click: compatibleAttachmentsClick,
    product_click: productClick,
    model_listing_click: evolutionModelListingClick,
    product_page_cta: productPageCta,
    promotional_banner_cta: promotionalBannerCTA,
    offers_banner_click: offersBannerClick,
    hero_spot_cta: heroSpotCta,
    product_compare_click: productCompareClick,
    product_compare_buildAndPrice_click: productCompareBuildAndPriceClick,
    product_compare_cta: productCompareCta,
    product_features_click: productFeaturesClick,
    product_navigation_click: productNavigationClick,
    product_listing_filter: productListingFilter,
    search_inventory: searchInventory,
    view_search_results: viewSearchResults,
    carousel_click: carouselClick,
    campaign_card_cta: campaignCardCta,
    search,
    search_result_selected: searchResultSelected,
    media_click: mediaClick,
    browse,
    error_general: errorGeneral,
    cta_banner_click: ctaBannerClick,
    sparrow_qr_code: sparrowQrCode,
    image_gallery_grid_click: imageGalleryGridClick,
    side_nav_gallery_click: sideNavGalleryClick,
    flexible_listing_click: flexibleListingCardClick,
    categorized_captioned_image_click: categorizedCaptionedImageClick,
    dealer_page_click: dealerPageClick,
    coveo_analytics_events: coveoAnalyticsEvents,
    dealer_not_found_event: dealerNotFoundEvent,
    dealer_inventory_event: dealerInventoryEvent,
    dealer_inventory_product_click: dealerInventoryProductClick,
    dealer_page_product_click: dealerPageProductClick,
    anchor_link_navigation_click: anchorLinkNavigationClick,
    table_click: tableClick
  }
}
