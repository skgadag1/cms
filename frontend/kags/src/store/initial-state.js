export const initialState = {
  ui: {
    serials: {},
    currentMachine: {},
    troubleshooting: {
      keyword: '',
      searchType: 'true'
    },
    faultcodes: {
      keyword: ''
    },
    authorisation: {},
    features: [],
    featuresSerial: null
  },
  showConceptNames: false,
  user: {},
  concepts: {},
  version: 1.9
}

export default initialState
