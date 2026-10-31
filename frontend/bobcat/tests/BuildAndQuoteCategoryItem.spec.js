import {shallowMount} from '@vue/test-utils'
import {BCard} from 'bootstrap-vue'
import cloudinary from './utils/cloudinary.mock'
import BuildAndQuoteCategoryItem from '@/components/elements/BuildAndQuoteCategoryItem'
import linkUtils from '~/plugins/utils/linkUtils'
import categoryMock from '~/server/responses/bobcat-api/products/categories'

const options = {
  propsData: {
    category: categoryMock[0]
  },
  mocks: {
    $nuxt: {
      context: {
        $utils: {
          ...cloudinary.$utils,
          getLinkField: linkUtils.getLinkField
        },
        $regions: {
          current: {
            name: 'en',
            label: 'english',
            language: 'en'
          }
        }
      }
    },
    $fetchState: {
      pending: false
    }
  },
  stubs: {
    BCard,
    'b-icon': true,
    'b-card-header': true,
    'b-card-footer': true,
    'b-collapse': true,
    ImageSet: true,
    LazyHydrate: true,
    WarningAlert: true
  },
  directives: {
    'b-toggle': () => {}
  }
}

const getWrapper = (opt = options) => shallowMount(BuildAndQuoteCategoryItem, opt)
const wrapper = getWrapper()

describe('BuildAndQuoteCategoryItem component', () => {
  it('renders component with empty category and without title', async () => {
    const wrapper = getWrapper({...options, propsData: {category: {}}})
    const _link = wrapper.find('[data-test-id="build-quote-category-item__header-link"]')
    expect(wrapper.find('[data-test-id="build-quote-category-item"]').exists()).toBe(true)
    expect(wrapper.find('[data-test-id="build-quote-category-item__header"]').exists()).toBe(true)
    expect(_link.exists()).toBe(true)
    expect(_link.element.tagName.toLowerCase()).toEqual('a')
    expect(_link.find('[data-test-id="category-item__image"]').exists()).toBe(true)
    expect(wrapper.find('[data-test-id^="build-quote-subcategory-item"]').exists()).toBe(false)
  })

  it('renders title', async () => {
    const _wrapper = getWrapper({...options, propsData: {category: {}}})
    expect(_wrapper.find('[data-test-id="build-quote-category-item__title"]').exists()).toBe(true)

    expect(wrapper.find('[data-test-id="build-quote-category-item__title"]').text()).toEqual(categoryMock[0].title)
  })

  it('renders category image', async () => {
    expect(wrapper.find('[data-test-id="category-item__image"]').exists()).toBe(true)
  })

  it('renders component with subcategories', async () => {
    expect(wrapper.find('[data-test-id="build-quote-category-item"]').exists()).toBe(true)
    expect(wrapper.find('[data-test-id="build-quote-category-item__header-link"]').exists()).toBe(false)
    expect(wrapper.find('[data-test-id="build-quote-category-item__header-div"]').exists()).toBe(true)
    expect(wrapper.find('[data-test-id="build-quote-category-item__header-div"]').element.tagName.toLowerCase()).toEqual('div')
    expect(wrapper.find('[data-test-id^="build-quote-subcategory-item"]').exists()).toBe(true)
  })

  it('renders subcategory image', async () => {
    expect(wrapper.find('[data-test-id^="build-quote-subcategory-item"] [data-test-id="subcategory-item__image"]').exists()).toBe(true)
  })

  it('renders subcategory with no image error', async () => {
    const _el = wrapper.find('[data-test-id="build-quote-subcategory-item__2c2327f1-63e1-4ee8-aefd-7513f708fe49"]')
    expect(_el.find('[data-test-id="subcategory-item__image"]').exists()).toBe(false)
    expect(_el.find('[data-test-id="subcategory-error_noimage"]').exists()).toBe(false)
    await wrapper.setData({inEditorEdit: true})
    await wrapper.vm.$forceUpdate()
    expect(_el.find('[data-test-id="subcategory-error_noimage"]').exists()).toBe(true)
  })

  it('renders subcategory title', async () => {
    const _el = wrapper.findAll('[data-test-id^="build-quote-subcategory-item__"] .card-footer').at(0)
    expect(_el.exists()).toBe(true)
    expect(_el.text()).toEqual(wrapper.vm.subcategories[0].title)
  })

  it('renders subcategory no url error message', async () => {
    expect(wrapper.find('[data-test-id="build-quote-subcategory-item__2c2327f1-63e1-4ee8-aefd-7513f708fe49"] [data-test-id="subcategory-error_nourl"]').exists()).toBe(true)
  })

  it('renders subcategory custom url', async () => {
    const _el = wrapper.find('[data-test-id="build-quote-subcategory-item__930093ea-3f4d-4ea5-9d17-39bae09a39f4"]')
    const _linkField = linkUtils.getLinkField(wrapper.vm.subcategories[0].pageLink)

    expect(_el.find('[data-test-id="subcategory-error_nourl"]').exists()).toBe(false)
    expect(_el.attributes('href')).toEqual(_linkField.url)
    expect(_el.attributes('target')).toEqual(_linkField.target)
  })

  it('renders subcategory global url', () => {
    const wrapper = getWrapper({...options, propsData: {...options.propsData, url: 'test-url-%ID%'}})
    const _el = wrapper.find('[data-test-id="build-quote-subcategory-item__930093ea-3f4d-4ea5-9d17-39bae09a39f4"]')

    expect(_el.attributes('href')).toEqual('test-url-104')
    expect(_el.attributes('target')).toEqual('_blank')
  })
})
