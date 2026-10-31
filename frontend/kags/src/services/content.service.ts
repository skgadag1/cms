import type {AxiosResponse} from 'axios'
import http from '@/utils'
import api from '@/api/apiUrls'

export const contentService = {
  fetchTranslations,
  fetchConcepts,
  fetchSitemap,
  fetchSafetyDocs
}

function fetchTranslations(_path: string): Promise<AxiosResponse<any>> {
  return http.get(api.TRANSLATIONS.replace('{path}', _path))
}

function fetchConcepts(_path: string): Promise<AxiosResponse<any>> {
  return http.get(api.DP_CONCEPTS.replace('{path}', _path))
}

function fetchSitemap(): Promise<AxiosResponse<any>> {
  return http.get(api.SITEMAP)
}

function fetchSafetyDocs(): Promise<AxiosResponse<any>> {
  return http.get(api.SAFETY_DOCS)
}
