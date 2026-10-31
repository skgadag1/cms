import type {AxiosRequestConfig, AxiosResponse} from 'axios'
import http from '@/utils'
import api from '@/api/apiUrls'
import store from '@/store'

export const troubleshooting = {
  fetchTroubleshootingGuides,
  fetchTroubleshootingTopGuides,
  fetchTroubleshootingSessionList,
  troubleshootingSessionStart,
  troubleshootingSessionResume,
  troubleshootingSessionSummary,
  troubleshootingSessionSummaryPDF
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
      if (_sn === store.getters.getCurrentSerial) reject(e.response ? e.response.data : {'code': '', 'message': e})
      else reject({isCancelled: true})
    })
  })
}

function fetchTroubleshootingGuides(_sn: string, _postParameters: AxiosRequestConfig): Promise<AxiosResponse<any>> {
  return promiseWrapper(http.get(api.TROUBLESHOOTING_GUIDES_LIST.replace('{serial}', _sn), _postParameters), _sn)
}

function fetchTroubleshootingTopGuides(_sn: string, _postParameters: AxiosRequestConfig): Promise<AxiosResponse<any>> {
  return promiseWrapper(http.get(api.TROUBLESHOOTING_TOP_GUIDES_LIST.replace('{serial}', _sn), _postParameters), _sn)
}

function fetchTroubleshootingSessionList(_sn: string): Promise<AxiosResponse<any>> {
  // session state changes frequently (new/resumed/closed sessions), so this must
  // bypass the shared axios GET cache (see src/utils.js) rather than serve stale data
  return promiseWrapper(http.get(api.TROUBLESHOOTING_SESSION_LIST.replace('{serial}', _sn), {cache: false}), _sn)
}

function troubleshootingSessionStart(_sn: string, _guideId: string): Promise<AxiosResponse<any>> {
  return promiseWrapper(http.get(api.TROUBLESHOOTING_SESSION_START.replace('{serial}', _sn).replace('{guide_id}', _guideId)), _sn)
}

function troubleshootingSessionResume(_sessionId: string): Promise<AxiosResponse<any>> {
  return promiseWrapper(http.get(api.TROUBLESHOOTING_SESSION_RESUME.replace('{session_id}', _sessionId)))
}

function troubleshootingSessionSummary(_sessionId: string): Promise<AxiosResponse<any>> {
  return promiseWrapper(http.get(api.TROUBLESHOOTING_SESSION_SUMMARY.replace('{session_id}', _sessionId)))
}

async function troubleshootingSessionSummaryPDF(_sessionId: string): Promise<AxiosResponse<any>> {
  return promiseWrapper(http.get(api.TROUBLESHOOTING_SESSION_SUMMARY_PDF.replace('{session_id}', _sessionId), {
    responseType: 'blob'
  }))
}
