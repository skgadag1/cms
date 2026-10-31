import {shallowMount} from '@vue/test-utils'
import ProductOverview from '@/components/ProductOverview'

const _title = '%ID% title'
const _model = {id: 'M0115', '@name': 'test', overview: '<p class="test-class">Sth very mocked</p>'}

const options = {
  propsData: {
    title: _title
  },
  stubs: {
    'b-overlay': true,
    ImageSet: true,
    LazyHydrate: true,
    WarningAlert: true
  },
  provide: {
    model: _model
  }
}

const getWrapper = (opt = options) => shallowMount(ProductOverview, opt)
const wrapper = getWrapper()

describe('ProductOverview component [data props]', () => {
  it('has rendered error info message in editmode', async () => {
    const _wrapper = getWrapper({...options, provide: {model: null}})
    await _wrapper.setData({inEditorEdit: true})
    await _wrapper.vm.$forceUpdate()
    expect(_wrapper.find('[data-test-id="product-overview-title"]').exists()).toBe(false)
    expect(_wrapper.find('[data-test-id="product-overview-html"]').exists()).toBe(false)
    expect(_wrapper.find('[data-test-id="product-overview-model-alert"]').exists()).toBe(true)
  })

  it('has rendered silently when no model', () => {
    const _wrapper = getWrapper({...options, mocks: {model: null}})
    expect(_wrapper.find('[data-test-id="product-overview-empty-alert"]').exists()).toBe(false)
  })

  it('has title rendered', () => {
    expect(wrapper.find('[data-test-id="product-overview-title"]').exists()).toBe(true)
    expect(wrapper.find('h2').text()).toEqual(_title.replace('%ID%', _model.id))
  })

  it('has html slot rendered', () => {
    expect(wrapper.find('[data-test-id="product-overview-html"]').exists()).toBe(true)
    expect(wrapper.find('[data-test-id="product-overview-html"] .test-class').text()).toEqual('Sth very mocked')
  })

  it('has html slot not rendered with error when no overview', async () => {
    const _wrapper = getWrapper({...options, provide: {model: {'@path': _model.id}}})
    await _wrapper.setData({inEditorEdit: true})
    await _wrapper.vm.$forceUpdate()
    expect(_wrapper.find('[data-test-id="product-overview-html"]').exists()).toBe(false)
    expect(_wrapper.find('[data-test-id="product-overview-empty-alert"]').isVisible()).toBe(true)
  })
})
