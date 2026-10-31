import {shallowMount} from '@vue/test-utils'
import ProductSpecificationsInventory from '@/components/ProductSpecificationsInventory'

const propsData = {
  keySpecsTitle: ''
}

const currentProduct = {
  id: 'current-model',
  name: 'Current model',
  lifecycle: 'Current',
  '@path': '/na/en/current-model',
  '@nodeType': 'dxp:product'
}

const historicalProduct = {
  id: 'historical-model',
  name: 'Historical model',
  lifecycle: 'Historical',
  '@path': '/na/en/historical-model',
  '@nodeType': 'dxp:product',
  productDetailsDataSource: 'historical-source'
}

const productGroup = {
  '@nodeType': 'dxp:productGroup',
  '@nodes': ['currentProduct', 'historicalProduct'],
  currentProduct,
  historicalProduct
}

const getWrapper = ({model = productGroup, pageTemplateId = 'bobcat:pages/machine-details'} = {}) => shallowMount(ProductSpecificationsInventory, {
  propsData,
  provide: {
    model,
    pageTemplateId,
    selectedVariant: null
  },
  stubs: {
    'b-overlay': true,
    Specifications: {template: '<section><slot /></section>'},
    CompareTableWrapper: {
      name: 'CompareTableWrapper',
      props: {models: Array},
      template: '<div />'
    },
    WarningAlert: true
  },
  mocks: {
    $fetchState: {pending: false},
    $nuxt: {
      context: {
        $services: {
          bobcatapi: {
            getConcepts: () => ({})
          }
        },
        $regions: {
          current: {
            name: 'en',
            language: {name: 'en'}
          }
        }
      }
    }
  }
})

const modelsFor = wrapper => wrapper.findComponent({name: 'CompareTableWrapper'}).props('models')
const renderTables = async (wrapper) => {
  wrapper.vm.labelsLoaded = true
  await wrapper.vm.$nextTick()
}

describe('ProductSpecificationsInventory', () => {
  it('uses the historical product and preserves its specification data source', async () => {
    const wrapper = getWrapper({
      pageTemplateId: 'bobcat:pages/historical-machine-details'
    })
    await renderTables(wrapper)

    expect(modelsFor(wrapper)).toEqual([{
      id: historicalProduct.id,
      title: historicalProduct.name,
      '@path': historicalProduct['@path'],
      productDetailsDataSource: historicalProduct.productDetailsDataSource
    }])
  })

  it('continues to use the current product on current pages', async () => {
    const wrapper = getWrapper()
    await renderTables(wrapper)

    expect(modelsFor(wrapper)).toEqual([{
      id: currentProduct.id,
      title: currentProduct.name,
      '@path': currentProduct['@path'],
      productDetailsDataSource: undefined
    }])
  })

  it('falls back to the injected model when no lifecycle-filtered model is available', async () => {
    const model = {
      id: 'fallback-model',
      name: 'Fallback model',
      '@path': '/na/en/fallback-model',
      productDetailsDataSource: 'fallback-source'
    }
    const wrapper = getWrapper({
      model,
      pageTemplateId: 'bobcat:pages/historical-machine-details'
    })
    await renderTables(wrapper)

    expect(modelsFor(wrapper)).toEqual([{
      id: model.id,
      title: model.name,
      '@path': model['@path'],
      productDetailsDataSource: model.productDetailsDataSource
    }])
  })
})
