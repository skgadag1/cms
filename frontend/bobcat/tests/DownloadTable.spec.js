import {mount} from '@vue/test-utils'
import {BTable} from 'bootstrap-vue'
import cloudinary from './utils/cloudinary.mock'
import DownloadTable from '@/components/DownloadTable'
import mock from '~/server/responses/bobcat-api/pages/components/downloadTable'
import WarningAlert from '@/components/elements/WarningAlert'

const props = mock.main['0']
const wrapper = mount(DownloadTable, {
  propsData: {
    rows: props.rows,
    title: props.title,
    fileNameLabel: props.fileNameLabel,
    headerLabel: props.headerLabel,
    versionLabel: props.versionLabel,
    downloadLinkLabel: props.downloadLinkLabel,
    downloadLabel: props.downloadLabel
  },
  mocks: {
    $utils: {
      ...cloudinary.$utils
    }
  },
  stubs: {
    'b-icon': true,
    'b-table': BTable,
    LazyHydrate: true,
    WarningAlert
  }
})

const mappedRows = props?.rows['@nodes'].map(x => ({
  fileName: props.rows[x]?.fileName,
  version: props.rows[x]?.version,
  file: props.rows[x]?.file
}))

describe('Download-table component [elements]', () => {
  it('has title with h2 tag', () => {
    expect(wrapper.find('h2[data-test-id="download-table-title"]').text()).toEqual(props.title)
  })

  it('has header title with h5 tag', () => {
    expect(wrapper.find('div[data-test-id="download-table-header"] h5').text()).toEqual(props.headerLabel)
  })

  it('renders as many rows as has in props', () => {
    expect(wrapper.findAll('tr[role="row"]').length - 1).toEqual(mappedRows.length)
  })

  it('has head fileName with label mocked in props', () => {
    expect(wrapper.findAll('th[role="columnheader"]').at(0).find('p').text()).toEqual(props.fileNameLabel)
  })

  it('has head version with label mocked in props', () => {
    expect(wrapper.findAll('th[role="columnheader"]').at(1).find('p').text()).toEqual(props.versionLabel)
  })

  it('has head file with label mocked in props', () => {
    expect(wrapper.findAll('th[role="columnheader"]').at(2).find('p').text()).toEqual(props.downloadLinkLabel)
  })

  it('has label for download link', () => {
    expect(wrapper.find('a[data-test-id="download-table-download-button"]').text()).toEqual(props.downloadLabel)
  })

  it('download link has download icon ', () => {
    expect(wrapper.find('a[data-test-id="download-table-download-button"] span b-icon-stub').exists()).toBe(true)
  })

  it('If there is no rows alert appears ', async () => {
    await wrapper.setData({mappedRows: undefined})
    await wrapper.setData({inEditorEdit: true})
    await wrapper.vm.$forceUpdate()
    expect(wrapper.find('div[data-test-id="download-table-alert"]').exists()).toBe(true)
  })

  it('If there is no header in props, header element will not appear ', async () => {
    await wrapper.setProps({headerLabel: ''})
    expect(wrapper.find('div[data-test-id="download-table-header"]').exists()).toBe(false)
  })

  it('has LazyHydrate component', () => {
    const lh = wrapper.find('lazyhydrate-stub')
    expect(lh.exists()).toBe(true)
  })
})
