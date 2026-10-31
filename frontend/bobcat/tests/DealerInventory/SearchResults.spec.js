import {shallowMount} from '@vue/test-utils'
import SearchResults from '@/components/SearchResults'
import mockedSearch from '~/server/responses/content-api/components/DealerSearch.js'

const _props = {
  results: [],
  selectedModels: [],
  noResultsMsg: 'no results',
  selectedType: {label: 'test', irwName: 'test'},
  distance: 30.9,
  removeModel: () => {
  },
  removeType: () => {
  },
  sorting: mockedSearch?.facet?.['@nodes'].map(name => mockedSearch.facet[name]).filter(x => x.sorting === 'true')
}

const options = {
  propsData: _props,
  stubs: {
    'b-overlay': true,
    'b-icon': true,
    'b-form': true,
    'b-form-input': true,
    'b-badge': true,
    LazyHydrate: true,
    FacetsChips: true,
    SearchProduct: true,
    SwitchDropdown: true,
    WarningAlert: true
  },
  mocks: {
    $fetchState: {pending: false},
    $nuxt: {
      context: {
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
            language: 'en'
          }
        }
      }
    }
  }
}

const _results = [{
  name: 'B54911187',
  status: 'IN_STOCK',
  prices: [],
  dealer: {
    arNumber: '01220',
    name: 'R. S. Braswell Company, Kannapolis, NC',
    distance: 30.9,
    unitOfLength: 'miles',
    shippingAddress: {street: '485 SOUTH CANNON BLVD', city: 'KANNAPOLIS', zipcode: '28083', state: 'NC', country: 'US'}
  },
  label: 'CT1025HSTD'
}, {
  name: 'B54811055',
  status: 'IN_STOCK',
  prices: [],
  dealer: {
    arNumber: '02141',
    name: 'Piedmont Bobcat, Kernersville, NC',
    distance: 31.2,
    unitOfLength: 'miles',
    shippingAddress: {street: '1015 NC HWY 66 S', city: 'KERNERSVILLE', zipcode: '27285', state: 'NC', country: 'US'}
  },
  label: 'CT1021HSTD',
  image: {src: 'cloudinary:bobcat-ct1021-s6c1267-19d2-fc-ko-544x362#eyJhc3BlY3RfcmF0aW8iOjEuNTAyNzYsImFzc2V0X2ZvbGRlciI6ImJvYmNhdC1hc3NldHMvbmFvLWFwcHJvdmVkL2NvbXBhY3QtdHJhY3RvcnMiLCJjcmVhdGVkX2F0IjoiMjAyMi0wOS0zMFQxNTo0NzowMCswMDowMCIsImFzc2V0X2lkIjoiNzVmYThjZDI3ZjUzNzFhZDU4MjEyZDdmN2VlZmE0YmUiLCJ0eXBlIjoidXBsb2FkIiwicHVibGljX2lkIjoiYm9iY2F0LWN0MTAyMS1zNmMxMjY3LTE5ZDItZmMta28tNTQ0eDM2MiIsInVwbG9hZGVkX2J5Ijp7ImFjY2Vzc19rZXkiOiI4NDM0MzIyMzExNDE4MTIiLCJjdXN0b21faWQiOiJkZWIubHVlYmNob3dAZG9vc2FuLmNvbSIsImV4dGVybmFsX2lkIjoiMTMzZjVhOGRhODcwNjZmM2VhNjQ5MDU1ZWJkM2I0In0sImNvbnRleHQiOnt9LCJoZWlnaHQiOjM2MiwiZm9ybWF0IjoianBnIiwicmVzb3VyY2VfdHlwZSI6ImltYWdlIiwic2VjdXJlX3VybCI6Imh0dHBzOi8vcmVzLmNsb3VkaW5hcnkuY29tL2Rvb3Nhbi1ib2JjYXQtZGV2L2ltYWdlL3VwbG9hZC92MTY2NDU1MjgyMC9ib2JjYXQtY3QxMDIxLXM2YzEyNjctMTlkMi1mYy1rby01NDR4MzYyLmpwZyIsImJhY2t1cF9ieXRlcyI6OTM3NDEsImRpc3BsYXlfbmFtZSI6ImJvYmNhdC1jdDEwMjEtczZjMTI2Ny0xOWQyLWZjLWtvLTU0NHgzNjIiLCJ2ZXJzaW9uIjoxNjY0NTUyODIwLCJhY2Nlc3NfbW9kZSI6InB1YmxpYyIsImNyZWF0ZWRfYnkiOnsiYWNjZXNzX2tleSI6Ijg0MzQzMjIzMTE0MTgxMiIsImN1c3RvbV9pZCI6ImRlYi5sdWViY2hvd0Bkb29zYW4uY29tIiwiZXh0ZXJuYWxfaWQiOiIxMzNmNWE4ZGE4NzA2NmYzZWE2NDkwNTVlYmQzYjQifSwidXJsIjoiaHR0cDovL3Jlcy5jbG91ZGluYXJ5LmNvbS9kb29zYW4tYm9iY2F0LWRldi9pbWFnZS91cGxvYWQvdjE2NjQ1NTI4MjAvYm9iY2F0LWN0MTAyMS1zNmMxMjY3LTE5ZDItZmMta28tNTQ0eDM2Mi5qcGciLCJmaWxlbmFtZSI6ImJvYmNhdC1jdDEwMjEtczZjMTI2Ny0xOWQyLWZjLWtvLTU0NHgzNjIiLCJwaXhlbHMiOjE5NjkyOCwidXBsb2FkZWRfYXQiOiIyMDIyLTA5LTMwVDE1OjQ3OjAwKzAwOjAwIiwiYnl0ZXMiOjkzNzQxLCJ3aWR0aCI6NTQ0LCJldGFnIjoiZTVjM2I1MDI0MzM0NzM1NjE5ODMyMzI1MzQ3YWI4NTUiLCJzdGF0dXMiOiJhY3RpdmUifQ=='},
  keyProperties: []
}, {
  name: 'B54911258',
  status: 'COMING_SOON',
  prices: [],
  dealer: {
    arNumber: '01220',
    name: 'R. S. Braswell Company, Kannapolis, NC',
    distance: 30.9,
    unitOfLength: 'miles',
    shippingAddress: {street: '485 SOUTH CANNON BLVD', city: 'KANNAPOLIS', zipcode: '28083', state: 'NC', country: 'US'}
  },
  label: 'CT1025HSTD'
}]

