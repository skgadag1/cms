import {mount} from '@vue/test-utils'
import ProcedureItemMobile from '@/components/common/TroubleshootingPro/ProcedureItemMobile'
import {BButton} from 'bootstrap-vue'
import config from '@/mixins/commonMixin'
import Vue from 'vue'

let $_wrapper

const $_props = {
  item: {
    tree_id: '54623',
    long_description: '<html><body><h2>H1528 - Ride Control Output Controller Failure</h2><h2>Detailed Description</h2><div>     A Code H1528 occurs when the Auxiliary Controller detects an internal fault with the Ride Control Output.    </div><h2>Possible Causes</h2><ul><li>Auxiliary Controller. </li></ul><h2>Conditions for Alarm</h2><ul><li>Key switch / (run/enter) On.</li></ul><h2>Conditions for Clearing Alarm</h2><ul></ul></body></html>',
    short_description: 'Ride Control Output Controller Failure',
    score: 1,
    title: 'H1528',
    scoreValue: 100
  }
}

beforeEach(() => {
  $_wrapper = mount(ProcedureItemMobile, {
    mixin: config(Vue),
    propsData: $_props
  })
})

afterEach(() => {
  $_wrapper.destroy()
})

describe('components/common/Machine/ProcedureItemMobile.vue', () => {
  it('is vue instance', () => {
    expect($_wrapper.vm).toBeTruthy()
  })

  it('check proper rendering of item fields', () => {
    expect($_wrapper.find('.box').exists()).toBeTruthy()
    expect($_wrapper.find('.box').text()).toContain($_props.item.title)
    expect($_wrapper.find('.box').text()).toContain($_props.item.short_description)
  })

  it('check progress bar and its attributes', () => {
    expect($_wrapper.find('.progress-bar').exists()).toBeTruthy()
    expect($_wrapper.find('.progress-bar').attributes('aria-valuenow')).toBe($_props.item.scoreValue.toString())
  })

  it('check start session and details buttons', () => {
    expect($_wrapper.findAllComponents(BButton).exists()).toBeTruthy()
    expect($_wrapper.findAllComponents(BButton).length).toBe(2)
  })
})
