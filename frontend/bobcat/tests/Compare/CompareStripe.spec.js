import {shallowMount} from '@vue/test-utils'
import cloudinary from './../utils/cloudinary.mock'
import CompareItem from '@/components/elements/Compare/CompareItem'
import CompareStripe from '@/components/Compare/CompareStripe'
import productUtils from '~/plugins/utils/productUtils'

const _props = {
  models: [{
    '@id': 'test'
  }, {
    '@id': 'anotherModel'
  }],
  maxToCompare: 6,
  options: [{
    '@id': 'test'
  }]
}

const _model = {id: 'M0118', '@name': 'test', '@path': '/na/test'}

const opt = {
  propsData: _props,
  provide: {
    model: _model
  },
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
    CompareItem,
    CompareModelsDropdown: true,
    'b-icon': true,
    'b-form-checkbox': true,
    'b-button': true,
    'b-img': true
  }
}

const wrapper = shallowMount(CompareStripe, opt)

describe('CompareItem component [data props]', () => {
  it('has proper empty number', () => {
    expect(wrapper.findAllComponents(CompareItem).length).toEqual(6)
    expect(wrapper.findAll('[data-test-id="compare-stripe-empty-item"]').length).toEqual(_props.maxToCompare - _props.models.length)
  })

  it('renders compare buttons', async () => {
    expect(wrapper.findAll('[data-test-id="compare-stripe-button"]').length).toEqual(2)
    await wrapper.setProps({models: []})
    wrapper.vm.$nextTick(() => expect(wrapper.findAll('[data-test-id="compare-stripe-button"]').exists()).toBe(false))
  })

  it('fires open method on click for dropdown', async () => {
    const mockedFn = jest.fn()
    wrapper.setMethods({emptyClickHandler: mockedFn})
    await wrapper.find('[data-test-id="compare-stripe-empty-item"]').vm.$emit('empty-trigger')
    expect(mockedFn).toHaveBeenCalled()
  })
})
