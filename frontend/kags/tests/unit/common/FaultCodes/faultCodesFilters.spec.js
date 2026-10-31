import {shallowMount, mount} from '@vue/test-utils'
import {BBadge} from 'bootstrap-vue'
import FaultCodesFilters from '@/components/common/FaultCodes/FaultCodesFilters'
import BasicFilterCheckboxes from '@/components/common/BasicFilterCheckboxes'
import BasicFilterCode from '@/components/common/BasicFilterCode'
import BasicFilterDate from '@/components/common/BasicFilterDate'
import BasicFilterRange from '@/components/common/BasicFilterRange'
import config from '@/mixins/commonMixin'
import {__createMocks} from '@/store/mocks'
import Vue from 'vue'
import {INITIAL_FILTERS} from '@/components/common/FaultCodes/FaultCodesComponent'

let $_wrapper

const $_filters = {
  status: {
    title: 'StaTUs',
    options: [
      {label: 'First no', value: false},
      {label: 'Second yes', value: true}
    ]
  },
  severity: {
    title: 'some severity',
    options: [
      {label: 'First yes', value: true},
      {label: 'Second no', value: false}
    ]
  },
  code: {title: 'code title', placeholder: 'code', options: INITIAL_FILTERS.code},
  date: {title: 'date title', options: INITIAL_FILTERS.date},
  engineHours: {title: 'engine hours title', options: INITIAL_FILTERS.engineHours, minLabel: 'min', maxLabel: 'max'}
}

const $_config = {
  'clearFilters': 'Clear all'
}

const $_methods = {
  handleRemove: jest.fn()
}

beforeEach(() => {
  $_wrapper = shallowMount(FaultCodesFilters, {
    mixin: config(Vue),
    propsData: {config: $_config},
    methods: $_methods
  })
})

describe('components/common/FaultCodes/FaultCodesFilters.vue', () => {
  it('is properly rendered (default)', () => {
    expect($_wrapper.vm).toBeTruthy()
    expect($_wrapper.findAll('.mask').length).toBe(0)
    expect($_wrapper.findAllComponents(BasicFilterCheckboxes).length).toBe(0)
  })

  it('is rendering loading state', async () => {
    await $_wrapper.setProps({loading: true})

    expect($_wrapper.findAll('.mask').length).toBe(1)
  })

  it('renders status filter', async () => {
    await $_wrapper.setProps({filterStatus: $_filters.status})
    expect($_wrapper.findAllComponents(BasicFilterCheckboxes).length).toBe(1)
    expect($_wrapper.findComponent(BasicFilterCheckboxes).vm.title).toEqual($_filters.status.title)
  })

  it('renders severity filter', async () => {
    await $_wrapper.setProps({filterSeverity: $_filters.severity})
    expect($_wrapper.findAllComponents(BasicFilterCheckboxes).length).toBe(1)
    expect($_wrapper.findComponent(BasicFilterCheckboxes).vm.title).toEqual($_filters.severity.title)
  })

  it('renders code filter', async () => {
    await $_wrapper.setProps({filterCode: $_filters.code})
    expect($_wrapper.findAllComponents(BasicFilterCode).length).toBe(1)
    expect($_wrapper.findComponent(BasicFilterCode).vm.title).toEqual($_filters.code.title)
  })

  it('renders date filter', async () => {
    await $_wrapper.setProps({filterDate: $_filters.date})
    expect($_wrapper.findAllComponents(BasicFilterDate).length).toBe(1)
    expect($_wrapper.findComponent(BasicFilterDate).vm.title).toEqual($_filters.date.title)
  })

  it('renders engine filter', async () => {
    await $_wrapper.setProps({filterRange: $_filters.engineHours})
    expect($_wrapper.findAllComponents(BasicFilterRange).length).toBe(1)
    expect($_wrapper.findComponent(BasicFilterRange).vm.title).toEqual($_filters.engineHours.title)
  })

  it('renders all filters', async () => {
    await $_wrapper.setProps({
      filterSeverity: $_filters.severity,
      filterStatus: $_filters.status,
      filterCode: $_filters.code,
      filterDate: $_filters.date,
      filterRange: $_filters.engineHours
    })
    expect($_wrapper.findAllComponents(BasicFilterCheckboxes).length).toBe(2)
    expect($_wrapper.findComponent(BasicFilterCheckboxes).vm.options[0].value).toEqual($_filters.severity.options[1].value)
    expect($_wrapper.findComponent(BasicFilterCheckboxes).vm.options[1].value).toEqual($_filters.severity.options[0].value)
    expect($_wrapper.findAllComponents(BasicFilterCode).length).toBe(1)
    expect($_wrapper.findAllComponents(BasicFilterDate).length).toBe(1)
    expect($_wrapper.findAllComponents(BasicFilterRange).length).toBe(1)
    expect($_wrapper.findComponent(BBadge).exists()).toBe(true)
    expect($_wrapper.findAllComponents(BBadge).length).toBe(2)
  })

  it('removes chips', async () => {
    $_wrapper = mount(FaultCodesFilters, {
      mixin: config(Vue),
      propsData: {
        filterSeverity: $_filters.severity,
        filterStatus: $_filters.status,
        config: $_config
      },
      methods: $_methods,
      store: __createMocks({
        getters: {
          isEmpty: jest.fn().mockReturnValue(true)
        }
      }).store
    })
    expect($_wrapper.findComponent(BBadge).exists()).toBe(true)
    expect($_wrapper.findAllComponents(BBadge).length).toBe(2)
    await $_wrapper.findComponent(BBadge).trigger('click')
    expect($_methods.handleRemove).toHaveBeenCalled()
  })

  it('emits up proper events', () => {
    $_wrapper = mount(FaultCodesFilters, {
      mixin: config(Vue),
      propsData: {
        filterSeverity: $_filters.severity,
        config: $_config
      },
      store: __createMocks({
        getters: {
          isEmpty: jest.fn().mockReturnValue(true)
        }
      }).store
    })

    $_wrapper.findComponent(BasicFilterCheckboxes).vm.$emit('updated')
    expect($_wrapper.emitted().updated).toBeTruthy()

    $_wrapper.findComponent(BasicFilterCheckboxes).vm.$emit('updated')
    expect($_wrapper.emitted().updated.length).toBe(2)
  })
})
