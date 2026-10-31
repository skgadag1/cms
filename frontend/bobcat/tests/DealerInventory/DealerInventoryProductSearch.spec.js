import {shallowMount} from '@vue/test-utils'
import DealerInventoryProductSearch from '@/components/elements/Dealer/DealerInventoryProductSearch'
import DropdownAdvanced from '@/components/elements/Form/DropdownAdvanced'

const _props = {
  options: [
    {irwName: 'compact-excavator', name: 'Compact Excavator'},
    {irwName: 'zero-turn-mower', name: 'Zero-Turn Mower'},
    {irwName: 'compact-track-loader', name: 'Compact Track Loader'},
    {irwName: 'skid-steer-loader', name: 'Skid-Steer Loader'}
  ],
  productDefaultValues: JSON.stringify([
    {category: 'Loaders', values: ['Skid-Steer Loader', 'Compact Track Loader']},
    {category: 'Excavators', values: ['Compact Excavator']}
  ]),
  othersCategoryLabel: 'Other Equipment'
}

const factory = (propsData = _props) => shallowMount(DealerInventoryProductSearch, {
  propsData,
  stubs: {
    'b-button': {template: '<button :disabled="disabled" @click="$emit(\'click\')"><slot /></button>', props: ['disabled']}
  }
})

const findDropdown = wrapper => wrapper.findComponent(DropdownAdvanced)

describe('DealerInventoryProductSearch', () => {
  it('renders authored labels without English fallbacks', () => {
    const wrapper = factory({
      ..._props,
      headline: 'Encuentre equipos en stock hoy',
      description: '<p>Seleccione un tipo de producto.</p>',
      searchLabel: 'Buscar'
    })
    expect(wrapper.find('[data-test-id="dl-inv-product-search-headline"]').text()).toBe('Encuentre equipos en stock hoy')
    expect(wrapper.find('[data-test-id="dl-inv-product-search-description"]').html()).toContain('Seleccione un tipo de producto.')
    expect(wrapper.find('[data-test-id="dl-inv-product-search-button"]').text()).toBe('Buscar')
    expect(wrapper.text()).not.toMatch(/Find equipment|Select a product type|Note:/)
  })

  it('renders no description when none is authored', () => {
    const wrapper = factory()
    expect(wrapper.find('[data-test-id="dl-inv-product-search-description"]').exists()).toBe(false)
  })

  it('groups options by authored categories in authored order, unmatched last', () => {
    const wrapper = factory()
    expect(wrapper.vm.dropdownOptions).toEqual([
      {
        category: 'Loaders',
        values: [
          {label: 'Skid-Steer Loader', value: 'skid-steer-loader', selected: false},
          {label: 'Compact Track Loader', value: 'compact-track-loader', selected: false}
        ]
      },
      {category: 'Excavators', values: [{label: 'Compact Excavator', value: 'compact-excavator', selected: false}]},
      {category: 'Other Equipment', values: [{label: 'Zero-Turn Mower', value: 'zero-turn-mower', selected: false}]}
    ])
  })

  it('renders a flat list when no categories are authored', () => {
    const wrapper = factory({options: _props.options})
    expect(wrapper.vm.dropdownOptions).toHaveLength(1)
    expect(wrapper.vm.dropdownOptions[0].category).toBe('')
    expect(wrapper.vm.dropdownOptions[0].values).toHaveLength(4)
  })

  it('regroups options when the authored categories change', async () => {
    const wrapper = factory()
    await wrapper.setProps({productDefaultValues: JSON.stringify([{category: 'Mowers', values: ['Zero-Turn Mower']}])})
    expect(wrapper.vm.dropdownOptions.map(group => group.category)).toEqual(['Mowers', 'Other Equipment'])
  })

  it('filters options by keyword', async () => {
    const wrapper = factory()
    wrapper.vm.filterValues('loader')
    await wrapper.vm.$nextTick()
    expect(wrapper.vm.dropdownOptions).toHaveLength(1)
    expect(wrapper.vm.dropdownOptions[0].values.map(o => o.value)).toEqual(['skid-steer-loader', 'compact-track-loader'])
  })

  it('keeps search disabled until a product is selected, then emits its irwName', async () => {
    const wrapper = factory()
    const button = wrapper.find('[data-test-id="dl-inv-product-search-button"]')
    expect(button.attributes('disabled')).toBe('disabled')

    findDropdown(wrapper).vm.$emit('changed', [{label: 'Compact Excavator', value: 'compact-excavator'}])
    await wrapper.vm.$nextTick()
    expect(button.attributes('disabled')).toBeUndefined()
    expect(wrapper.vm.dropdownOptions[1].values[0].selected).toBe(true)

    await button.trigger('click')
    expect(wrapper.emitted('search')).toEqual([['compact-excavator']])
  })

  it('disables search again when the product is deselected', async () => {
    const wrapper = factory()
    findDropdown(wrapper).vm.$emit('changed', [{label: 'Compact Excavator', value: 'compact-excavator'}])
    findDropdown(wrapper).vm.$emit('changed', [])
    await wrapper.vm.$nextTick()
    expect(wrapper.find('[data-test-id="dl-inv-product-search-button"]').attributes('disabled')).toBe('disabled')
  })
})
