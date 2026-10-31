import {mount} from '@vue/test-utils'
import FieldModsTable from '@/components/common/FieldMods/FieldModsTable'
import BasicGridTable from '@/components/common/BasicGridTable'
import config from '@/mixins/commonMixin'
import Vue from 'vue'
import $_mods from '../../../public/mocks/equipment-api/field_modifications.json'

let $_wrapper

beforeEach(() => {
  $_wrapper = mount(FieldModsTable, {
    mixins: [config(Vue)],
    propsData: {
      fields: [
        {key: 'status', label: 'statusHeader', sortable: false},
        {key: 'class', label: 'classHeader', sortable: false},
        {key: 'code', label: 'codeHeader', sortable: false},
        {key: 'description', label: 'descriptionHeader', sortable: false},
        {key: 'date', label: 'expiresOnHeader', sortable: false}
      ],
      items: $_mods,
      config: {
        troubleshootingUrl: 'test'
      }
    }
  })
})

afterEach(() => {
  $_wrapper.destroy()
})

describe('components/common/FieldModifications/FieldModsTable.vue', () => {
  it('is vue instance', () => {
    expect($_wrapper.vm).toBeTruthy()
  })

  it('properly renders', async () => {
    expect($_wrapper.findComponent(BasicGridTable).exists()).toBe(true)
    expect($_wrapper.findComponent(BasicGridTable).vm.disabled).toEqual($_wrapper.vm.disabled)
    await $_wrapper.setProps({disabled: true})
    expect($_wrapper.findComponent(BasicGridTable).vm.disabled).toEqual($_wrapper.vm.disabled)
    expect($_wrapper.vm.items.length).toEqual($_mods.length)
  })
})