const getWrapper = (opt = options) => shallowMount(SearchResults, opt)
const wrapper = getWrapper()

describe('SearchResults component [data props]', () => {
  it('shows no results message', () => {
    expect(wrapper.find('[data-test-id="dinv-search-noresults"] div').text()).toBe(_props.noResultsMsg)
  })

  it('displays sorting dropdown if options', () => {
    expect(wrapper.find('[data-test-id="dinv-search-sorting"]').isVisible()).toBe(true)
    expect(wrapper.find('[data-test-id="dinv-search-sorting"]').attributes('options').length).toBeGreaterThan(0)
  })

  it('displays disabled sorting dropdown when no results', () => {
    expect(wrapper.find('[data-test-id="dinv-search-sorting"]').attributes('disabled')).toBe('true')
  })

  it('display proper results count', async () => {
    await wrapper.setProps({results: _results})
    const countDesktop = wrapper.find('[data-test-id="dinv-search-results-text-desktop"]')
    const countMobile = wrapper.find('[data-test-id="dinv-search-results-text-desktop"]')
    expect(countDesktop.exists()).toBe(true)
    expect(countMobile.exists()).toBe(true)
    expect(countDesktop.text().startsWith(`${_results.length} `)).toBe(true)
  })

  it('display active sorting dropdown', async () => {
    expect(wrapper.find('[data-test-id="dinv-search-sorting"]').isVisible()).toBe(true)
    expect(wrapper.find('[data-test-id="dinv-search-sorting"]').attributes('disabled')).toBeFalsy()
  })

  it('display pagination', async () => {
    await wrapper.setProps({results: [..._results, ..._results, ..._results, ..._results, ..._results, ..._results, ..._results]})
    expect(wrapper.find('[data-test-id="dinv-search-pagination"').isVisible()).toBe(true)
    expect(wrapper.findAll('[data-test-id^="dinv-search-pagination"').length).toBe(3)
  })
})
