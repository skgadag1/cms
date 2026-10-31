/*global jest*/
import Vue from 'vue'
import Vuex from 'vuex'
import state from './../initial-state'

Vue.use(Vuex)

export const getters = {
  isEmpty: jest.fn().mockReturnValue(false),
  getCurrentAbbreviation: jest.fn().mockReturnValue(null),
  getCurrentMachine: jest.fn().mockReturnValue('ALJ812464'),
  getCurrentUser: jest.fn().mockReturnValue('{ name: "test", email: email@example.com, id: 123 }'),
  getCurrentUserKey: jest.fn().mockReturnValue('email@example.com'),
  getCurrentMachineError: jest.fn().mockReturnValue(false),
  getSerials: jest.fn().mockReturnValue('{ current: [], recent: [] }'),
  getTroubleshootingKeyword: jest.fn().mockReturnValue(''),
  getTroubleshootingSearchType: jest.fn().mockReturnValue('true'),
  getFaultCodesKeyword: jest.fn().mockReturnValue(''),
  getCurrentSerial: jest.fn().mockReturnValue('testSerial123'),
  isFaultCodesAuthorized: jest.fn().mockReturnValue(true),
  isCurrentMachineAuthorized: jest.fn().mockReturnValue(true),
  isCurrentMachineValidSubscription: jest.fn().mockReturnValue(true),
  isCurrentMachineConnected: jest.fn().mockReturnValue(true),
  getCurrentMachineLoading: jest.fn().mockReturnValue(false),
  getFeatureFlag: jest.fn().mockReturnValue(() => false),
  areFeatureFlagsResolved: jest.fn().mockReturnValue(true)
}

export const mutations = {
  saveKeyword() {},
  activateSerialNumber() {},
  saveTroubleshootingKeyword() {}
}

export const actions = {
  fetchMachine() {},
  fetchMachineAuth() {},
  fetchMachineConnection() {},
  fetchMachineDetails() {},
  fetchConcepts() {},
  fetchMachinePassword() {},
  fetchFeatureFlags() {}
}

// eslint-disable-next-line no-underscore-dangle
export function __createMocks(custom = {getters: {}, mutations: {}, actions: {}, state: {}}) {
  const mockGetters = Object.assign({}, getters, custom.getters)
  const mockMutations = Object.assign({}, mutations, custom.mutations)
  const mockActions = Object.assign({}, actions, custom.actions)
  const mockState = Object.assign({}, state, custom.state)

  return {
    getters: mockGetters,
    mutations: mockMutations,
    actions: mockActions,
    state: mockState,
    store: new Vuex.Store({
      getters: mockGetters,
      mutations: mockMutations,
      actions: mockActions,
      state: mockState
    })
  }
}

export const store = __createMocks().store
