import {shallowMount} from '@vue/test-utils'
import cloudinary from './utils/cloudinary.mock'
import ModelPricing from '@/components/ModelPricing'
import mockModelPricing from '~/server/responses/bobcat-api/pages/components/modelPricing'
import linkUtils from '~/plugins/utils/linkUtils'

const {
  title,
  subtitle,
  switchable,
  quickLinks,
  selectCurrencyLabel,
  bqLink,
  netPriceLabel,
  rebateLabel,
  paymentLabel,
  financingLabel,
  productionYearsLabel,
  htag,
  htagSubtitle
} = mockModelPricing.main['01']

const options = {
  propsData: {
    title,
    subtitle,
    switchable,
    quickLinks,
    selectCurrencyLabel,
    bqLink,
    netPriceLabel,
    rebateLabel,
    paymentLabel,
    financingLabel,
    productionYearsLabel,
    htag,
    htagSubtitle
  },
  provide: {
    model: mockModelPricing.model
  },
  mocks: {
    $fetchState: {
      pending: false
    },
    $nuxt: {
      context: {
        ...cloudinary,
        $utils: {
          ...cloudinary.$utils,
          ...linkUtils
        },
        $services: {
          bobcatapi: {
            getConcepts: () => {
              return {}
            }
          }
        },
        $regions: {
          current: {
            name: 'en',
            label: 'english',
            language: {
              name: 'en'
            }
          }
        },
        $breakpoints: {
          mobile: true,
          tablet: false,
          desktop: true
        },
        $config: {}
      }
    }
  },
  stubs: {
    SwitchToggle: true,
    BuildAndQuote: '<div/>',
    CloudinaryGallery: '<div/>',
    LazyHydrate: true
  }
}

const getWrapper = (opt = options) => shallowMount(ModelPricing, opt)
const wrapper = getWrapper()

describe('ModelPricing component', () => {
  it('display proper historical context', async () => {
    expect(wrapper.find('[data-test-id="model-pricing-production-year__mobile"]').exists()).toBe(true)
    expect(wrapper.find('[data-test-id="model-pricing-production-year__desktop"]').exists()).toBe(true)
  })

  it('display proper current context', async () => {
    const _wrapper = getWrapper({...options, provide: {model: {}}})
    expect(_wrapper.find('[data-test-id="model-pricing-production-year__mobile"]').exists()).toBe(false)
    expect(_wrapper.find('[data-test-id="model-pricing-production-year__desktop"]').exists()).toBe(false)
  })

  it('display proper title heading for mobile/tablet/desktop', async () => {
    const _mocks = options.mocks
    // mobile
    _mocks.$nuxt.context.$breakpoints = {mobile: true, tablet: false, desktop: false}
    let _wrapper = getWrapper({...options, mocks: _mocks})
    expect(_wrapper.find('h2[data-test-id="model-pricing-title__mobile"]').exists()).toBe(true)
    expect(_wrapper.find('h2[data-test-id="model-pricing-title__desktop"]').exists()).toBe(false)

    // tablet (design equals mobile)
    _mocks.$nuxt.context.$breakpoints = {mobile: false, tablet: true, desktop: false}
    _wrapper = getWrapper({...options, mocks: _mocks})
    expect(_wrapper.find('h2[data-test-id="model-pricing-title__mobile"]').exists()).toBe(true)
    expect(_wrapper.find('h2[data-test-id="model-pricing-title__desktop"]').exists()).toBe(false)

    // desktop
    _mocks.$nuxt.context.$breakpoints = {mobile: false, tablet: false, desktop: true}
    _wrapper = getWrapper({...options, mocks: _mocks})
    expect(_wrapper.find('h2[data-test-id="model-pricing-title__mobile"]').exists()).toBe(false)
    expect(_wrapper.find('h2[data-test-id="model-pricing-title__desktop"]').exists()).toBe(true)
  })
})
