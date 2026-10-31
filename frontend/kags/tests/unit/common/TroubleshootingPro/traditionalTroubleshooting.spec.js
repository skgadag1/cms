import {shallowMount} from '@vue/test-utils'
import flushPromises from 'flush-promises'
import TraditionalTroubleshooting from '@/components/common/TroubleshootingPro/TraditionalTroubleshooting'
import store from '@/store'
import config from '@/mixins/commonMixin'
import Vue from 'vue'
import http from '@/utils'

let $_wrapper

const resumeModalStub = {
  template: '<div />',
  methods: {show: jest.fn(), hide: jest.fn()}
}

const openSessionsStub = {
  template: '<div />',
  methods: {refresh: jest.fn()},
  data: () => ({sessionItems: []})
}

function mountComponent() {
  return shallowMount(TraditionalTroubleshooting, {
    store,
    mixin: config(Vue),
    stubs: {
      ResumeExistingSessionModal: resumeModalStub,
      OpenSessionsForSerial: openSessionsStub
    }
  })
}

beforeEach(() => {
  // safe default so the commonMixin's own translation fetch (also routed through http.get) doesn't crash
  http.get = jest.fn().mockResolvedValue({data: []})
  window.open = jest.fn()
})

afterEach(async () => {
  await flushPromises()
  if ($_wrapper) $_wrapper.destroy()
  jest.restoreAllMocks()
})

describe('components/common/TroubleshootingPro/TraditionalTroubleshooting.vue', () => {
  it('is vue instance', () => {
    $_wrapper = mountComponent()
    expect($_wrapper.vm).toBeTruthy()
  })

  it('shows the Resume modal instead of starting a session when a matching open session exists', async () => {
    $_wrapper = mountComponent()
    await flushPromises()
    await $_wrapper.setData({openSessions: [{session_id: 's1', title: 'W3276'}]})

    const showSpy = jest.spyOn($_wrapper.vm.$refs.resumeModal, 'show')

    $_wrapper.vm.startSession({tree_id: 't1', title: 'W3276'})

    expect(showSpy).toHaveBeenCalled()
    expect($_wrapper.vm.pendingItem).toEqual({tree_id: 't1', title: 'W3276'})
    expect(window.open).not.toHaveBeenCalled()
  })

  it('starts the session directly when no matching open session exists', async () => {
    $_wrapper = mountComponent()
    await flushPromises()
    await $_wrapper.setData({openSessions: [{session_id: 's1', title: 'M7974'}]})

    http.get = jest.fn().mockResolvedValue({data: {url: 'https://example.test/session'}})

    $_wrapper.vm.startSession({tree_id: 't1', title: 'W3276'})
    await flushPromises()

    expect(window.open).toHaveBeenCalledWith('https://example.test/session')
  })

  it('refreshes the open sessions list 3 seconds after a new session starts', async () => {
    jest.useFakeTimers()
    $_wrapper = mountComponent()
    await flushPromises()

    http.get = jest.fn().mockResolvedValue({data: {url: 'https://example.test/session'}})
    const refreshSpy = jest.spyOn($_wrapper.vm.$refs.openSessionsRef, 'refresh')

    $_wrapper.vm.proceedStartSession({tree_id: 't1', title: 'W3276'})
    await flushPromises()

    expect(refreshSpy).not.toHaveBeenCalled()
    jest.advanceTimersByTime(3000)
    expect(refreshSpy).toHaveBeenCalled()

    jest.useRealTimers()
  })
})
