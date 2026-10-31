import {shallowMount} from '@vue/test-utils'
import {BDropdownItem, BDropdown} from 'bootstrap-vue'
import SubnavigationMain from '@/components/elements/Navigation/SubnavigationMain'
import MegaList from '@/components/elements/Navigation/MegaList'
import MegaListColumn from '@/components/elements/Navigation/MegaListColumn'
import MegaListColumnWrapper from '@/components/elements/Navigation/MegaListColumnWrapper'
import MegaMenu from '@/components/elements/Navigation/MegaMenu'
import MegaMenuItem from '@/components/elements/Navigation/MegaMenuItem'
import mock from '@/server/responses/content-api/navigationResponse'
import pathUtils from '@/plugins/utils/path'
import linkUtils from '~/plugins/utils/linkUtils'

const addActive = (items) => {
  if (Array.isArray(items)) {
    items.map((menuItem) => {
      menuItem.isActive = false
      return addActive(menuItem.items)
    })
  }
  return items
}

const items = addActive(mock)

const wrapper = shallowMount(SubnavigationMain, {
  propsData: {
    items
  },
  mocks: {
    $nuxt: {
      context: {
        $utils: {
          ...pathUtils,
          ...linkUtils
        },
        $regions: {
          current: {
            language: {
              name: 'en'
            },
            name: 'na'
          }
        },
        $breakpoints: {
          mobile: true,
          tablet: false,
          desktop: true
        }
      }
    },
    $router: {
      currentRoute: {
        path: '/bobcat/na/equipment/subpage/Compact-Track-Loaders'
      }
    },
    $utils: {
      ...pathUtils,
      ...linkUtils
    },
    $regions: {
      current: {
        language: {
          name: 'en'
        },
        name: 'na'
      }
    }
  },
  stubs: {
    MegaList,
    MegaListColumn,
    MegaListColumnWrapper,
    MegaMenu,
    MegaMenuItem,
    LazyHydrate: true,
    NuxtLink: true,
    'b-dropdown': BDropdown,
    'b-dropdown-item': BDropdownItem,
    'b-icon': true,
    'b-row': true,
    'b-col': true
  }
})

const ul = wrapper.find('[data-test-id="subnavigation-main"]')
const lvl1 = ul.findAll('li.dropdown-nav-link')
const megaMenuSection = wrapper.find('[data-test-id="megamenu-section"]')
const MegaMenuItemElements = megaMenuSection.findAll('[data-test-id^="megamenu-item__"]')

describe('SubnavigationMain component [data props]', () => {
  it('has renders all lvl1 items', () => {
    expect(lvl1.length).toEqual(items.length)
  })

  it('renders with proper order and labels', () => {
    for (const subNavItem in lvl1.wrappers) { expect(lvl1.at(subNavItem).text()).toMatch(items[subNavItem].label) }
  })

  it('renders all 2 megamenus', () => {
    const allMegaLength = items.filter(item => item.type === 'megaMenu' && item.rows.length).length
    expect(MegaMenuItemElements.exists()).toBe(true)
    expect(MegaMenuItemElements.wrappers.length).toEqual(allMegaLength)
  })

  it('should render one dropdown menu', () => {
    const dropdown = wrapper.findAllComponents(BDropdown)
    expect(dropdown.exists()).toBe(true)
    expect(dropdown.wrappers.length).toEqual(1)
  })

  it('should not render hidden element', () => {
    expect(lvl1.at(1).text()).not.toBe('attachments')
  })
})

describe('SubnavigationMain component [states]', () => {
  it('renders 0 active state', () => {
    expect(wrapper.findAll('.active').length).toBe(0)
    expect(wrapper.findAll('.active').length).toBe(items.filter(x => x.isActive).length)
  })

  it('renders all active states', async () => {
    // watch out, based on mock
    items[3].isActive = true
    items[3].rows[0].isActive = true
    items[3].rows[0].columns[0].isActive = true
    items[3].rows[0].columns[0].items[0].isActive = true
    await lvl1.at(3).find('button').trigger('click')
    expect(ul.findAll('.active').length).toBe(items.filter(x => x.isActive).length)
  })
})

describe('SubnavigationMain component [logic]', () => {
  it('opens proper megamenu after click', async () => {
    const megamenuId = items[3].id
    if (!lvl1.at(3).classes('opened')) { await lvl1.at(3).find('button').trigger('click') }
    const mms = megaMenuSection.find(`[data-test-id^="megamenu-item__${megamenuId}"]`)
    expect(wrapper.vm.activeMenu).toMatch(megamenuId)
    expect(wrapper.find('.overlay').isVisible()).toBe(true)
    expect(mms.isVisible()).toBe(true)

    if (!lvl1.at(4).classes('opened')) { await lvl1.at(4).find('button').trigger('click') }
    expect(wrapper.vm.activeMenu).not.toMatch(megamenuId)
    expect(mms.classes()).toContain('megamenu-hidden') // this class makes it hidden
  })

  it('close properly megamenu on overlay click', async () => {
    expect(wrapper.find('.overlay').isVisible()).toBe(true)

    await wrapper.find('.overlay').trigger('click')
    expect(wrapper.find('.overlay').exists()).toBe(false)
  })

  it('close properly megamenu when opening another', async () => {
    let megamenuId = items[3].id
    if (!lvl1.at(4).classes('opened')) { await lvl1.at(4).find('button').trigger('click') }
    let mms = megaMenuSection.find(`[data-test-id^="megamenu-item__${megamenuId}"]`)
    expect(mms.findAll('b-col-stub').length).toBe(9)

    megamenuId = items[3].id
    if (!lvl1.at(3).classes('opened')) { await lvl1.at(3).find('button').trigger('click') }
    mms = megaMenuSection.find(`[data-test-id^="megamenu-item__${megamenuId}"]`)
    expect(mms.findAll('b-col-stub').length).toBe(9)
  })
})

describe('SubnavigationMain component [items]', () => {
  it('render proper labels - lvl1', () => {
    expect(lvl1.at(0).text()).toEqual(items[0].label)
    expect(lvl1.at(1).text()).toEqual(items[1].label)
    expect(lvl1.at(2).find('button:first-child').text()).toEqual(items[2].label)
    expect(lvl1.at(3).find('button:first-child').text()).toEqual(items[3].label)
  })

  it('render proper labels - lvl3', () => {
    // watch out, based on mock
    const lvl3 = wrapper.findAll('[data-test-id="megamenu-item__12356779"] [data-test-id^="subnavigation-megamenu-lvl3-12356779__"]')
    const lvl3items = [
      ...items[3].rows[0].columns[0].items,
      ...items[3].rows[0].columns[2].items, // it is ordered by item.position!
      ...items[3].rows[0].columns[1].items,
      ...items[3].rows[1].columns[1].items, // it is ordered by item.position!
      ...items[3].rows[1].columns[0].items,
      ...items[3].rows[2].columns[0].items
    ]

    for (const i in lvl3items.lenght) { expect(lvl3.at(i).text()).toEqual(lvl3items[i].label) }
  })
})
