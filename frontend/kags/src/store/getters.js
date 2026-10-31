export default {
  getCurrentUser: state => {
    return state ? state.user : null
  },
  getCurrentUserKey: (state, getters) => {
    // for now email
    return state && getters.getCurrentUser ? getters.getCurrentUser.id : null
  },
  getCurrentUserInternal: (state) => {
    return state && state.user && state.user.internal || false
  },
  getSerials: (state, getters) => {
    const _key = getters.getCurrentUserKey
    return _key && state.ui.serials[_key] ? state.ui.serials[_key] : null
  },
  getConcepts: state => {
    if (typeof state.showConceptNames !== 'undefined' && state.showConceptNames) return {'show': 'concepts'}
    return state ? state.concepts : {}
  },
  getConcept: state => (name) => {
    return state && state.concepts && state.concepts[name] ? state.concepts[name] : {}
  },
  getRecentSerials: (state, getters) => {
    return getters.getSerials ? getters.getSerials.recent.slice(0).reverse() : []
  },
  getCurrentSerial: (state, getters) => {
    const _key = getters.getCurrentUserKey
    return _key && getters.getSerials && getters.getSerials.current.filter(x => x.active).length
      ? getters.getSerials.current.filter(x => x.active)[0].serial_number
      : null
  },
  getCurrentMachine: (state, getters) => {
    const _s = getters.getCurrentSerial
    return state.ui.currentMachine[_s] ? state.ui.currentMachine[_s].data : {}
  },
  getCurrentMachineError: (state, getters) => {
    const _s = getters.getCurrentSerial
    return state.ui.currentMachine[_s] ? state.ui.currentMachine[_s].error : null
  },
  getCurrentMachineLoading: (state, getters) => {
    const _s = getters.getCurrentSerial
    return state.ui.currentMachine[_s] ? state.ui.currentMachine[_s].loading : false
  },
  getMachinePassword: (state, getters) => {
    const _s = getters.getCurrentSerial
    return state.ui.currentMachine[_s] ? state.ui.currentMachine[_s].password : null
  },
  getCurrentAbbreviation: (state, getters) => {
    const _key = getters.getCurrentUserKey
    return _key && getters.getSerials && getters.getSerials.current.filter(x => x.active).length
      ? getters.getSerials.current.filter(x => x.active)[0].abbreviation
      : null
  },
  getTroubleshootingKeyword: state => {
    return state.ui.troubleshooting.keyword
  },
  getTroubleshootingSearchType: state => {
    return state.ui.troubleshooting.searchType
  },
  getFaultCodesKeyword: state => {
    return state.ui.faultcodes.keyword
  },
  isEmpty: (state, getters) => {
    const _key = getters.getCurrentUserKey
    return _key && getters.getSerials && getters.getSerials.current.length
      ? getters.getSerials.current.length === 0
      : true
  },
  isFaultCodesAuthorized: (state, getters) => {
    const _s = getters.getCurrentSerial
    return (state.ui.authorisation[_s] && state.ui.authorisation[_s].faultCodesAuthorized || false) ? state.ui.authorisation[_s].faultCodesAuthorized : false
  },
  isCurrentMachineValidSubscription: (state, getters) => {
    const _s = getters.getCurrentSerial
    const _u = getters.getCurrentUser
    if (_u.internal)
      return true //internal users have always valid subscription
    else
      return state.ui.currentMachine[_s] &&
        state.ui.currentMachine[_s].assetData &&
        state.ui.currentMachine[_s].assetData.subscriptions &&
        state.ui.currentMachine[_s].assetData.subscriptions.length > 0 &&
        state.ui.currentMachine[_s].assetData.subscriptions.find(subs => new Date() < new Date(subs.end_date)) || false
  },
  isCurrentMachineConnected: (state, getters) => {
    const _s = getters.getCurrentSerial
    return state.ui.authorisation[_s] && state.ui.authorisation[_s].machineConnected || false
  },
  isCurrentMachineAuthorized: (state, getters) => {
    const _s = getters.getCurrentSerial
    return state.ui.authorisation[_s] && state.ui.authorisation[_s].machineAuthorized || false
  },
  getRegionalizedFeatures: (state) => {
    const currentRegion = 'NA'
    if (!currentRegion || !state.ui.features || !state.ui.features.length) return {}
    return state.ui.features
      ?.filter(f => f.name.toLowerCase().endsWith(currentRegion.toLowerCase()))
      .reduce((acc, f) => {
        const key = f.name.slice(0, f.name.length - currentRegion.length)
        acc[key] = f.enabled
        return acc
      }, {}) || {}
  },
  getFeatureFlag: (state, getters) => (flagName) => {
    // fail-closed: an unknown/unresolved flag is treated as disabled
    return getters.getRegionalizedFeatures[flagName] ?? false
  },
  areFeatureFlagsResolved: (state) => {
    return state.ui.featuresSerial !== null
  }
}
