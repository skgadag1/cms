import {MACHINE_SAVE, MACHINE_AUTH_SAVE, MACHINE_CONN_SAVE, CONCEPT_SAVE, SERIAL_UPDATE, FEATURES_SAVE} from './mutations'
import {bdService, contentService} from '@/services'

export default {
  fetchMachine({commit, getters}) {
    const _s = getters.getCurrentSerial
    if (!_s || _s === 'null') return

    commit(MACHINE_SAVE, {
      error: null,
      loading: true,
      sn: _s
    })
    const _u = getters.getCurrentUser
    // get IoT data
    bdService.fetchMachineData(_s).then(r => {
      if (r.status === 200) {
        commit(MACHINE_SAVE, {
          data: r.data,
          error: null,
          loading: false,
          sn: _s
        })
      }
    }).catch((error) => {
      let message, code
      if (error.response) {
        message = error.response.data.error_message || error.response.data.errorMessage || (error.response.data.error && error.response.data.error.message) || error.toString()
        code = parseInt(error.response.data.error_code || error.response.data.errorCode || (error.response.data.error && error.response.data.error.code) || error.response.status)
      }
      commit(MACHINE_SAVE, {
        data: {},
        error: {message, code},
        loading: false,
        sn: _s
      })
    })

    // get Subscription data
    if (!_u.internal && _u.companyId) {
      let assetData = {}
      bdService.fetchSubscription(_u.companyId, _s).then(r => {
        if (r.status === 200) assetData = r.data
      }).catch(() => {
        // swallow fetch failures (e.g. 404) — no subscription/asset data
        assetData = {}
      }).finally(() => {
        commit(MACHINE_SAVE, {
          sn: _s,
          assetData: assetData
        })
      })
    }
  },
  fetchMachinePassword({commit, getters}) {
    const _s = getters.getCurrentSerial
    bdService.fetchMachinePassword(_s).then(r => {
      if (r.status === 200) {
        commit(MACHINE_SAVE, {
          sn: _s,
          password: r.data
        })
      }
    }).catch(() => {
      // swallow fetch failures (e.g. 404) — no password available
    })
  },
  fetchMachineDetails({commit, getters}) {
    const _s = getters.getCurrentSerial
    // get product data
    if (_s) {
      bdService.fetchDetails(_s).then(({data}) => {
        if (data.serial_number === _s) {
          commit(SERIAL_UPDATE, data)
        }
      }).catch(() => {
      })
    }
  },
  fetchMachineAuth({commit, getters}) {
    const _s = getters.getCurrentSerial
    const _u = getters.getCurrentUser
    if (_u.internal === false && _u.companyId > 0) {
      let assetData = {}
      let authorized = false
      bdService.fetchSubscription(_u.companyId, _s).then(r => {
        if (r.status === 200) {
          assetData = r.data
          authorized = true
        }
      }).catch(() => {
        // swallow fetch failures (e.g. 404) — treated as unauthorized / no data
        assetData = {}
        authorized = false
      }).finally(() => {
        commit(MACHINE_SAVE, {
          sn: _s,
          assetData: assetData
        })
        commit(MACHINE_AUTH_SAVE, {val: authorized, sn: _s})
      })
    } else {
      commit(MACHINE_AUTH_SAVE, {val: true, sn: _s})
    }
  },
  fetchMachineConnection({commit, getters}) {
    const _s = getters.getCurrentSerial
    let connected = false
    if (!_s || _s === 'null') return

    bdService.fetchMachineData(_s).then(r => {
      const _key = getters.getCurrentUserKey
      connected = _key && getters.getSerials && getters.getSerials.current.filter(x => x.active).length
        ? getters.getSerials.current.filter(x => x.active)[0].is_connectable
        : false
      connected = r.data && r.data.networkType === '4G' && connected
    }).catch(() => {
      // swallow fetch failures (e.g. 404) — treated as "not connected"
      connected = false
    }).finally(() => {
      commit(MACHINE_CONN_SAVE, {sn: _s, val: connected})
    })
  },
  fetchFeatureFlags({commit, getters}) {
    const _s = getters.getCurrentSerial
    if (!_s || _s === 'null') return Promise.resolve()

    return bdService.fetchFeatureFlags(_s).then(r => {
      // ignore stale responses if the serial changed while in flight
      if (getters.getCurrentSerial !== _s) return
      commit(FEATURES_SAVE, {flags: r.status === 200 ? r.data : [], sn: _s})
    }).catch(() => {
      if (getters.getCurrentSerial === _s) commit(FEATURES_SAVE, {flags: [], sn: _s})
    })
  },
  fetchConcepts({commit, getters}, cn) {
    const concepts = getters.getConcepts
    let _l = {}
    if (!Object.prototype.hasOwnProperty.call(concepts, cn) || (concepts[cn] && concepts[cn]._expires && concepts[cn]._expires < Date.now() || false)) {
      commit(CONCEPT_SAVE, {cn, _expires: Date.now() + 1000}) //wait 1 sec before loading again
      contentService.fetchConcepts(cn).then(r => {
        if (r.data) {
          _l = JSON.parse(JSON.stringify(r.data))
          _l._expires = (6 * 60 * 60 * 1e3) + Date.now() // 6 hours cache for concepts
        } else {
          _l = {error: 'No labels for the given concept: ' + cn, _expires: Date.now()}
        }
      }).catch(() => {
        _l = {error: 'Error while fetching translations', _expires: Date.now()}
      }).finally(() => {
        commit(CONCEPT_SAVE, {cn, data: _l})
      })
    }
  }
}
