import {shallowMount} from '@vue/test-utils'
import CompareTable from '@/components/Compare/CompareTable'
import mockedSpecs from '~/server/responses/products-api/specs'

const _model = {id: 'M0055', '@name': 'test', overview: '<p class="test-class">Sth very mocked</p>', '@path': '/na/test', '@nodeType': 'dxp:product'}
const _createTableData = (length = 1) => {
  const models = Array.from({length}).map((_, i) => ({..._model, id: 'M00' + (55 + i)}))
  return {
    models,
    sections: models.reduce((agg, c) => {
      agg[c.id] = {model: _model, mockedSpecs}
      return agg
    }, {})
  }
}

const _props = {
  tableData: _createTableData()
}

const options = {
  propsData: _props,
  stubs: {
    LazyHydrate: true,
    ProductSpecItem: true,
    'b-icon': true,
    'b-button': true,
    Accordion: true,
    CompareTableHeader: true
  },
  mocks: {
    $fetchState: {pending: false},
    $nuxt: {
      context: {
        $regions: {
          current: {
            name: 'en',
            label: 'english',
            language: 'en'
          }
        }
      }
    }
  }
}

const getWrapper = (opt = {}) => shallowMount(CompareTable, {...options, ...opt})
const wrapper = getWrapper()

describe('CompareTable component [data props]', () => {
  beforeEach(() => {
    process.client = true
    jest.useFakeTimers()
  })

  afterEach(() => {
    delete process.client
    jest.useRealTimers()
  })

  it('has being rendered [default state]', () => {
    expect(wrapper.find('[data-test-id="compare-table"]').exists()).toBe(true)
    expect(wrapper.find('[data-test-id="compare-wide-container"]').classes()).not.toContain('wide')
    expect(wrapper.find('[data-test-id="specification-table-compare-header"]').exists()).toBe(false)
    expect(wrapper.findAll('[data-test-id^="compare-table__header"]').length).toBe(Object.keys(_props.tableData.sections).length)
  })

  it('renders one model layout', async () => {
    await wrapper.setProps({showHeader: true})
    expect(wrapper.vm.FIRST_COLUMN_WIDTH_CLASS).toEqual('col-sticky col-md')
  })

  it('renders multiple models layout', async () => {
    const _wrapper = getWrapper({propsData: {...options.propsData, showHeader: true, tableData: _createTableData(2)}})
    expect(_wrapper.vm.FIRST_COLUMN_WIDTH_CLASS).toEqual('col-sticky col-md')
  })

  it('is wide for more than 3 models on mobile', async () => {
    const _wrapper = getWrapper({
      propsData: {tableData: _createTableData(4)},
      mocks: {...options.mocks, $nuxt: {context: {...options.mocks.$nuxt.context, $utils: {...options.mocks.$nuxt.context.$utils, isMobile: () => true, isTablet: () => false}}}}
    })
    jest.advanceTimersByTime(500)
    await wrapper.vm.$nextTick()
    expect(_wrapper.find('[data-test-id="compare-wide-container"]').classes()).toContain('wide')
  })

  it('is not wide when less than 5 models on desktop', async () => {
    const _wrapper = getWrapper({
      propsData: {tableData: _createTableData(5)},
      mocks: {...options.mocks, $nuxt: {context: {...options.mocks.$nuxt.context, $utils: {...options.mocks.$nuxt.context.$utils, isMobile: () => false, isTablet: () => false}}}}
    })
    jest.advanceTimersByTime(500)
    await wrapper.vm.$nextTick()
    expect(_wrapper.find('[data-test-id="compare-wide-container"]').classes()).not.toContain('wide')
  })

  it('is wide when more than 2 models on mobile', async () => {
    const _wrapper = getWrapper({
      propsData: {tableData: _createTableData(3)},
      mocks: {...options.mocks, $nuxt: {context: {...options.mocks.$nuxt.context, $utils: {...options.mocks.$nuxt.context.$utils, isMobile: () => true, isTablet: () => false}}}}
    })
    jest.advanceTimersByTime(500)
    await wrapper.vm.$nextTick()
    expect(_wrapper.find('[data-test-id="compare-wide-container"]').classes()).toContain('wide')
  })

  it('is wide when more than 3 models on tablet', async () => {
    const _wrapper = getWrapper({
      propsData: {tableData: _createTableData(4)},
      mocks: {...options.mocks, $nuxt: {context: {...options.mocks.$nuxt.context, $utils: {...options.mocks.$nuxt.context.$utils, isMobile: () => false, isTablet: () => true}}}}
    })
    jest.advanceTimersByTime(500)
    await wrapper.vm.$nextTick()
    expect(_wrapper.find('[data-test-id="compare-wide-container"]').classes()).toContain('wide')
  })
})
