import {mount} from '@vue/test-utils'
import config from '@/mixins/commonMixin'
import Vue from 'vue'
import {__createMocks} from '@/store/mocks'
import FeedbackYesNoWidget from '@/components/common/Feedback/FeedbackYesNoWidget'

let $_wrapper

let $props = {
  options: [
    {
      id: 10008,
      title: {
        English: 'Troubleshooting Search'
      },
      value: 'Troubleshooting Search'
    },
    {
      id: 10009,
      title: {
        English: 'Navigation'
      },
      value: 'Navigation'
    }
  ]
}

describe('components/Feedback/FeedbackYesNoWidget.vue', () => {
  beforeEach(() => {
    jest.clearAllMocks()
    $_wrapper = mount(FeedbackYesNoWidget, {
      mixin: config(Vue),
      store: __createMocks().store,
      propsData: $props
    })
  })

  it('is vue instance', () => {
    expect($_wrapper.vm).toBeTruthy()
  })

  it('check thumbs up and down', () => {
    expect($_wrapper.find('.thumb__positive').exists()).toBe(true)
    expect($_wrapper.find('.thumb__negative').exists()).toBe(true)
  })

  it('check if selecting negative feedback renders correctly', () => {
    $_wrapper.vm.positive.selected = true
    expect($_wrapper.find('.thumb__positive', '.active').exists()).toBe(true)
    $_wrapper.vm.selectFeedback('negative')
    expect($_wrapper.find('.thumb__negative', '.active').exists()).toBe(true)
  })
})
