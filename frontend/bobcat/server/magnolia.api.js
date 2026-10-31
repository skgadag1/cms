/* eslint-disable no-console */
import {Response} from 'miragejs'
import api from '../api'
import pageNav from './responses/magnolia/pagenav/bobcat'
import mainheader from './responses/magnolia/pages/components/mainheader'
import herospot from './responses/magnolia/pages/components/herospot'
import promotionalContent from './responses/magnolia/pages/components/promotionalContent'
import notificationBar from './responses/magnolia/pages/components/notificationBar'
import componentInheritance from './responses/magnolia/inheritance/bobcat'
import regions from './responses/magnolia/regions/bobcat'

export default function (server) {
  server.get(api.getEndpoint(api.endpoints.MAGNOLIA_API.PAGENAV, {path: 'bobcat'}), () => {
    console.debug('[Mock] pagenav endpoint')
    return pageNav
  })

  server.get(api.getEndpoint(api.endpoints.MAGNOLIA_API.PAGES, {path: 'bobcat/na/components/herospot'}), () => {
    return herospot
  })

  server.get(api.getEndpoint(api.endpoints.MAGNOLIA_API.PAGES, {path: 'bobcat/na/components/mainheader'}), () => {
    return mainheader
  })

  server.get(api.getEndpoint(api.endpoints.MAGNOLIA_API.PAGES, {path: 'bobcat/na/components/model-pricing'}), () => {
    return require('./responses/magnolia/pages/components/modelPricing').default
  })

  server.get(api.getEndpoint(api.endpoints.MAGNOLIA_API.PAGES, {path: 'bobcat/na/components/promotional-content'}), () => {
    return promotionalContent
  })

  server.get(api.getEndpoint(api.endpoints.MAGNOLIA_API.PAGES, {path: 'bobcat/na/components/notification-bar'}), () => {
    return notificationBar
  })

  server.get(api.getEndpoint(api.endpoints.MAGNOLIA_API.PAGENAV, {path: 'bobcat/:path'}), (schema, request) => {
    console.debug('[Mock] pagenav endpoint')
    const {path} = request.params
    console.debug('[Mock] pagenav endpoint corrected', path)
    return pageNav[path]
  })

  server.get(api.getEndpoint(api.endpoints.MAGNOLIA_API.PAGES, {path: 'bobcat'}), () => {
    console.debug('[Mock] pages endpoint')
    return require('./responses/magnolia/pages/bobcat').default
  })

  server.get(api.getEndpoint(api.endpoints.MAGNOLIA_API.PAGES, {path: 'bobcat/na'}), () => {
    console.debug('[Mock] pages endpoint')
    return require('./responses/magnolia/pages/na').default
  })

  server.get(api.getEndpoint(api.endpoints.MAGNOLIA_API.PAGES, {path: 'bobcat/na/components'}), () => {
    console.debug('[Mock] component\'s pages endpoint')
    return require('./responses/magnolia/pages/na/components').default
  })

  server.get(api.getEndpoint(api.endpoints.MAGNOLIA_API.PAGES, {path: 'bobcat/na/components/flexiblelisting'}), () => {
    console.debug('[Mock] pages endpoint: flexiblelisting')
    return require('./responses/magnolia/pages/components/flexiblelisting').default
  })

  server.get(api.getEndpoint(api.endpoints.MAGNOLIA_API.PAGES, {path: 'bobcat/na/components/cta'}), () => {
    console.debug('[Mock] pages endpoint: cta')
    return require('./responses/magnolia/pages/components/cta').default
  })

  server.get(api.getEndpoint(api.endpoints.MAGNOLIA_API.PAGES, {path: 'bobcat/na/components/accordion'}), () => {
    console.debug('[Mock] pages endpoint: accordion')
    return require('./responses/magnolia/pages/components/accordion').default
  })

  server.get(api.getEndpoint(api.endpoints.MAGNOLIA_API.PAGES, {path: 'bobcat/na/components/content-carousel'}), () => {
    console.debug('[Mock] pages endpoint: content-carousel')
    return require('./responses/magnolia/pages/components/contentCarousel').default
  })

  server.get(api.getEndpoint(api.endpoints.MAGNOLIA_API.PAGES, {path: 'bobcat/na/components/captioned-image'}), () => {
    console.debug('[Mock] pages endpoint: captioned-image-gallery')
    return require('./responses/magnolia/pages/components/captionedImage').default
  })

  server.get(api.getEndpoint(api.endpoints.MAGNOLIA_API.PAGES, {path: 'bobcat/na/components/features'}), () => {
    console.debug('[Mock] pages endpoint: features')
    return require('./responses/magnolia/pages/components/features').default
  })

  server.get(api.getEndpoint(api.endpoints.MAGNOLIA_API.PAGES, {path: 'bobcat/na/components/mid-buying-tools'}), () => {
    console.debug('[Mock] pages endpoint: mid-buying-tools')
    return require('./responses/magnolia/pages/components/midBuyingTools').default
  })

  server.get(api.getEndpoint(api.endpoints.MAGNOLIA_API.PAGES, {path: 'bobcat/na/components/articles/quote'}), () => {
    console.debug('[Mock] pages endpoint: articles quote')
    return require('./responses/magnolia/pages/components/quote').default
  })

  server.get(api.getEndpoint(api.endpoints.MAGNOLIA_API.PAGES, {path: 'bobcat/na/components/articles/header-section'}), () => {
    console.debug('[Mock] pages endpoint: articles header-section')
    return require('./responses/magnolia/pages/components/headerSection').default
  })

  server.get(api.getEndpoint(api.endpoints.MAGNOLIA_API.PAGES, {path: 'bobcat/na/components/articles/text-section'}), () => {
    console.debug('[Mock] pages endpoint: articles text-section')
    return require('./responses/magnolia/pages/components/textSection').default
  })

  server.get(api.getEndpoint(api.endpoints.MAGNOLIA_API.PAGES, {path: 'bobcat/na/components/articles/video'}), () => {
    console.debug('[Mock] pages endpoint: articles video')
    return require('./responses/magnolia/pages/components/video').default
  })

  server.get(api.getEndpoint(api.endpoints.MAGNOLIA_API.PAGES, {path: 'bobcat/na/components/download-table'}), () => {
    console.debug('[Mock] pages endpoint: download-table')
    return require('./responses/magnolia/pages/components/downloadTable').default
  })

  server.get(api.getEndpoint(api.endpoints.MAGNOLIA_API.PAGES, {path: 'bobcat/na/components/series-navigation'}), () => {
    console.debug('[Mock] pages endpoint: series-navigation')
    return require('./responses/magnolia/pages/components/seriesNavigation').default
  })

  server.get(api.getEndpoint(api.endpoints.MAGNOLIA_API.PAGES, {path: 'bobcat/na/components/region-selector'}), () => {
    console.debug('[Mock] pages endpoint: region-selector')
    return require('./responses/magnolia/pages/components/regionSelector').default
  })

  server.get(api.getEndpoint(api.endpoints.MAGNOLIA_API.PAGES, {path: 'bobcat/na/components/attachment-related-products'}), () => {
    console.debug('[Mock] pages endpoint: attachment-related-products')
    return require('./responses/magnolia/pages/components/attachmentRelatedProducts').default
  })

  server.get(api.getEndpoint(api.endpoints.MAGNOLIA_API.PAGES, {path: 'bobcat/na/components/breadcrumb'}), () => {
    console.debug('[Mock] pages endpoint: breadcrumb')
    return require('./responses/magnolia/pages/components/breadcrumb').default
  })

  server.get(api.getEndpoint(api.endpoints.MAGNOLIA_API.PAGES, {path: 'bobcat/na/components/accordion-group'}), () => {
    console.debug('[Mock] pages endpoint: accordion-group')
    return require('./responses/magnolia/pages/components/accordionGroup').default
  })

  server.get(api.endpoints.MAGNOLIA_API.REGIONS, () => {
    console.debug('[Mock] regions endpoint')
    return regions
  })

  server.get(api.endpoints.MAGNOLIA_API.INHERITANCE.split('?')[0].replace('%PATH%', ''), () => {
    console.debug('[Mock] inheritance endpoint')
    return componentInheritance
  })

  // simulate network error
  server.get(api.endpoints.MAGNOLIA_API.PAGENAV, {path: 'bobcat/test'}, () => {
    console.debug('[Mock] pagenav error endpoint')
    throw new Error('Network error')
  })

  server.get('/', () => {
    console.debug('[Mock] pages endpoint: not existing url, 404')
    return new Response(
      404,
      {errors: ['No page found']}
    )
  })
}
