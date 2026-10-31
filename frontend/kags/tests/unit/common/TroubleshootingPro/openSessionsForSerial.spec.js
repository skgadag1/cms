import {shallowMount, mount} from '@vue/test-utils'
import flushPromises from 'flush-promises'
import OpenSessionsForSerial from '@/components/common/TroubleshootingPro/OpenSessionsForSerial'
import store from '@/store'
import config from '@/mixins/commonMixin'
import Vue from 'vue'
import {bdService} from '@/services'

let $_wrapper

function mountComponent(mountFn = shallowMount) {
  return mountFn(OpenSessionsForSerial, {
    store,
    mixin: config(Vue),
    computed: {
      getCurrentSerial: () => 'ALJ812464'
    }
  })
}

afterEach(async () => {
  await flushPromises()
  if ($_wrapper) $_wrapper.destroy()
  jest.restoreAllMocks()
})

describe('components/common/TroubleshootingPro/OpenSessionsForSerial.vue', () => {
  it('fetches and filters to only In Progress / Paused sessions', async () => {
    jest.spyOn(bdService, 'fetchTroubleshootingSessionList').mockResolvedValue({
      data: [
        {session_id: '1', title: 'W3276', state: 'In Progress', createdAt: '2020-01-01', isResumingAllowed: true},
        {session_id: '2', title: 'M7974', state: 'Paused', createdAt: '2020-01-01', isResumingAllowed: false},
        {session_id: '3', title: 'M0909', state: 'Finished New Solution', createdAt: '2020-01-01', isResumingAllowed: true},
        {session_id: '4', title: 'M0618', state: 'Canceled', createdAt: '2020-01-01', isResumingAllowed: true}
      ]
    })

    $_wrapper = mountComponent()
    await flushPromises()

    expect(bdService.fetchTroubleshootingSessionList).toHaveBeenCalledWith('ALJ812464')
    expect($_wrapper.vm.sessionItems.length).toBe(2)
    expect($_wrapper.vm.sessionItems.map(s => s.session_id)).toEqual(['1', '2'])
  })

  it('paginates at 5 per page', async () => {
    const sessions = Array.from({length: 12}, (_, i) => ({
      session_id: 'sess-' + i,
      title: 'W3276',
      state: 'In Progress',
      createdAt: '2020-01-01',
      isResumingAllowed: true
    }))
    jest.spyOn(bdService, 'fetchTroubleshootingSessionList').mockResolvedValue({data: sessions})

    $_wrapper = mountComponent()
    await flushPromises()

    expect($_wrapper.vm.sessionItems.length).toBe(12)
    expect($_wrapper.vm.displayedSessions.length).toBe(5)

    $_wrapper.vm.currentPage = 3
    await $_wrapper.vm.$nextTick()
    expect($_wrapper.vm.displayedSessions.length).toBe(2)
  })

  it('emits sessions-updated whenever the session list changes', async () => {
    jest.spyOn(bdService, 'fetchTroubleshootingSessionList').mockResolvedValue({
      data: [{session_id: '1', title: 'W3276', state: 'In Progress', createdAt: '2020-01-01', isResumingAllowed: true}]
    })

    $_wrapper = mountComponent()
    await flushPromises()

    expect($_wrapper.emitted('sessions-updated')).toBeTruthy()
    expect($_wrapper.emitted('sessions-updated')[0][0].length).toBe(1)
  })

  it('refresh() re-fetches the session list', async () => {
    jest.spyOn(bdService, 'fetchTroubleshootingSessionList').mockResolvedValue({data: []})
    $_wrapper = mountComponent()
    await flushPromises()

    bdService.fetchTroubleshootingSessionList.mockClear()
    $_wrapper.vm.refresh()
    await flushPromises()

    expect(bdService.fetchTroubleshootingSessionList).toHaveBeenCalledTimes(1)
  })

  it('shows Review and Resume actions gated by isResumingAllowed', async () => {
    jest.spyOn(bdService, 'fetchTroubleshootingSessionList').mockResolvedValue({
      data: [
        {session_id: '1', title: 'W3276', state: 'In Progress', createdAt: '2020-01-01', isResumingAllowed: true},
        {session_id: '2', title: 'M7974', state: 'In Progress', createdAt: '2020-01-01', isResumingAllowed: false}
      ]
    })

    $_wrapper = mountComponent(mount)
    await flushPromises()
    await $_wrapper.vm.$nextTick()

    expect($_wrapper.find('[data-test-id="open-session-item__review__1"]').exists()).toBe(true)
    expect($_wrapper.find('[data-test-id="open-session-item__resume__1"]').exists()).toBe(true)
    expect($_wrapper.find('[data-test-id="open-session-item__review__2"]').exists()).toBe(true)
    expect($_wrapper.find('[data-test-id="open-session-item__resume__2"]').exists()).toBe(false)
  })
})
