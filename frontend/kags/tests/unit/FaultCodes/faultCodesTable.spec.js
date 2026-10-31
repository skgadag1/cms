import {mount} from '@vue/test-utils'
import FaultCodesTable from '@/components/common/FaultCodes/FaultCodesTable'
import BasicGridTable from '@/components/common/BasicGridTable'
import config from '@/mixins/commonMixin'
import Vue from 'vue'
import $_codes from '../../../public/mocks/iot-api/faultcodes/ALJ812464.json'

let $_wrapper

beforeEach(() => {
  $_wrapper = mount(FaultCodesTable, {
    mixins: [config(Vue)],
    propsData: {
      fields: [
        {key: 'status', label: 'statusLabel', sortable: true},
        {key: 'severity', label: 'severityLabel', sortable: true},
        {key: 'faultCode', label: 'faultCodeLabel', sortable: true},
        {key: 'name', label: 'descriptionLabel', sortable: true},
        {key: 'startTimeLocalized', label: 'startTimeLabel', sortable: true},
        {key: 'engineHoursStart', label: 'engineHoursLabel', thClass: 'engine', sortable: true}
      ],
      codes: $_codes.items,
      config: {
        troubleshootingUrl: 'test'
      }
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

  it('properly renders', async () => {
    expect($_wrapper.findComponent(BasicGridTable).exists()).toBe(true)
    expect($_wrapper.findComponent(BasicGridTable).vm.disabled).toEqual($_wrapper.vm.disabled)
    await $_wrapper.setProps({disabled: true})
    expect($_wrapper.findComponent(BasicGridTable).vm.disabled).toEqual($_wrapper.vm.disabled)
    expect($_wrapper.vm.codes.length).toEqual($_codes.items.length)
  })
})
