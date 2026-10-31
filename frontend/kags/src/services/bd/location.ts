import type {AxiosResponse} from 'axios'
import http from '@/utils'
import api from '@/api/apiUrls'
import store from '@/store'

export const location = {
  fetchLocationHistory,
  fetchLastKnownLocation
}

function fetchLocationHistory(_sn: string, _params: Record<string, unknown>): Promise<AxiosResponse<any>> {
  return http.get(api.LOCATION_DATA.replace('{serial}', _sn), {params: _params})
}

function fetchLastKnownLocation(_sn: string): Promise<any | false> {
  let endDateTime = new Date() // now
  let searchRangeDays = 6 //start with 1 week
  let calculatedStart = new Date(endDateTime.getTime())
  calculatedStart.setDate(calculatedStart.getDate() - searchRangeDays)
  return new Promise<any | false>((resolve, reject) => {
    (async function () {
      let finished = false
      while (!finished) {
        const r = await http.get(api.LOCATION_DATA.replace('{serial}', _sn), {params: {limit: 2000, start_date_time: calculatedStart, end_date_time: endDateTime, includeAddress: true}})

        if (_sn === store.getters.getCurrentSerial) {
          if (r.data && r.data.length > 0) {
            if (r.data && r.data.length <= 1999 || searchRangeDays === 1) { //data length is within 1 and 1999
              resolve(r.data.sort((a: {startTs: string | number}, b: {startTs: string | number}) => new Date(a.startTs).getTime() - new Date(b.startTs).getTime()).pop())
              this.finished = true
            } else { // too many items, narrowing search
              searchRangeDays = searchRangeDays === 3 ? 1 : searchRangeDays / 2 //reduce days from 6 to 3, then 1
            }
          } else {
            if (calculatedStart.getTime() <= 0) { // no items since 1970
              resolve(false)
              this.finished = true
            }
            //not enough items, expand search
            endDateTime = new Date(calculatedStart.getTime())
            searchRangeDays *= 2
          }
          // calculate new startDate and try again
          calculatedStart.setDate(calculatedStart.getDate() - searchRangeDays)
        } else {
          this.finished = true
          reject({isCancelled: true})
        }
      }
    })().catch(e => {
      if (_sn === store.getters.getCurrentSerial) reject(e)
      else reject({isCancelled: true})
    })
  })
}
