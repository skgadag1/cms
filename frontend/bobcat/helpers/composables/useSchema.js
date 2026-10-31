/* eslint-disable no-console */
import {convertToPlain} from '../functional/AppHelpers'

export default function useSchema(content, $utils, $regions, $config) {
  const _lang = {name: $regions?.current?.language?.name, label: $regions?.current?.language?.label}

  const toIsoDuration = (seconds) => {
    const total = Math.round(Number(seconds))
    if (!total || isNaN(total)) { return undefined }
    const h = Math.floor(total / 3600)
    const m = Math.floor((total % 3600) / 60)
    const s = total % 60
    return `PT${h ? h + 'H' : ''}${m ? m + 'M' : ''}${s ? s + 'S' : ''}`
  }

  const nameFromPublicId = publicId => publicId?.split('/').pop().replace(/[-_]+/g, ' ').trim()

  const createVideoObject = (_video) => {
    if (!_video?.video) { return null }

    const _meta = _video.metadata || {}
    const name = _meta.context?.caption || _meta.title || _meta.display_name || _meta.filename || nameFromPublicId(_meta.public_id)
    const uploadDate = _meta.created_at

    // Google marks VideoObject without these as invalid, so skip it instead of emitting a broken item
    if (!name || !_video.poster || !uploadDate) { return null }

    return {
      '@context': 'https://schema.org/',
      '@type': 'VideoObject',
      name,
      description: _meta.context?.alt || _meta.description || _meta['alt_' + _lang.name],
      thumbnailUrl: _video.poster,
      thumbnail: {
        '@type': 'ImageObject',
        url: _video.poster
      },
      uploadDate,
      duration: toIsoDuration(_meta.duration),
      contentUrl: _video.video,
      ...((_video.description || _meta.description) && {description: (_video.description || _meta.description)})
    }
  }

  const createImageObject = (_img, _width = 600, _height = 600) => {
    if (!_img?.src) { return null }
    const _imageUrl = $utils.getCloudinaryAsset(_img.src, true, {width: _width, height: _height, crop: 'pad'})
    if (!_imageUrl) { return null }
    return {
      '@context': 'https://schema.org/',
      '@type': 'ImageObject',
      url: _imageUrl,
      contentUrl: _imageUrl,
      creator: organization(),
      creditText: 'Bobcat Company',
      copyrightNotice: 'Bobcat Company',
      ...(!isNaN(_height) && {height: `${_height}px`}),
      ...(!isNaN(_width) && {width: `${_width}px`}),
      ...((_img.name || _img.title || _img.imageTitle) && {name: (_img.name || _img.title || _img.imageTitle)})
    }
  }

  const organization = () => {
    return {
      '@type': 'Organization',
      name: 'Bobcat Company'
    }
  }

  const contactPoint = (telephone, contactType, hoursAvailable) => {
    return {
      '@type': 'ContactPoint',
      areaServed: ['United States', 'Canada'],
      availableLanguage: ['English'],
      telephone,
      contactType,
      hoursAvailable
    }
  }

  const corporation = () => {
    const addressSchema = {
      '@type': 'PostalAddress',
      streetAddress: '250 E Beaton Dr',
      addressLocality: 'West Fargo',
      addressRegion: 'ND',
      postalCode: '58078',
      addressCountry: 'US'
    }

    const geoSchema = {
      '@type': 'GeoCoordinates',
      latitude: '46.84',
      longitude: '-96.89'
    }

    const openingHours = 'Mo, Tu, We, Th, Fr 08:00-16:30'

    return {
      ...organization(),
      '@context': 'https://schema.org/',
      '@type': 'Corporation',
      '@id': `${$config.PUBLIC_HOST}/#organization`,
      url: `${$config.PUBLIC_HOST}`,
      alternateName: 'Doosan Bobcat',
      legalName: 'Doosan Bobcat North America, Inc.',
      foundingDate: '1958',
      logo: 'https://res.cloudinary.com/doosan-bobcat/image/upload/v1724088281/bobcat-assets/common/logos/svg/bobcat-company-official-logo.svg',
      image: 'https://res.cloudinary.com/doosan-bobcat/image/upload/t_jpg/v1700584507/bobcat-assets/na-approved/company/locations/west-fargo-nd/images/bobcat-west-fargo-headquarters-0015-090623.jpg',
      sameAs: [
        'https://en.wikipedia.org/wiki/Bobcat_Company',
        'https://www.facebook.com/BobcatCompany',
        'https://x.com/bobcatcompany',
        'https://www.instagram.com/bobcatcompany',
        'https://www.youtube.com/@BobcatCo',
        'https://www.linkedin.com/company/bobcat-company',
        'https://www.tiktok.com/@bobcatofficial'
      ],
      description: 'Bobcat Company is a global company leading the industry in designing, manufacturing, marketing and distributing top-tier compact equipment.',
      address: addressSchema,
      geo: geoSchema,
      hasMap: 'https://www.google.com/maps/place/Bobcat+Company/@46.8486328,-96.8966827,17z',
      openingHours,
      award: `${$config.PUBLIC_HOST}/na/en/company/about/history/awards-recognition`,
      contactPoint: [
        contactPoint('701-241-8700', 'Regional Headquarters', openingHours),
        contactPoint('1-800-743-4340', 'Customer Service', 'Mo, Tu, We, Th, Fr, Sa, Su 00:00-23:59')
      ]
    }
  }

  const product = () => {
    if (!content || !content?.model || !content?.model?.pricing) {
      return null
    }
    return {
      '@context': 'https://schema.org/',
      '@type': 'Product',
      name: {},
      offers: {
        '@type': 'Offer'
      }
    }
  }

  const breadcrumbs = () => {
    if (!content.breadcrumbs?.length) {
      return null
    }

    const breadcrumbs = content.breadcrumbs.map((breadcrumb, index) => ({
      '@type': 'ListItem',
      position: index + 1,
      name: breadcrumb?.fullText,
      item: `${$config.PUBLIC_HOST}${breadcrumb?.href}`
    }))

    return {
      '@context': 'https://schema.org',
      '@type': 'BreadcrumbList',
      name: 'BreadcrumbList',
      itemListElement: breadcrumbs
    }
  }

  const webSite = () => {
    const websiteSchema = {
      '@type': 'WebSite',
      '@id': `${$config.PUBLIC_HOST}/#website`,
      url: $config.PUBLIC_HOST,
      name: 'Bobcat Company',
      publisher: {
        '@id': `${$config.PUBLIC_HOST}/#organization`
      }
    }

    return {
      '@context': 'https://schema.org',
      '@graph': [
        websiteSchema,
        webPage(websiteSchema['@id'])
      ]
    }
  }

  const webPage = (websiteId = null) => {
    if (!content) { return null }
    const _pageImage = createImageObject(content.primaryPageImage)

    const webPageScheme = {
      '@context': 'https://schema.org/',
      '@type': 'WebPage',
      '@id': `${$config.PUBLIC_HOST}/#webpage`,
      inLanguage: {
        '@type': 'Language',
        name: _lang.label
      },
      name: content.title,
      url: `${$config.PUBLIC_HOST}${$utils.getPageUrl(content, _lang.name)}`,
      datePublished: content.versionTime || content['mgnl:lastModified']
    }

    if (_pageImage) { webPageScheme.primaryImageOfPage = _pageImage }

    if (websiteId) {
      webPageScheme.isPartOf = {'@id': websiteId}
    }

    return webPageScheme
  }

  const faqPage = (id = null) => {
    const questions = []
    const _mainEntity = []

    if (content?.faq) {
      _mainEntity.push(...content.faq.flatMap((item) => {
        if (!item.children || item.children.length === 0) {
          return [{
            '@type': 'Question',
            name: item.title,
            acceptedAnswer: {
              '@type': 'Answer',
              text: item.description ? convertToPlain(item.description) : null
            }
          }]
        }

        return item.children.map(child => ({
          '@type': 'Question',
          name: child.title,
          acceptedAnswer: {
            '@type': 'Answer',
            text: child.description ? convertToPlain(child.description) : null
          }
        }))
      }))
    } else {
      content?.forEach(s => (s['@nodes'])?.forEach(q => questions.push(s[q])))
      _mainEntity.push(...questions?.filter(q => !q?.question?.length || q?.answer?.length)?.map(q => ({
        '@type': 'Question',
        name: q?.question,
        acceptedAnswer: {
          '@type': 'Answer',
          text: q?.answer
        }
      })))
    }

    return {
      ...(id && {'@id': id}),
      '@context': 'https://schema.org',
      '@type': 'FAQPage',
      inLanguage: {
        '@type': 'Language',
        name: _lang.label
      },
      mainEntity: _mainEntity
    }
  }

  const createProductObject = (_product) => {
    const productPrice = _product.price ? String(_product.price).replace(',', '').trim() : undefined
    if (!_product?.name || $regions?.current?.name !== 'na' || !productPrice) { return null }

    const _imageUrl = $utils.getCloudinaryAsset(_product.imageUrl)

    return {
      '@context': 'https://schema.org',
      '@type': 'Product',
      '@id': _product.pageUrl,
      name: _product.name,
      image: _imageUrl,
      description: _product.description,
      url: _product.pageUrl,
      brand: {
        '@type': 'Brand',
        name: 'Bobcat'
      },
      manufacturer: {
        ...organization()
      },
      category: _product.category,
      offers: {
        '@type': 'Offer',
        price: productPrice,
        priceCurrency: 'USD',
        description: 'Starting at $' + productPrice,
        itemCondition: 'https://schema.org/NewCondition',
        availability: 'https://schema.org/InStoreOnly'
      }
    }
  }

  return {
    corporation,
    product,
    webSite,
    faqPage,
    breadcrumbs,
    createImageObject,
    createVideoObject,
    createProductObject
  }
}
