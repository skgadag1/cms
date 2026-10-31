import {shallowMount} from '@vue/test-utils'
import InfographicTable from '@/components/InfographicTable'

const bentoLayoutJson = JSON.stringify({
  tiles: [
    {
      id: '1',
      tileOption: 'title',
      contentTitle: '<p>Lift Capacity</p>',
      before: '<p>Upper</p>',
      after: '<p>Lower</p>',
      bgColor: 'black',
      responsive: {
        desktop: {x: 0, y: 0, w: 2, h: 1},
        tablet: {x: 0, y: 0, w: 3, h: 1},
        mobile: {x: 0, y: 0, w: 6, h: 1}
      }
    },
    {
      id: '2',
      tileOption: 'imgBg',
      imBg: 'https://res.cloudinary.com/demo/image/upload/sample.jpg',
      before: '<p>Powerful Engine</p>',
      bgColor: 'none',
      responsive: {
        desktop: {x: 2, y: 0, w: 4, h: 2},
        tablet: {x: 3, y: 0, w: 3, h: 2},
        mobile: {x: 0, y: 1, w: 6, h: 2}
      }
    },
    {
      id: '3',
      tileOption: 'img',
      img: 'https://res.cloudinary.com/demo/image/upload/icon.png',
      after: '<p>Speed</p>',
      bgColor: 'white',
      responsive: {
        desktop: {x: 0, y: 1, w: 2, h: 1},
        tablet: {x: 0, y: 1, w: 3, h: 1},
        mobile: {x: 0, y: 3, w: 6, h: 1}
      }
    }
  ]
})

function mountInfographicTable(propsData) {
  return shallowMount(InfographicTable, {
    propsData,
    stubs: {
      LazyHydrate: true
    },
    mocks: {
      $nuxt: {
        context: {
          $utils: {
            getCloudinaryAsset: jest.fn(),
            getCloudinaryAssetMetadata: jest.fn(() => ({publicId: '', metadata: {}}))
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
}

describe('InfographicTable component [bento layout]', () => {
  it('renders bento grid when listDataSource is "bento"', () => {
    const wrapper = mountInfographicTable({
      title: '<p>Track Loader</p>',
      listDataSource: 'bento',
      bentoLayout: bentoLayoutJson
    })

    const bento = wrapper.find('.infographic-table-bento')
    expect(bento.exists()).toBe(true)
    expect(wrapper.find('.infographic-table-elements').exists()).toBe(false)
  })

  it('renders one tile per entry in bentoLayout', () => {
    const wrapper = mountInfographicTable({
      listDataSource: 'bento',
      bentoLayout: bentoLayoutJson
    })

    expect(wrapper.findAll('.infographic-table-bento-tile').length).toBe(3)
  })

  it('renders title tile content via v-html', () => {
    const wrapper = mountInfographicTable({
      listDataSource: 'bento',
      bentoLayout: bentoLayoutJson
    })

    const titleTile = wrapper.find('.infographic-table-bento-tile-title')
    expect(titleTile.exists()).toBe(true)
    expect(titleTile.html()).toContain('Lift Capacity')
  })

  it('renders bg image tile with absolute image element', () => {
    const wrapper = mountInfographicTable({
      listDataSource: 'bento',
      bentoLayout: bentoLayoutJson
    })

    const bgImg = wrapper.find('.infographic-table-bento-tile-bg')
    expect(bgImg.exists()).toBe(true)
    expect(bgImg.attributes('src')).toBe('https://res.cloudinary.com/demo/image/upload/sample.jpg')
  })

  it('renders foreground image tile', () => {
    const wrapper = mountInfographicTable({
      listDataSource: 'bento',
      bentoLayout: bentoLayoutJson
    })

    const fgImg = wrapper.find('.infographic-table-bento-tile-img')
    expect(fgImg.exists()).toBe(true)
    expect(fgImg.attributes('src')).toBe('https://res.cloudinary.com/demo/image/upload/icon.png')
  })

  it('sets responsive grid CSS custom properties on each tile', () => {
    const wrapper = mountInfographicTable({
      listDataSource: 'bento',
      bentoLayout: bentoLayoutJson
    })

    const firstTile = wrapper.findAll('.infographic-table-bento-tile').at(0)
    const style = firstTile.attributes('style') || ''
    expect(style).toContain('--bento-d-col: 1 / span 2')
    expect(style).toContain('--bento-d-row: 1 / span 1')
    expect(style).toContain('--bento-t-col: 1 / span 3')
    expect(style).toContain('--bento-m-col: 1 / span 6')
  })

  it('falls back to elements view when listDataSource is "elements"', () => {
    const wrapper = mountInfographicTable({
      listDataSource: 'elements',
      bentoLayout: bentoLayoutJson,
      items: {'@nodes': []}
    })

    expect(wrapper.find('.infographic-table-bento').exists()).toBe(false)
    expect(wrapper.find('.infographic-table-elements').exists()).toBe(true)
  })

  it('falls back to elements view for legacy components without listDataSource', () => {
    const wrapper = mountInfographicTable({
      items: {'@nodes': []}
    })

    expect(wrapper.find('.infographic-table-bento').exists()).toBe(false)
    expect(wrapper.find('.infographic-table-elements').exists()).toBe(true)
  })

  it('renders an empty bento grid (no tiles) without crashing when bentoLayout is empty', () => {
    const wrapper = mountInfographicTable({
      listDataSource: 'bento',
      bentoLayout: ''
    })

    const bento = wrapper.find('.infographic-table-bento')
    expect(bento.exists()).toBe(true)
    expect(wrapper.findAll('.infographic-table-bento-tile').length).toBe(0)
  })

  it('handles malformed bentoLayout JSON without throwing', () => {
    const wrapper = mountInfographicTable({
      listDataSource: 'bento',
      bentoLayout: 'not-json'
    })

    expect(wrapper.find('.infographic-table-bento').exists()).toBe(true)
    expect(wrapper.findAll('.infographic-table-bento-tile').length).toBe(0)
  })
})
