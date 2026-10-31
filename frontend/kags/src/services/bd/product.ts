import type {AxiosResponse} from 'axios'
import http from '@/utils'
import api from '@/api/apiUrls'
import store from '@/store'

export const product = {
  fetchDetails,
  fetchFaultCodesAutocomplete,
  fetchMaintenanceSchedule,
  fetchSoftware,
  fetchMachinePassword,
  updateMachinePassword,
  validateSerial,
  fetchBulletins
}

function promiseWrapper(_promise: Promise<AxiosResponse<any>>, _sn: string = store.getters.getCurrentSerial + '', _handleError = false): Promise<AxiosResponse<any>> {
  return new Promise<AxiosResponse<any>>((resolve, reject) => {
    _promise.then(r => {
      if (_sn === store.getters.getCurrentSerial) {
        if (_handleError) {
          if (r.data.error) reject(r.data.error)
          else if (r.data.errorMessage) {
            const _e = JSON.parse(r.data.errorMessage)
            reject({'code': _e.statusCode || '', 'message': _e.errorMessage})
          }
        }
        resolve(r)
      } else {
        reject({isCancelled: true})
      }
    }).catch(e => {
      if (_sn === store.getters.getCurrentSerial) reject(e.response ? e.response.data : {'code': e.status, 'message': e})
      else reject({isCancelled: true})
    })
  })
}

function fetchDetails(_sn: string): Promise<AxiosResponse<any>> {
  return promiseWrapper(http.get(api.PRODUCTS_DETAILS.replace('{serial}', _sn)), _sn)
}

function validateSerial(_sn: string): Promise<AxiosResponse<any>> {
  return promiseWrapper(http.get(api.PRODUCTS_DETAILS.replace('{serial}', _sn)), store.getters.getCurrentSerial, true)
}

function fetchFaultCodesAutocomplete(_codePrefix: string): Promise<AxiosResponse<any>> {
  return http.get(api.FAULT_CODES_AUTOCOMPLETE.replace('{code_prefix}', _codePrefix))
}

function fetchSoftware(_sn: string): Promise<AxiosResponse<any>> {
  return promiseWrapper(http.get(api.MACHINE_SOFTWARE.replace('{serial}', _sn)), _sn)
}

function fetchMaintenanceSchedule(_sn: string): Promise<AxiosResponse<any>> {
  return promiseWrapper(http.get(api.SERVICE_MAINTENANCE_DETAILS.replace('{serial}', _sn)), _sn)
}

function fetchMachinePassword(_sn: string): Promise<AxiosResponse<any>> {
  return promiseWrapper(http.get(api.PRODUCT_PASSWORD.replace('{serial}', _sn)), _sn)
}

function updateMachinePassword(_sn: string, _panel: string): Promise<AxiosResponse<any>> {
  return promiseWrapper(http.put(api.PRODUCT_PASSWORD_UPDATE.replace('{serial}', _sn).replace('{panel}', _panel)))
}

function fetchBulletins(_sn: string, params?: Record<string, unknown>): Promise<AxiosResponse<any>> {
  return promiseWrapper(http.get(api.BULLETIN_DETAILS.replace('{serial}', _sn), {params}))
}
