import type {AxiosResponse} from 'axios'
import http from '@/utils'
import api from '@/api/apiUrls'
import store from '@/store'
import qs from 'qs'

export const documents = {
  fetchSchematics,
  fetchSchematicDetails,
  fetchDocuments,
  fetchDocumentDownloadLink,
  fetchManuals,
  fetchServiceLetters,
  fetchSoftware
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

function fetchSchematics(_sn: string): Promise<AxiosResponse<any>> {
  return promiseWrapper(http.get(api.SCHEMATICS_LIST.replace('{serial}', _sn)), _sn, true)
}

function fetchSchematicDetails(_sn: string, _schematicId: string): Promise<AxiosResponse<any>> {
  return promiseWrapper(http.get(api.SCHEMATICS_DETAILS.replace('{serial}', _sn).replace('{schematic_id}', _schematicId)), _sn)
}

function fetchDocuments(_params: Record<string, unknown>): Promise<any> {
  return new Promise<any>((resolve, reject) => {
    http.get(api.SERVICE_LIBRARY_DOCUMENTS, {
      params: _params,
      paramsSerializer: params => qs.stringify(params, {arrayFormat: 'comma'})
    }).then(({data}) => {
      if (data.error) reject(data.error)
      resolve(data)
    }).catch(r => {
      reject(r.response ? r.response.data : {'code': '', 'message': r})
    })
  })
}

function fetchDocumentDownloadLink(_documentId: string): Promise<any> {
  return new Promise<any>((resolve, reject) => {
    http.get(api.SERVICE_LIBRARY_DOCUMENT_DOWNLOAD.replace('{document_id}', _documentId)).then(r => {
      resolve(r.data.download_link)
    }).catch(r => {
      reject(r.response ? r.response.data : {'code': '', 'message': r})
    })
  })
}

function fetchManuals(_sn: string): Promise<AxiosResponse<any>> {
  return promiseWrapper(http.get(api.SERVICE_LIBRARY_MANUALS.replace('{serial}', _sn)), _sn, true)
}

function fetchServiceLetters(_fieldModCode: string): Promise<any> {
  return new Promise<any>((resolve, reject) => {
    http.get(api.SERVICE_LIBRARY_LETTERS.replace('{field_mod_code}', _fieldModCode)).then(r => {
      if (r.data.error) reject(r.data.error)
      if (r.data && r.data.download_link) {
        resolve(r.data.download_link)
      } else {
        reject()
      }
    }).catch(r => {
      reject(r.response ? r.response.data : {'code': '', 'message': r})
    })
  })
}

function fetchSoftware(software: string, locale: string): Promise<AxiosResponse<any>> {
  return http.get(api.LATEST_SOFTWARE.replace('{software}', software), {
    params: {locale}
  })
}
