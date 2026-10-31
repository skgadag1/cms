import {shallowMount} from '@vue/test-utils'
import BuildAndQuote from '@/components/elements/BuildAndQuote'
import mockModelPricing from '~/server/responses/bobcat-api/pages/components/modelPricing'

global.process.client = false

const currencyObject = {
  us: {
    value: 'USD',
    label: 'USD'
  },
  cad: {
    value: 'CAD',
    label: 'CAD'
  }
}

const opt = {
  propsData: {
    pricing: mockModelPricing.model.pricing,
    labels: {
      netPriceLabel: mockModelPricing.main['01'].netPriceLabel,
      rebateLabel: mockModelPricing.main['01'].rebateLabel,
      paymentLabel: mockModelPricing.main['01'].paymentLabel,
      financingLabel: mockModelPricing.main['01'].financingLabel
    },
    currency: currencyObject.us.label,
    hasIndicator: true,
    indicatorLabel: 'test'
  },
  mocks: {
    $fetchState: {
      pending: false
    }
  },
  stubs: {
    LazyHydrate: true,
    WarningAlert: true
  }
}

const getWrapper = (options = opt) => shallowMount(BuildAndQuote, options)
const wrapper = getWrapper()

describe('BuildAndQuote component', () => {
  it('display proper labels', () => {
    const titles = wrapper.findAll('h6')
    expect(titles.length).toEqual(Object.keys(wrapper.vm.labels).length)

    expect(titles.at(0).text()).toContain(wrapper.vm.labels[Object.keys(wrapper.vm.labels)[0]])
    expect(titles.at(1).text()).toContain(wrapper.vm.labels[Object.keys(wrapper.vm.labels)[1]])
    expect(titles.at(2).text()).toContain(wrapper.vm.labels[Object.keys(wrapper.vm.labels)[2]])
    expect(titles.at(3).text()).toContain(wrapper.vm.labels[Object.keys(wrapper.vm.labels)[3]])
  })

  it('display proper currency label', async () => {
    const price = wrapper.find('[data-test-id="build-quote-column__left"] p:first-of-type')
    expect(price.text().endsWith('USD')).toEqual(true)

    const cadWrapper = getWrapper({...opt, propsData: {...opt.propsData, currency: currencyObject.cad.label}})
    const priceCad = cadWrapper.find('[data-test-id="build-quote-column__left"] p:first-of-type')
    expect(priceCad.text().endsWith('CAD')).toEqual(true)
  })

  it('handle errors', async () => {
    expect(wrapper.find('[data-test-id="build-quote__errormessage"]').exists()).toBeFalsy()
    const _wrapper = getWrapper({...opt, propsData: {...opt.propsData, pricing: {}}})
    expect(_wrapper.find('[data-test-id="build-quote__errormessage"]').exists()).toBe(true)
  })

  it('shows proper rebate', async () => {
    expect(wrapper.find('[data-test-id="build-quote-column__left"] p[data-test-id="model-pricing-rebate__value"]').text()).toEqual('$3,000 USD')
  })

  it('shows proper financing info', async () => {
    expect(wrapper.find('[data-test-id="build-quote-column__right"] p[data-test-id="model-pricing-financing__value"]').text()).toEqual('0% for 48 Months')
  })

  it('shows proper indicator label', async () => {
    expect(wrapper.find('[data-test-id="dinv-model-pricing-includeFrontendLoader-indicator"]').exists()).toBe(true)
  })
})
