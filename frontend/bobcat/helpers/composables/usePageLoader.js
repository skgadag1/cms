import {ref, useRouter} from '@nuxtjs/composition-api'
import {BUSINESS_REGIONS} from '~/constants'
import {inEditorEdit} from '~/helpers/vue-editor'
import {ATTACHMENT_PAGE_TEMPLATE_NAME} from '~/constants/pageTemplates'
import {DEALER_INVENTORY_ROUTE, DEALER_LOCATOR_PAGE_TEMPLATE_ROUTE} from '~/constants/staticRoutes'
import useMounted from '~/helpers/composables/useMounted'

export default function usePageLoader({$utils, $services, $regions, $config, route, redirect}) {
  const content = ref(null)
  const primaryImage = ref(null)
  const title = ref(null)
  const facetTitle = ref(null)
  const navigationLabel = ref(null)
  const description = ref(null)
  const keywords = ref(null)
  const pageRobots = ref(null)
  const excludeFromSearch = ref(null)
  const templateAnnotations = ref({})
  const {clearComponentsStack} = useMounted()
  const router = useRouter()
  const isNotNARegion = ref(!(BUSINESS_REGIONS.na.includes($regions.current.name)))

  const generateMissingAreas = (content, templateAnnotations) => {
    // extended version from TemplateAnnotations from module '@magnolia/tempalte-annotations'
    // which also adds names to '@areaNodes'
    if (!content || !templateAnnotations) {
      return content
    }
    const decoratedContent = JSON.parse(JSON.stringify(content))
    const areas = []
    Object.keys(templateAnnotations).forEach((key) => {
      if (key.includes(content?.['@path']) && templateAnnotations[key].startsWith('cms:area')) {
        const areaName = key.split('/').pop()
        if (!(areaName in content.areaNodes)) {
          decoratedContent.areaNodes[areaName] = {
            '@path': key || '' + '/' + areaName,
            '@nodes': content.areaNodes?.[areaName]?.areaNodes || [],
            '@nodeType': 'mgnl:area',
            '@id': content?.['@path'] || '' + '/' + areaName
          }
        }
        areas.push(areaName)
      }
    })
    if (decoratedContent['@areaNodes'].length !== areas.length) {
      areas.forEach((areaName) => { if (!(decoratedContent['@areaNodes'].includes(areaName))) { decoratedContent['@areaNodes'].push(areaName) } })
    }

    // sorting areas in "proper" order: header, rest (alph), footer
    const allAreaNames = decoratedContent['@areaNodes']
    decoratedContent['@areaNodes'] = [...allAreaNames.filter(x => x === 'header'), ...allAreaNames.filter(x => x !== 'header' && x !== 'footer').sort(), ...allAreaNames.filter(x => x === 'footer')].filter(x => x)

    return decoratedContent
  }

  const loadPage = async (currentRoute, dealerInventoryParams) => {
    try {
      if (process.server) { clearComponentsStack() }
      const {data} = await $services.content.fetchPageDetails(currentRoute)
      if (!data) { return }
      let contentData = data
      const pageTemplateId = data['mgnl:template']
      let model = null
      if (dealerInventoryParams && !dealerInventoryParams.equipmentPrint) {
        model = await $services.dealerinventory.getModelDetails(dealerInventoryParams.dealerID, dealerInventoryParams.modelID, true, true, route.value.query)
        if (model.errors) {
          const queryParams = route.value?.query
          if (queryParams.distance) { queryParams.distance = Math.ceil(parseFloat(queryParams.distance) / 10) * 10 }
          const url = `/${$regions.current.name}/${$regions.current.language.name}${DEALER_INVENTORY_ROUTE}?${Object.entries(queryParams).map(([key, value]) => `${key}=${value}`).join('&')}`
          // eslint-disable-next-line no-console
          console.error(`[PageLoader] unable to find model in dealer inventory /${dealerInventoryParams.dealerID}/${dealerInventoryParams.modelID}`)
          redirect(301, url)
        }
        model.serial = model.serial || dealerInventoryParams.modelID
      } else if (dealerInventoryParams && dealerInventoryParams.equipmentPrint) {
        const modelId = await $services.content.getModelPageId(dealerInventoryParams.modelID, 'Current')
        model = await $services.bobcatapi.getProduct(modelId)
      } else {
        model = data.model?.['@id'] || data.attachmentModel?.['@id'] // fallback to published "old" pages with resolved models
          ? data.model || data.attachmentModel
          : (pageTemplateId === ATTACHMENT_PAGE_TEMPLATE_NAME ? await $services.bobcatapi.getAttachment(data?.attachmentModel) : await $services.bobcatapi.getProduct(data?.model))
      }
      if (contentData['@areaNodes']) {
        const weight = {header: -1, main: 0, footer: 1}
        contentData['@areaNodes'] = contentData['@areaNodes'].sort((a, b) => weight[a] - weight[b])
      }

      if (inEditorEdit) {
        templateAnnotations.value = await $services.bobcatapi.getTemplateAnnotations(data['@path'])
        contentData = generateMissingAreas(data, templateAnnotations.value)
      }

      title.value = contentData.titleWithAffix || contentData.title || contentData['@name']
      facetTitle.value = contentData.facetTitle || contentData.navigationLabel
      navigationLabel.value = contentData.navigationLabel
      description.value = contentData.description
      keywords.value = contentData.keywords
      pageRobots.value = contentData.robots
      excludeFromSearch.value = contentData.excludeFromSearch
      content.value = {
        ...contentData, // ToDo: map only relevant props here, remove rest,
        ...(model && {model})
      }

      if (currentRoute.path.includes(`${DEALER_LOCATOR_PAGE_TEMPLATE_ROUTE}/`)) {
        const mdmId = router.currentRoute.path.split(DEALER_LOCATOR_PAGE_TEMPLATE_ROUTE)[1]?.replace('/', '')
        const dealerDetails = await $services.dealerlocator.getDealerDetails(mdmId)
        if (!dealerDetails || dealerDetails?.errors) {
          const dealerLocatorUrl = `/${$regions.current.name}/${$regions.current.language.name}${DEALER_LOCATOR_PAGE_TEMPLATE_ROUTE}`
          return redirect(301, dealerLocatorUrl)
        }

        if (mdmId && dealerDetails) {
          description.value = (description?.value
            ?.replaceAll('%DEALER_NAME%', dealerDetails?.mailingLabel || '')
            ?.replaceAll('%DEALER_CITY%', dealerDetails?.shippingCity || '')
            ?.replaceAll('%DEALER_STATE%', dealerDetails?.shippingState || '')
            ?.replaceAll('%DEALER_COUNTRY%', dealerDetails?.shippingCountry || '')) || ''

          const joinNonEmpty = (values, separator) =>
            values.filter(value => value !== null && value !== undefined && value !== '').join(separator)

          if (isNotNARegion.value) {
            title.value = title.value.replace('%DEALER_DYNAMIC_TITLE_DATA%',
              `${joinNonEmpty([dealerDetails?.mailingLabel, dealerDetails?.shippingCity], ' - ')}`)
          } else {
            title.value = title.value.replace('%DEALER_DYNAMIC_TITLE_DATA%', `${joinNonEmpty([dealerDetails?.mailingLabel, dealerDetails?.shippingCity], ' - ')}${dealerDetails?.shippingState ? `, ${dealerDetails.shippingState}` : ''}`)
          }
        }
      }

      const _assetSrc = await $utils.getCloudinaryAsset(content.value?.primaryPageImage?.src, true, {
        width: 600,
        height: 600,
        crop: 'pad',
        format: 'jpg'
      })
      if (_assetSrc) {
        const {metadata} = await $utils.getCloudinaryAssetMetadata(content.value?.primaryPageImage?.src)
        primaryImage.value = {
          src: _assetSrc,
          alt: content.value?.primaryPageImage?.imageAlt || content.value?.primaryPageImage?.imageTitle,
          title: content.value?.primaryPageImage?.imageTitle,
          cloudinaryAsset: content.value?.primaryPageImage?.src,
          ...metadata
        }
      }

      try {
        content.value.dxpTags = await getDxpFacets(data?.dxpTags?.['@nodes']?.map(x => data?.dxpTags[x]?.dxpTag))
      } catch (e) {
        // eslint-disable-next-line no-console
        console.warn(`[usePageLoader] Unable to retrieve dxp tags (facets), ${e}`)
      }
    } catch (e) {
      // eslint-disable-next-line no-console
      console.warn(`[usePageLoader] Unable to desctructuring data from endpoint, ${e}`)
    }
  }

  const fetchCampaings = async () => {
    if (content.value?.['@id']) {
      try {
        const results = await $services.bobcatapi.getCampaignById(content.value['@id'])
        return results?.map(val => ({
          ...val,
          image: {
            src: $utils.getCloudinaryAsset(val?.image?.src, true, {crop: 'fill'}) || $utils.getCloudinaryAsset($config.PLACEHOLDER_IMAGE, true, {crop: 'fill'}),
            alt: val?.image?.imageAlt,
            title: val?.image?.imageTitle
          },
          link: $utils.getLinkField(val?.link, $regions.current.language.name)
        })) || []
      } catch (e) {
        // eslint-disable-next-line no-console
        console.debug(`[usePageLoader] Unable to get campaigns ${e}`)
      }
    }
  }

  const generateFacetsFromDxpTags = (dxpTags = content.value?.dxpTags) => {
    if (Array.isArray(dxpTags) && dxpTags) {
      const _tags = []

      dxpTags?.forEach((tag) => {
        switch (tag['@nodeType']) {
        case 'dxp:tag':
          _tags.push(extractTag(tag))
          break
        case 'dxp:tagGroup':
          _tags.push(...tag['@nodes'].map(x => extractTag(tag[x])))
          break
        default:
          break
        }
      })

      try {
        return JSON.stringify(_tags).replace(/"/g, '&quot;')
      } catch (e) {
        // eslint-disable-next-line no-console
        console.error(`[PageLoader] unable to stringify tags, ${_tags} ${e}`)
        return null
      }
    }
  }

  const extractTag = (t) => {
    const _splittedPath = t['@path'].split('/')
    const _nameFromPath = _splittedPath[_splittedPath.length - 2]
    return {
      id: t['@name'],
      type: _nameFromPath,
      value: t.label
    }
  }

  const getDxpFacets = async (_tagsIds = []) => {
    if (!Array.isArray(_tagsIds) && !_tagsIds.length) { return null }
    _tagsIds = _tagsIds.map(x => typeof x === 'object' ? x?.['@id'] : x).filter(y => y)
    const _data = await $services.bobcatapi.fetchDxpTags(_tagsIds) || []
    return _data
  }

  const generateTemplateName = () => {
    let pageTemplateName = ''

    if (content.value) {
      try {
        const _name = content.value['mgnl:template'].split('/')[1]
        switch (_name) {
        case 'attachment-details':
          pageTemplateName = 'attachment'
          break
        case 'machine-details':
          pageTemplateName = 'product'
          break
        case 'historical-machine-details':
          pageTemplateName = 'historical-product'
          break
        case 'article':
          pageTemplateName = 'article'
          break
        case 'video':
          pageTemplateName = 'video'
          break
        default:
          pageTemplateName = 'page'
        }
      } catch {
        // eslint-disable-next-line no-console
        console.error('[PageLoader] Unable to parse template name from content')
      }
    }

    return pageTemplateName
  }

  const generateSortRating = () => {
    let sortRating = ''

    if (content.value) {
      try {
        sortRating = content.value.sortRating
      } catch {
        // eslint-disable-next-line no-console
        console.error('[PageLoader] Unable to parse page sort rating from content')
      }
    }

    return sortRating
  }

  // const generateHreflangObjects = (alternativeUrls = []) => alternativeUrls.map((alt) => { return {rel: 'alternate', href: `${$config.PUBLIC_HOST}${alt.url}`, hreflang: alt.lang} }) || []

  return {
    templateAnnotations,
    content,
    primaryImage,
    title,
    facetTitle,
    navigationLabel,
    description,
    keywords,
    loadPage,
    getDxpFacets,
    generateFacetsFromDxpTags,
    generateTemplateName,
    generateSortRating,
    fetchCampaings,
    pageRobots,
    excludeFromSearch
  }
}
