/* eslint-disable no-console */
import {inEditorEdit} from '~/helpers/vue-editor'

export default async function ({$regions, route, redirect, $services, req}) {
  const defaultRegion = $regions.all.find(region => region?.businessUnit?.toLowerCase() === process.env.NUXT_DEFAULT_REGION)
  if (!defaultRegion) {
    console.error('[Region middleware] Cannot define default region - check region\'s businessUnit(?)')
  }
  const defaultRegionLangList = defaultRegion['@nodes']?.map(langName => defaultRegion[langName]).filter(lang => lang?.enabled === 'true')
  const fallbackLanguage = (defaultRegionLangList?.find(lang => lang?.isDefault === 'true') || defaultRegionLangList?.find(lang => lang?.['@name']?.toLowerCase() === process.env.NUXT_DEFAULT_LANGUAGE) || defaultRegionLangList[0])['@name']

  let currentRegionName = $regions?.current?.name
  let currentLanguageName = $regions?.current?.language?.name

  if (inEditorEdit || route?.path?.startsWith('/error') || route?.path?.startsWith('/region')) {
    // if author or error or region page - skip
    return null
  } else if (!currentRegionName || !$regions?.all?.find(region => region?.['@name']?.toLowerCase() === currentRegionName)) {
    // no region or region invalid - need to detect
    let location = null

    try {
      const {data} = await $services.content.getClientLocation(req)
      location = data?.selected || data?.detected || data
      console.warn(`[regions] Client region detected as ${JSON.stringify(location)}`)
    } catch (error) {
      console.warn(`[regions] Unable to read client location, ${error.message}`)
    }

    // if no region - set detected region
    const detectedRegion = $regions.all.find(region => region?.['@name']?.toLowerCase() === location?.siteRegion?.toLowerCase()) ||
      $regions.all.find(region => region?.businessUnit?.toLowerCase() === location?.siteRegion?.toLowerCase()) ||
      $regions.all.find(region => region?.businessUnit?.toLowerCase() === location?.region?.toLowerCase()) ||
      $regions.all.find(region => region?.businessUnit?.toLowerCase() === process.env.NUXT_DEFAULT_REGION)
    currentRegionName = detectedRegion?.['@name'] || process.env.NUXT_DEFAULT_REGION

    // set best matching lang - detected or default
    const detectedRegionLangs = detectedRegion?.['@nodes']?.map(langName => detectedRegion[langName]).filter(lang => lang?.enabled === 'true') || []
    const defaultRegionLang = (detectedRegionLangs.find(lang => lang.isDefault === 'true') || detectedRegionLangs.find(lang => lang?.['@name']?.toLowerCase() === process.env.NUXT_DEFAULT_LANGUAGE) || detectedRegionLangs[0])?.['@name'] || process.env.NUXT_DEFAULT_LANGUAGE

    currentLanguageName = (
      (
        detectedRegionLangs.find(lang => lang?.['@name']?.toLowerCase() === location?.countryCode?.toLowerCase()) ||
        detectedRegionLangs.find(lang => lang?.['@name']?.toLowerCase() === defaultRegionLang) ||
        detectedRegionLangs.find(lang => lang?.['@name']?.toLowerCase() === fallbackLanguage) ||
        detectedRegionLangs[0]
      )?.['@name'] || defaultRegionLang
    ).toLowerCase()

    const queryParams = route.fullPath.split('?')?.[1]
    const newPath = `/${currentRegionName}/${currentLanguageName}${queryParams?.length ? (route.path?.endsWith('/') ? route.path.slice(0, -1) : route.path) + '?' + queryParams : (route.path?.endsWith('/') ? route.path.slice(0, -1) : route.path)}`
    console.log(`Redirect from ${route.path} to ${newPath}`)
    return redirect(newPath)
  } else if (route.path.startsWith(`/${currentRegionName}`) && !route.path.startsWith(`/${currentRegionName}/${currentLanguageName}`)) {
    const queryParams = route.fullPath.split('?')?.[1]
    const urlWithLang = route.fullPath.replace(new RegExp(`/${currentRegionName}($|/?)`), `/${currentRegionName}/${currentLanguageName || fallbackLanguage}$1`).split('?')?.[0]

    const newPath = `${queryParams?.length ? (urlWithLang?.endsWith('/') ? urlWithLang.slice(0, -1) : urlWithLang) + '?' + queryParams : (urlWithLang?.endsWith('/') ? urlWithLang.slice(0, -1) : urlWithLang)}`
    console.log(`Redirect from ${route.path} to ${newPath}`)
    return redirect(newPath)
  }
}
