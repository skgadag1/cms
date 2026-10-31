import type {AxiosResponse} from 'axios'
import http from '@/utils'
import api from '@/api/apiUrls'

export const serviceUtils = {
  fetchFeatureFlags
}

function fetchFeatureFlags(_sn: string): Promise<AxiosResponse<unknown>> {
  return http.get(api.FEATURE_FLAG.replace('{serial}', _sn))
}
