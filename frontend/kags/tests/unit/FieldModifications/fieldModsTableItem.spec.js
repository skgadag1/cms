import {shallowMount} from '@vue/test-utils'
import FieldModsTableItem from '@/components/common/FieldMods/FieldModsTableItem'
import config from '@/mixins/commonMixin'
import Vue from 'vue'
import $_mods from '../../../public/mocks/equipment-api/field_modifications.json'

let $_wrapper
let $_mod = $_mods[0]

beforeEach(() => {
  $_wrapper = shallowMount(FieldModsTableItem, {
    mixins: [config(Vue)],
    propsData: {
      url: 'test',
      mod: $_mod
    }
  })
})

afterEach(() => {
  $_wrapper.destroy()
})

describe('components/common/FieldModifications/FieldModsTableItem.vue', () => {
  it('is vue instance', () => {
    expect($_wrapper.vm).toBeTruthy()
  })

  it('proper rendering', () => {
    expect($_wrapper.find('.item__description').text()).toEqual($_mod.description)
    expect($_wrapper.find('.item__code').text()).toEqual($_mod.field_modification_code)
    expect($_wrapper.find('.item__class').text()).toEqual($_mod.campaign_code)
    expect($_wrapper.find('.item__status').text().toLowerCase()).toEqual($_mod.status.toLowerCase())
  })
})
