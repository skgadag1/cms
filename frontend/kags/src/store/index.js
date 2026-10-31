import Vuex from 'vuex'
import mutations, {MACHINE_AUTH_SAVE, FAULTCODES_AUTH_SAVE, MACHINE_CONN_SAVE} from './mutations'
import actions from './actions'
import getters from './getters'
import {triggerGlobalStoreSync} from '@/utils'
import initialState from './initial-state'

const store = new Vuex.Store({
  state: initialState,
  mutations,
  actions,
  getters
})

const saveStore = state => {
  for (const prop in state) {
    if (Object.prototype.hasOwnProperty.call(state, prop)) localStorage.setItem(prop, JSON.stringify(state[prop]))
  }
}

// subscribe to save store on change
store.subscribe((mutation, state) => {
  saveStore(state)
  if ([MACHINE_AUTH_SAVE, MACHINE_CONN_SAVE, FAULTCODES_AUTH_SAVE].includes(mutation.type)) triggerGlobalStoreSync()
})

window.addEventListener('storageUpdated', () => {
  store.commit('restoreState', true)
})

export default store
