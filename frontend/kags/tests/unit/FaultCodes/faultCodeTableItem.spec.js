import {shallowMount} from '@vue/test-utils'
import FaultCodeTableItem from '@/components/common/FaultCodes/FaultCodeTableItem'
import ButtonIndicatorSearch from '@/components/common/ButtonIndicatorSearch'
import config from '@/mixins/commonMixin'
import Vue from 'vue'

let $_wrapper

const $_code = {
  'faultCode': 'E00017207',
  'controller': 'UNKNOWN',
  'name': 'Sensor Supply 1 Out of Range High',
  'description': 'The auxiliary controller detects a voltage greater than 8.25 volts.',
  'startTime': '2021-10-19T10:12:00.527Z',
  'endTime': '2021-12-19T12:21:25.372Z',
  'engineHoursStart': 65,
  'engineHoursEnd': 300,
  'status': 'Active',
  'severity': 'WARNING',
  'startTimeLocalized': '10/19/2021, 12:12:00 PM'
}

beforeEach(() => {
  $_wrapper = shallowMount(FaultCodeTableItem, {
    mixins: [config(Vue)],
    propsData: {
      url: 'test',
      code: $_code,
      searchLabel: 'search label here'
    }
  })
})

afterEach(() => {
  $_wrapper.destroy()
})

describe('components/common/FaultCodes/FaultCodeTableItem.vue', () => {
  it('is vue instance', () => {
    expect($_wrapper.vm).toBeTruthy()
  })

  it('proper rendering', () => {
    expect($_wrapper.findComponent(ButtonIndicatorSearch).exists()).toBe(true)
    expect($_wrapper.find('.item__description').text()).toEqual($_code.name)
    expect($_wrapper.find('.item__code').text()).toEqual($_code.faultCode)
    expect($_wrapper.find('.item__severity').text()).toEqual($_code.severity)
    expect($_wrapper.find('.item__status').text().toLowerCase()).toEqual($_code.status.toLowerCase())
  })
})
