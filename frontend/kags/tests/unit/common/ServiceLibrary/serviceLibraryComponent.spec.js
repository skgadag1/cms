import {mount} from '@vue/test-utils'
import ServiceLibrary from '@/components/common/ServiceLibrary/ServiceLibraryComponent'
import ServiceLibraryItem from '@/components/common/ServiceLibrary/ServiceLibraryItem'
import BasicGridTable from '@/components/common/BasicGridTable'
import ServiceLibraryFilters from '@/components/common/ServiceLibrary/ServiceLibraryFilters'
import ServiceLibraryFiltersMobile from '@/components/common/ServiceLibrary/ServiceLibraryFiltersMobile'
import config from '@/mixins/commonMixin'
import Vue from 'vue'
import $documentsData from '../../../../public/mocks/product-api/documents/documents-list.json'
import {BFormCheckbox} from 'bootstrap-vue'
import flushPromises from 'flush-promises'
import {bdService} from '@/services'
import OverlayWrapper from '@/components/common/OverlayWrapper'
import {__createMocks} from '@/store/mocks'

let $_wrapper
let _testData = {
  'total': 0,
  'items': [],
  'aggr': {
    'bulletin_filter': {
      'doc_count': 0,
      'buckets': [
        {
          'key': 'Field Modifications',
          'doc_count': 0
        }
      ]
    }
  }
}

beforeEach(() => {
  $_wrapper = mount(ServiceLibrary, {
    mixins: [config(Vue)],
    store: __createMocks().store,
    propsData: {
      disabled: false,
      config: {}
    }
  })
})

afterEach(() => {
  $_wrapper.destroy()
})

describe('components/common/ServiceLibrary/ServiceLibraryComponent.vue', () => {
  it('is vue instance', () => {
    expect($_wrapper.vm).toBeTruthy()
  })

  it('shows spinner when component is created', () => {
    expect($_wrapper.findComponent(OverlayWrapper).exists()).toBe(true)
    expect($_wrapper.findComponent(OverlayWrapper).vm.show).toBe(true)
  })

  it('shows noResults message when no data', async () => {
    await $_wrapper.setData({loading: false})
    expect($_wrapper.find('[data-test-id="no-results-message"]').exists()).toBe(true)
  })

  it('properly renders table', async () => {
    await $_wrapper.setData({loading: false, items: $documentsData.items})
    expect($_wrapper.findComponent(BasicGridTable).exists()).toBe(true)
    expect($_wrapper.findAllComponents(ServiceLibraryItem).length).toBe($documentsData.items.length)
  })

  it('facet state is synchronized', async () => {
    bdService.fetchDocuments = jest.fn().mockResolvedValue(_testData)
    $_wrapper.vm.fetchDocuments()
    await flushPromises()
    expect($_wrapper.findComponent(ServiceLibraryFilters).findAllComponents(BFormCheckbox).length).toBe(1)
    expect($_wrapper.findComponent(ServiceLibraryFiltersMobile).findComponent(BFormCheckbox).exists()).toBe(true)
    expect($_wrapper.findComponent(ServiceLibraryFilters).findComponent(BFormCheckbox).vm.isChecked).toBe(false)
    await $_wrapper.setData({facetFilter: {bulletin_filter: ['Field Modifications']}})
    expect($_wrapper.findComponent(ServiceLibraryFilters).findComponent(BFormCheckbox).vm.isChecked).toBe(true)
    expect($_wrapper.findComponent(ServiceLibraryFiltersMobile).findComponent(BFormCheckbox).vm.isChecked).toBe(true)
  })

  it('facet disabled on counted zero', async () => {
    bdService.fetchDocuments = jest.fn().mockResolvedValue(_testData)
    await $_wrapper.vm.fetchDocuments()
    expect($_wrapper.findAllComponents(BFormCheckbox).length).toEqual(2)
    expect($_wrapper.findComponent(ServiceLibraryFilters).findComponent(BFormCheckbox).vm.isDisabled).toEqual(true)
  })
})
