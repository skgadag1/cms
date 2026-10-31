export const STATE_RESTORE = 'restoreState'
export const SERIAL_ADD = 'addSerialNumber'
export const SERIAL_UPDATE = 'updateSerialNumber'
export const SERIAL_REMOVE = 'removeSerialNumber'
export const SERIAL_ACTIVATE = 'activateSerialNumber'
export const TROUBLESHOOING_KEYWORD_SAVE = 'saveTroubleshootingKeyword'

export const TROUBLESHOOTING_SEARCH_TYPE_SAVE = 'saveTroubleshootingSearchType'
export const FAULTCODES_KEYWORD_SAVE = 'saveFaultCodeKeyword'
export const MACHINE_AUTH_SAVE = 'saveMachineAuth'
export const MACHINE_CONN_SAVE = 'saveMachineConnection'
export const FAULTCODES_AUTH_SAVE = 'saveFaultCodeAuth'
export const USER_SAVE = 'saveUser'
export const MACHINE_SAVE = 'saveMachine'
export const MACHINE_CLEAR = 'clearMachine'
export const CONCEPT_SAVE = 'saveConcept'
export const CONCEPTS_SHOW = 'saveShowConcepts'
export const FEATURES_SAVE = 'saveFeatures'

export default {
  [STATE_RESTORE](state, force = false) {
    const stateRestored = {
      restored: true
    }
    const version = parseFloat(localStorage.getItem('version')) || 0

    if (force || (!state.restored && version === state.version)) {
      for (const prop in state) {
        if (Object.prototype.hasOwnProperty.call(state, prop) && localStorage.getItem(prop)) stateRestored[prop] = JSON.parse(localStorage.getItem(prop))
      }
    }

    // Keep the live (in-memory) featuresSerial rather than the persisted one.
    // - fresh load: live state is the initial `null`, so the gate holds until the
    //   current serial's flags resolve (no legacy flash, no stale persisted flags)
    // - mid-session STATE_RESTORE (cross-tab sync via `storageUpdated`): we must
    //   NOT wipe an already-resolved featuresSerial, otherwise the details panel
    //   flickers (unmounts then remounts) on every auth/connection mutation.
    if (stateRestored.ui) stateRestored.ui = {...stateRestored.ui, featuresSerial: state.ui.featuresSerial}

    this.replaceState(Object.assign({}, state, stateRestored))
  },
  [SERIAL_UPDATE](state, _s) {
    const _key = this.getters.getCurrentUserKey
    if (_key) {
      // current
      const index = state.ui.serials[_key].current.findIndex(x => x.serial_number === _s.serial_number)
      if (index >= 0) {
        _s.active = state.ui.serials[_key].current[index].active
        state.ui.serials[_key].current[index] = _s
      }

      // recent
      const recentIndex = state.ui.serials[_key].recent.findIndex(x => x.serial_number === _s.serial_number)
      if (recentIndex >= 0) {
        _s.active = state.ui.serials[_key].recent[recentIndex].active
        state.ui.serials[_key].recent[recentIndex] = _s
      }
    }
  },
  [SERIAL_ADD](state, _s) {
    const _key = this.getters.getCurrentUserKey
    if (_key) {
      _s.active = true
      if (!this.getters.getSerials) { const x = Object.assign({}, state.ui.serials); x[_key] = {current: [], recent: []}; state.ui.serials = x }
      this.commit(SERIAL_ACTIVATE, _s)

      // current
      if (!state.ui.serials[_key].current.filter(x => x.serial_number === _s.serial_number).length) state.ui.serials[_key].current.push(_s)
      state.ui.serials[_key].current = state.ui.serials[_key].current.slice(-4)

      // recent
      if (!state.ui.serials[_key].recent.filter(x => x.serial_number === _s.serial_number).length) state.ui.serials[_key].recent.push(_s)
      state.ui.serials[_key].recent = state.ui.serials[_key].recent.slice(-5)
    }
  },
  [SERIAL_REMOVE](state, _s) {
    const _key = this.getters.getCurrentUserKey
    if (_key) {
      if (!this.getters.getSerials) { const x = Object.assign({}, state.ui.serials); x[_key] = {current: [], recent: []}; state.ui.serials = x }
      state.ui.serials[_key].current = state.ui.serials[_key].current.filter(x => x.serial_number !== _s.serial_number)
      if (!this.getters.getCurrentSerial && state.ui.serials[_key].current.length) state.ui.serials[_key].current[state.ui.serials[_key].current.length - 1].active = true
    }
  },
  [SERIAL_ACTIVATE](state, _s) {
    const _key = this.getters.getCurrentUserKey
    if (_key) {
      const serial = typeof _s === 'object' ? _s.serial_number : _s
      state.ui.serials[_key].current = state.ui.serials[_key].current.map(x => {
        x.active = x.serial_number === serial
        return x
      })
    }
  },
  [TROUBLESHOOING_KEYWORD_SAVE](state, k) {
    state.ui.troubleshooting.keyword = k
  },
  [TROUBLESHOOTING_SEARCH_TYPE_SAVE](state, s) {
    state.ui.troubleshooting.searchType = s
  },
  [USER_SAVE](state, u) {
    state.user = u
  },
  [MACHINE_SAVE](state, payload) {
    state.ui.currentMachine = {
      ...state.ui.currentMachine,
      [payload.sn]: Object.assign(state.ui.currentMachine[payload.sn] || {}, payload)
    }
  },
  [FAULTCODES_KEYWORD_SAVE](state, k) {
    state.ui.faultcodes.keyword = k
  },
  [FAULTCODES_AUTH_SAVE](state, payload) {
    state.ui.authorisation = {...state.ui.authorisation, [payload.sn]: Object.assign(state.ui.authorisation[payload.sn] || {}, {faultCodesAuthorized: payload.val})}
  },
  [MACHINE_AUTH_SAVE](state, payload) {
    state.ui.authorisation = {...state.ui.authorisation, [payload.sn]: Object.assign(state.ui.authorisation[payload.sn] || {}, {machineAuthorized: payload.val})}
  },
  [MACHINE_CONN_SAVE](state, payload) {
    state.ui.authorisation = {...state.ui.authorisation, [payload.sn]: Object.assign(state.ui.authorisation[payload.sn] || {}, {machineConnected: payload.val})}
  },
  [CONCEPT_SAVE](state, payload) {
    state.concepts = {...state.concepts, [payload.cn]: payload.data}
  },
  [CONCEPTS_SHOW](state, payload) {
    state.showConceptNames = payload
  },
  [FEATURES_SAVE](state, payload) {
    // payload: { flags, sn } — track which serial the flags were resolved for
    state.ui = {...state.ui, features: payload.flags, featuresSerial: payload.sn}
  }
}
