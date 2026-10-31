import {shallowMount} from '@vue/test-utils'
import cloudinary from './../utils/cloudinary.mock'
import CompareItem from '@/components/elements/Compare/CompareItem'
import productUtils from '~/plugins/utils/productUtils'

const _props = {
  model: {
    '@id': 'testingId',
    title: 'Test'
  },
  compare: false,
  showRemove: false,
  selected: false,
  disabled: false,
  isRemovable: true,
  compareLabel: 'Compare me'
}

const opt = {
  propsData: _props,
  mocks: {
    $nuxt: {
      context: {
        ...cloudinary,
        $utils: {
          ...cloudinary.$utils,
          products: productUtils
        },
        $config: {}
      }
    }
  },
  stubs: {
    'b-icon': true,
    'b-img-lazy': true,
    'b-form-checkbox': true
  }
}

const wrapper = shallowMount(CompareItem, opt)

describe('CompareItem component [data props]', () => {
  it('has proper title', () => {
    expect(wrapper.find('[data-test-id="compare-item-title"]').exists()).toBe(true)
    expect(wrapper.find('[data-test-id="compare-item-title"]').text()).toEqual(_props.model.title)
  })

  it('renders compare', async () => {
    expect(wrapper.find('[data-test-id="compare-item-compare-button"]').exists()).toBe(false)
    await wrapper.setProps({compare: true})
    expect(wrapper.find('[data-test-id="compare-item-compare-button"]').exists()).toBe(true)
  })

  it('renders empty', async () => {
    const _wrapper = shallowMount(CompareItem, {...opt, propsData: {..._props, model: {}, compare: true}})
    expect(_wrapper.find('[data-test-id="compare-item-empty"]').exists()).toBe(true)
  })
})

describe('CompareItem component [events]', () => {
  it('emits remove event via trash icon', async () => {
    const wrapper = shallowMount(CompareItem, {...opt, propsData: {..._props, compare: true, showRemove: true}})
    expect(wrapper.find('[data-test-id="compare-item-remove"]').exists()).toBe(true)
    await wrapper.find('[data-test-id="compare-item-remove"]').trigger('click')
    expect(wrapper.emitted('remove')).toBeTruthy()
    expect(wrapper.emitted('remove')[0]).toEqual([_props.model['@id']])
  })

  it('emits remove event by button', async () => {
    const wrapper = shallowMount(CompareItem, {...opt, propsData: {..._props, compare: true, showRemove: true, selected: true}})
    expect(wrapper.find('[data-test-id="compare-item-compare-button"]').exists()).toBe(true)
    await wrapper.find('[data-test-id="compare-item-compare-button"]').trigger('click')
    expect(wrapper.emitted('add')).toBeFalsy()
    expect(wrapper.emitted('remove')).toBeTruthy()
    expect(wrapper.emitted('remove')[0]).toEqual([_props.model['@id']])
  })

  it('emits add event by default', async () => {
    const wrapper = shallowMount(CompareItem, {...opt, propsData: {..._props, compare: true}})
    expect(wrapper.find('[data-test-id="compare-item-compare-button"]').exists()).toBe(true)
    await wrapper.find('[data-test-id="compare-item-compare-button"]').trigger('click')
    expect(wrapper.emitted('add')).toBeTruthy()
    expect(wrapper.emitted('add')[0]).toEqual([_props.model])
  })

  it('emits empty event', async () => {
    const _wrapper = shallowMount(CompareItem, {...opt, propsData: {..._props, model: {}}})
    expect(_wrapper.find('[data-test-id="compare-item-empty"]').exists()).toBe(true)
    await _wrapper.find('[data-test-id="compare-item-empty"]').trigger('click')
    expect(_wrapper.emitted('empty-trigger')).toBeTruthy()
  })

  it('do not emits while disabled', async () => {
    const wrapper = shallowMount(CompareItem, {...opt, propsData: {..._props, compare: true, disabled: true}})
    const emitted = wrapper.emitted()

    expect(wrapper.find('[data-test-id="compare-item-compare-button"]').exists()).toBe(true)
    await wrapper.find('[data-test-id="compare-item-compare-button"]').trigger('click')
    expect(emitted.add).toBeUndefined()
    expect(emitted.remove).toBeUndefined()
    expect(emitted['empty-trigger']).toBeUndefined()
  })

  it('do not emits while not removable', async () => {
    const wrapper = shallowMount(CompareItem, {...opt, propsData: {..._props, compare: true, showRemove: true, isRemovable: false}})
    expect(wrapper.find('[data-test-id="compare-item-compare-button"]').exists()).toBe(true)
    await wrapper.find('[data-test-id="compare-item-compare-button"]').trigger('click')
    expect(wrapper.find('[data-test-id="compare-item-remove"]').exists()).toBe(false)
  })
})
