import {mount} from '@vue/test-utils'
import {BButton} from 'bootstrap-vue'
import ResumeExistingSessionModal from '@/components/common/TroubleshootingPro/ResumeExistingSessionModal'
import store from '@/store'
import config from '@/mixins/commonMixin'
import Vue from 'vue'

let $_wrapper

function buildSession(overrides = {}) {
  return {
    session_id: 'sess-1',
    title: 'W3276',
    description: 'CAN Joystick Information Error',
    createdAt: '2020-01-01',
    state: 'In Progress',
    isResumingAllowed: true,
    ...overrides
  }
}

function mountModal(propsData = {}) {
  return mount(ResumeExistingSessionModal, {
    store,
    mixin: config(Vue),
    propsData: {
      details: {title: 'W3276', short_description: 'CAN Joystick Information Error'},
      sessions: [],
      ...propsData
    }
  })
}

afterEach(() => {
  if ($_wrapper) $_wrapper.destroy()
})

describe('components/common/TroubleshootingPro/ResumeExistingSessionModal.vue', () => {
  it('only shows sessions matching the procedure being started', () => {
    $_wrapper = mountModal({
      sessions: [
        buildSession({session_id: 'match-1', title: 'W3276'}),
        buildSession({session_id: 'other', title: 'M7974'})
      ]
    })

    expect($_wrapper.vm.matchingSessions.length).toBe(1)
    expect($_wrapper.vm.matchingSessions[0].session_id).toBe('match-1')
  })

  it('paginates matching sessions at 5 per page', async () => {
    const sessions = Array.from({length: 12}, (_, i) => buildSession({session_id: 'sess-' + i}))
    $_wrapper = mountModal({sessions})

    expect($_wrapper.vm.displayedSessions.length).toBe(5)

    $_wrapper.vm.currentPage = 3
    await $_wrapper.vm.$nextTick()
    expect($_wrapper.vm.displayedSessions.length).toBe(2)
  })

  it('hides Resume when isResumingAllowed is false', () => {
    $_wrapper = mountModal({sessions: [buildSession({isResumingAllowed: false})]})

    const buttons = $_wrapper.findAllComponents(BButton)
    const buttonTexts = buttons.wrappers.map(b => b.text())
    expect(buttonTexts.some(t => /resume/i.test(t))).toBe(false)
  })

  it('emits start-new-session when "Start New Session" is clicked', async () => {
    $_wrapper = mountModal()
    $_wrapper.vm.show()
    await $_wrapper.vm.$nextTick()

    // b-modal only appends its content to document.body once shown, and portals it
    // there — outside $_wrapper's own DOM tree — so it's queried and clicked directly
    // rather than via a VTU wrapper.
    const startNewButton = document.body.querySelector('[data-test-id="resume-existing-session__start-new"]')
    expect(startNewButton).toBeTruthy()
    startNewButton.click()
    await $_wrapper.vm.$nextTick()

    expect($_wrapper.emitted('start-new-session')).toBeTruthy()
  })
})
