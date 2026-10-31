import type {AxiosResponse, CancelToken} from 'axios'
import http from '@/utils'
import axios from 'axios'
import api from '@/api/apiUrls'
import qs from 'qs'
import {FAULTCODES_AUTH_SAVE} from '@/store/mutations'
import store from '@/store'

const MAX_LIMIT = 100

// Cancels in-flight machine requests when the selected serial changes, so the
// previous serial's XHRs are aborted immediately instead of hanging pending.
let _machineRequestSerial: string | null = null
let _machineCancelSource: ReturnType<typeof axios.CancelToken.source> | null = null
function getCancelToken(sn: string): CancelToken {
  if (sn !== _machineRequestSerial) {
    if (_machineCancelSource) _machineCancelSource.cancel('serial changed')
    _machineCancelSource = axios.CancelToken.source()
    _machineRequestSerial = sn
  }
  return _machineCancelSource.token
}

export const machine = {
  fetchFaultCodes,
  fetchMachineData,
  fetchFieldMods,
  getWarrantyPlans,
  getBuiltOptions,
  fetchSubscription,
  fetchWarrantyInclusions,
  fetchWarrantyArticles
}

/**
 * @param {String} _sn Serial number.
 * @param {Number} _limit items count to fetch.
 * @param {String} _lastEvaluatedKey Key from last query
 * @param {Object} _params Filters to be applied.
 * @param {String} _sorting string.
 *
 * @return {Promise} Returns a promise with fetched codes.
 */
function fetchFaultCodes(_sn: string, _limit: number, _offset: number, _params: Record<string, unknown>, _sorting = '-incidentStart', _paginationLimit?: number): Promise<any> {
  _paginationLimit = _paginationLimit || MAX_LIMIT

  if (_limit > _paginationLimit) { // fetch codes in loop
    return new Promise<any>((resolve, reject) => {
      let allCodes: any[] = [];
      (async function () {
        let finished = false
        let offset = _offset
        let remained = null

        while (!finished) {
          remained = Math.min(_limit - allCodes.length, _paginationLimit)
          if (remained && remained > 0) {
            const r = await apiCall(_sn, remained, offset, _params, _sorting)
            if (r.items && r.items.length) {
              allCodes = allCodes.concat(r.items)
              offset += r.items.length
              if (r.items.length < remained) finished = true
            } else finished = true
          } else finished = true
        }
        resolve({items: allCodes})
      })().catch(e => reject(e))
    })
  } else return apiCall(_sn, _limit, _offset, _params, _sorting) // normal fetch
}

function apiCall(_sn: string, _limit: number, _offset: number, _params: Record<string, unknown>, _sorting = '-incidentStart'): Promise<any> {
  return new Promise<any>((resolve, reject) => {
    const combinedParams = {
      sort_key: _sorting,
      offset: _offset,
      ..._params,
      limit: _limit
    }
    http.get(api.FAULT_CODES_LIST.replace('{serial}', _sn.toLowerCase()), {
      params: combinedParams,
      paramsSerializer: params => qs.stringify(params, {arrayFormat: 'repeat'}),
      cancelToken: getCancelToken(_sn)
    }).then(r => {
      if (_sn === store.getters.getCurrentSerial) {
        store.commit(FAULTCODES_AUTH_SAVE, {val: true, sn: _sn}) // authorized
        resolve(r.data)
      } else {
        reject({isCancelled: true})
      }
    }).catch(e => {
      // request aborted because the serial changed -> treat as cancelled
      if (axios.isCancel(e) || _sn !== store.getters.getCurrentSerial) {
        reject({isCancelled: true})
        return
      }
      if (e.response && e.response.data && e.response.data.errorCode === 404 && e.response.data.errorMessage && e.response.data.errorMessage.includes('Machine with key')) store.commit(FAULTCODES_AUTH_SAVE, {
        val: false,
        sn: _sn
      }) // unauthorized
      else store.commit(FAULTCODES_AUTH_SAVE, {val: true, sn: _sn})
      let message, code
      if (e.response) {
        message = e.response.data.error_message || e.response.data.errorMessage || (e.response.data.error && e.response.data.error.message) || e.toString()
        code = parseInt(e.response.data.error_code || e.response.data.errorCode || (e.response.data.error && e.response.data.error.code) || e.response.status)
      }
      reject({message, code})
    })
  })
}

function fetchMachineData(_sn: string): Promise<AxiosResponse<any>> {
  return http.get(api.MACHINE_DATA.replace('{serial}', _sn))
}

function promiseWrapper(_promise: Promise<AxiosResponse<any>>, _sn: string = store.getters.getCurrentSerial + ''): Promise<any> {
  return new Promise<any>((resolve, reject) => {
    _promise.then(({data}) => {
      if (_sn === store.getters.getCurrentSerial) {
        resolve(data)
      } else {
        reject({isCancelled: true})
      }
    }).catch(e => {
      if (axios.isCancel(e) || _sn !== store.getters.getCurrentSerial) {
        reject({isCancelled: true})
        return
      }
      if (e.response && e.response.data && e.response.data.error_code === '404' && e.response.data.error_message === 'No field modifications found.') resolve([])
      else reject(e.response ? {'code': e.response.data.error_code, 'message': e.response.data.error_message} : {'code': '', 'message': e})
    })
  })
}

function fetchFieldMods(sn: string, campaignCode: string): Promise<any> {
  return promiseWrapper(http.get(api.SERVICE_FIELD_MODIFICATIONS.replace('{serial}', sn).replace('{campaign_code}', campaignCode), {
    cancelToken: getCancelToken(sn)
  }), sn)
}

function getWarrantyPlans(sn: string, _params: Record<string, unknown> = {}): Promise<any> {
  return promiseWrapper(http.get(api.WARRANTY_PLANS.replace('{serial}', sn), {
    params: _params,
    paramsSerializer: params => qs.stringify(params, {arrayFormat: 'repeat'})
  }), sn)
}

function getBuiltOptions(sn: string): Promise<any> {
  return promiseWrapper(http.get(api.OPTIONS_LIST.replace('{serial}', sn)), sn)
}

function fetchSubscription(_accountId: string, _sn: string): Promise<AxiosResponse<any>> {
  return http.get(api.ACCOUNT_SUBSCRIPTION.replace('{account}', _accountId).replace('{serial}', _sn.toLowerCase()))
}

function fetchWarrantyInclusions(sn: string, plan_code: string): Promise<AxiosResponse<any>> {
  return http.get(api.WARRANTY_INCLUSIONS.replace('{serial}', sn), {params: {plan_code}})
}

function fetchWarrantyArticles(language: string): Promise<AxiosResponse<any>> {
  const url = api.WARRANTY_ARTICLES.replace('{language}', language)

  return http.get(url)
}
