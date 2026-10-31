import type {AxiosResponse} from 'axios'
import api from '@/api/apiUrls'
import http from '@/utils'

export const notificationsService = {
  fetchActiveNotifications: async (appId: string): Promise<any> => {
    // Fix for CORS - Cache-Control header is not allowed
    delete http.defaults.headers['Cache-Control']

    return promiseWrapper(http.get(api.ACTIVE_NOTIFICATIONS.replace('{appId}', appId)))
  }
}

function promiseWrapper(_promise: Promise<AxiosResponse<any>>): Promise<any> {
  return new Promise<any>((resolve, reject) => {
    _promise.then(({data}) => {
      resolve(data)
    }).catch(e => {
      if (e.response && e.response.status === 404) resolve([])
      else reject(e)
    })
  })
}

export default notificationsService
