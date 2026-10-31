import {mount} from '@vue/test-utils'
import {BButton, BFormInput} from 'bootstrap-vue'
import Feedback from '@/components/Feedback/Feedback'
import config from '@/mixins/commonMixin'
import Vue from 'vue'
import {__createMocks} from '@/store/mocks'
import FeedbackWidgetFooter from '@/components/common/Feedback/FeedbackWidgetFooter'
import {STATE} from '@/components/Feedback/Feedback'
import _survey from '../../../public/mocks/feedback-api/feedback.json'

Vue.prototype.config = {latency: '0'}
let $_wrapper

const $_methods = {
  loadServeyData: jest.fn()
}

let _data = {
  componentConfig: {
    surveyId: '5322546',
    latency: '0',
    feedbackButton: 'Feedback',
    feedbackTitle: 'Feedback',
    feedbackSubmit: 'Submit',
    feedbackThankyou: 'Thank you'
  },
  componentVisible: true,
  survey: _survey
}

beforeEach(() => {
  jest.clearAllMocks()
  $_wrapper = mount(Feedback, {
    mixin: config(Vue),
    data() {
      return _data
    },
    store: __createMocks().store,
    methods: $_methods
  })
})

afterEach(() => {
  $_wrapper.destroy()
})

describe('components/Feedback/Feedback.vue', () => {
  it('is vue instance', () => {
    expect($_wrapper.vm).toBeTruthy()
  })

  it('should get to second step if feedback button is clicked', async () => {
    expect($_wrapper.find('.step-1').exists()).toBe(true)
    expect($_wrapper.findComponent(BButton).exists()).toBe(true)
    await $_wrapper.findComponent(BButton).trigger('click')
    expect($_wrapper.vm.step).toBe(STATE.YESNO)
  })

  it('should show a feedback question in second step', () => {
    expect($_wrapper.find('.step-2').exists()).toBe(true)
    expect($_wrapper.find('h3').text()).toBe(_survey.questions[0].title.English)
  })

  it('should show options to choose in third step', () => {
    _data.step = STATE.POSITIVE
    $_wrapper = mount(Feedback, {
      mixin: config(Vue),
      data() {
        return _data
      },
      store: __createMocks().store,
      methods: $_methods
    })
    expect($_wrapper.vm.step).toBe(STATE.POSITIVE)
    const _mi = $_wrapper.findAllComponents(BButton)
    let _questionOptions = _survey.questions[1].options
    for (const m in _questionOptions) expect(_mi.at(m).text()).toEqual(_questionOptions[m].title.English)
  })

  it('check email input in last step', () => {
    _data.step = STATE.FINAL
    $_wrapper = mount(Feedback, {
      mixin: config(Vue),
      data() {
        return _data
      },
      store: __createMocks().store,
      methods: $_methods
    })
    expect($_wrapper.findComponent(BFormInput).exists()).toBe(true)
    expect($_wrapper.findComponent(FeedbackWidgetFooter).vm.disabled).toBe(false)
  })
})
