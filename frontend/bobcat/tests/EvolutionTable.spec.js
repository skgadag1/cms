import {mount} from '@vue/test-utils'
import EvolutionTable from '@/components/EvolutionTable'

const link = {type: 'none', target: '_self', pageLink: '', externalUrl: ''}

const tableConfig = {
  background: 'white',
  tableColorScheme: 'black',
  showMore: true,
  showMoreRowLimit: 1,
  headers: [
    {id: 'work', label: 'Work', description: '', link},
    {id: 'core', label: 'Core', description: '', link}
  ],
  sections: [
    {
      id: 'standard-features',
      title: 'Standard Features',
      rows: [
        {
          id: 'power-bob-tach',
          label: 'Power Bob-Tach',
          labelColor: 'default',
          link,
          cells: [
            {id: 'c1', type: 'check', link},
            {id: 'c2', type: 'dash', link}
          ]
        }
      ]
    },
    {
      id: 'main-package-differences',
      title: 'Main Package Differences',
      rows: [
        {
          id: 'suspension-seat',
          label: 'Suspension Seat',
          labelColor: 'default',
          link,
          cells: [
            {id: 'c3', type: 'text', value: 'Vinyl', link},
            {id: 'c4', type: 'text', value: 'OPT', link}
          ]
        }
      ]
    },
    {
      id: 'package-options',
      title: 'Package Options',
      rows: [
        {
          id: 'cab',
          label: 'Cab',
          labelColor: 'default',
          link,
          cells: [
            {id: 'c5', type: 'text', value: 'STD', link},
            {id: 'c6', type: 'text', value: 'Display', link}
          ]
        }
      ]
    }
  ],
  disclaimers: []
}

const labels = {
  included: 'Included',
  notApplicable: 'Not Applicable',
  optional: 'Optional',
  standard: 'Standard',
  showMore: 'Show More',
  showLess: 'Show Less'
}

const mountTable = () => mount(EvolutionTable, {
  propsData: {
    nodeId: 't86',
    title: 'T86-2',
    titleAccent: 'PACKAGES',
    tableConfig
  },
  mocks: {
    $nuxt: {
      context: {
        $utils: {},
        $regions: {current: {name: 'na', language: {name: 'en'}}},
        $ga4: {events: {table_click: jest.fn()}},
        $services: {
          bobcatapi: {
            getConcepts: jest.fn().mockResolvedValue(labels)
          }
        }
      }
    }
  },
  stubs: {
    LazyHydrate: {template: '<div><slot /></div>'},
    DynamicButton: {template: '<button type="button" @click="$emit(\'click\', $event)"><slot /></button>'},
    'b-icon': true,
    ImageSet: true
  }
})

const mountedTable = async () => {
  const wrapper = mountTable()
  await wrapper.vm.$nextTick()
  await wrapper.vm.$nextTick()
  return wrapper
}

describe('EvolutionTable extraction markup', () => {
  it('keeps later sections in the HTML while they are visually collapsed', async () => {
    const wrapper = await mountedTable()
    const html = wrapper.html()

    expect(html).toContain('Main Package Differences')
    expect(html).toContain('Package Options')
    expect(html).toContain('Suspension Seat')
    expect(html).toContain('Vinyl')
    expect(html).toContain('Cab')
    expect(html).toContain('Display')
    expect(wrapper.find('#evolution-table-t86-main-package-differences').text()).toContain('Suspension Seat')
    expect(wrapper.find('#evolution-table-t86-package-options').text()).toContain('Cab')
    expect(wrapper.find('[data-test-id="evolution-table-show-more"]').attributes('aria-expanded')).toBe('false')
    expect(wrapper.findAll('.evolution-table__collapsed').length).toBeGreaterThan(0)
  })

  it('exposes status text from functional labels', async () => {
    const wrapper = await mountedTable()
    const cells = wrapper.findAll('.evolution-table__cell')

    expect(cells.at(0).text()).toBe('Included')
    expect(cells.at(0).html()).not.toContain('aria-label="Included"')
    expect(cells.at(1).text()).toBe('Not Applicable')
  })

  it('expands OPT and STD using functional labels', async () => {
    const wrapper = await mountedTable()
    const html = wrapper.html()

    expect(html).toContain('Optional')
    expect(html).toContain('Standard')
  })

  it('uses column, row, and group headers', async () => {
    const wrapper = await mountedTable()

    expect(wrapper.find('caption').text()).toContain('T86-2')
    expect(wrapper.find('caption').text()).toContain('Work')
    expect(wrapper.find('caption').text()).not.toContain('Compare')
    expect(wrapper.findAll('thead th[scope="col"]').length).toBe(3)
    expect(wrapper.find('th[scope="row"]').text()).toContain('Power Bob-Tach')
    expect(wrapper.findAll('th[scope="rowgroup"]').at(1).text()).toContain('Main Package Differences')
  })

  it('only changes visibility when Show More is selected', async () => {
    const wrapper = await mountedTable()
    const before = wrapper.find('#evolution-table-t86-package-options').html()

    await wrapper.find('[data-test-id="evolution-table-show-more"]').trigger('click')

    expect(wrapper.find('[data-test-id="evolution-table-show-more"]').attributes('aria-expanded')).toBe('true')
    expect(wrapper.find('.evolution-table__collapsed').exists()).toBe(false)
    expect(wrapper.find('#evolution-table-t86-package-options').html()).toContain('Cab')
    expect(before).toContain('Cab')
  })
})
