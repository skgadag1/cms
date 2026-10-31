import {ref} from '@nuxtjs/composition-api'
import {shallowMount} from '@vue/test-utils'
import cloudinary from './utils/cloudinary.mock'
import EvolutionAttachmentPricing from '@/components/EvolutionAttachmentPricing'
import linkUtils from '~/plugins/utils/linkUtils'

const mockAttachmentModel = {
  '@name': 'test-attachment',
  '@nodeType': 'dxp:attachment',
  title: 'Test Attachment',
  lifecycle: 'Current',
  attachmentType: {label: 'Bucket'},
  compatibleMachines: {
    machines: [
      {name: 'Machine A'},
      {name: 'Machine B'},
      {name: 'Machine C'},
      {name: 'Machine D'}
    ]
  },
  images: {
    '@nodes': ['images0'],
    images0: {
      '@id': 'img-1',
      '@name': 'images0',
      asset: {
        field: 'img',
        img: {src: 'cloudinary:test/image#{}'}
      }
    }
  }
}

const mockAttachmentSubcategory = {
  '@name': 'angle-blade',
  '@nodeType': 'dxp:attachmentGroup',
  '@nodes': ['variant1'],
  title: 'Angle Blade',
  image: {
    '@id': 'subcategory-image',
    src: 'cloudinary:test/subcategory-image#{}',
    imageAlt: 'Subcategory image'
  },
  images: {
    '@nodes': ['images0', 'images1'],
    images0: {
      '@id': 'asset-0',
      '@name': 'images0',
      asset: {field: 'img', img: {src: 'cloudinary:test/asset-0#{}'}}
    },
    images1: {
      '@id': 'asset-1',
      '@name': 'images1',
      asset: {field: 'img', img: {src: 'cloudinary:test/asset-1#{}'}}
    }
  },
  variant1: {
    '@name': 'variant1',
    '@nodeType': 'dxp:attachment',
    title: 'Variant 1',
    lifecycle: 'Current'
  }
}

const mockAttachmentModelWithVariants = {
  '@name': 'attachment-group',
  '@nodeType': 'dxp:attachmentGroup',
  '@nodes': ['variant1', 'variant2', 'variant3'],
  variant1: {
    '@name': 'variant1',
    '@nodeType': 'dxp:attachment',
    title: 'Variant 1',
    lifecycle: 'Current'
  },
  variant2: {
    '@name': 'variant2',
    '@nodeType': 'dxp:attachment',
    title: 'Variant 2',
    lifecycle: 'Current'
  },
  variant3: {
    '@name': 'variant3',
    '@nodeType': 'dxp:attachment',
    title: 'Variant 3',
    lifecycle: 'Current'
  }
}

const defaultProps = {
  nodeId: 'attachment-pricing-01',
  title: '<span>Attachment Title</span>',
  subtitle: '<span>Subtitle</span>',
  htag: 'h2'
}

const options = {
  propsData: defaultProps,
  provide: {
    model: ref(mockAttachmentModel),
    selectedVariant: ref('test-attachment')
  },
  mocks: {
    $nuxt: {
      context: {
        ...cloudinary,
        $utils: {
          ...cloudinary.$utils,
          ...linkUtils
        },
        $ga4: {
          events: {
            product_page_cta: jest.fn()
          }
        },
        $services: {
          bobcatapi: {
            getConcepts: jest.fn().mockResolvedValue({
              sizes: 'Available sizes',
              compatibleMachines: 'Compatible Machines:',
              more: 'More',
              attachmentImageDisclaimer: 'Attachment image disclaimer.',
              selectVariant: 'Select'
            })
          }
        },
        $regions: {
          current: {name: 'en', label: 'english', language: {name: 'en'}}
        },
        $breakpoints: {
          mobile: false,
          tablet: false,
          desktop: true
        },
        $config: {}
      }
    }
  },
  stubs: {
    LazyHydrate: true,
    CloudinaryGallery: '<div data-test-id="cloudinary-gallery"/>',
    ImageSet: '<div data-test-id="image-set"/>',
    WarningAlert: true,
    SwitchDropdown: {name: 'SwitchDropdown', template: '<div data-test-id="switch-dropdown"/>'},
    DynamicButton: {template: '<span data-test-id="cta-button"><slot /></span>'}
  }
}

const getWrapper = (opt = options) => shallowMount(EvolutionAttachmentPricing, opt)

describe('EvolutionAttachmentPricing component', () => {
  it('renders section with data-test-id attachment-pricing', () => {
    const wrapper = getWrapper()
    expect(wrapper.find('[data-test-id="attachment-pricing"]').exists()).toBe(true)
  })

  it('renders desktop title when desktop breakpoint', () => {
    const wrapper = getWrapper()
    expect(wrapper.find('[data-test-id="model-pricing-title__desktop"]').exists()).toBe(true)
  })

  it('renders mobile title when mobile breakpoint', () => {
    const opts = {
      ...options,
      mocks: {
        ...options.mocks,
        $nuxt: {
          context: {
            ...options.mocks.$nuxt.context,
            $breakpoints: {mobile: true, tablet: false, desktop: false}
          }
        }
      }
    }
    const wrapper = getWrapper(opts)
    expect(wrapper.find('[data-test-id="attachment-pricing-title__mobile"]').exists()).toBe(true)
  })

  it('displays model variant title', () => {
    const wrapper = getWrapper()
    expect(wrapper.text()).toContain('Test Attachment')
  })

  it('renders CloudinaryGallery when images exist', () => {
    const wrapper = getWrapper()
    expect(wrapper.find('[data-test-id="cloudinary-gallery"]').exists()).toBe(true)
  })

  it('renders ImageSet when no images', () => {
    const modelNoImages = ref({
      ...mockAttachmentModel,
      images: {'@nodes': []}
    })
    const wrapper = getWrapper({
      ...options,
      provide: {model: modelNoImages, selectedVariant: ref('test-attachment')}
    })
    expect(wrapper.find('[data-test-id="image-set"]').exists()).toBe(true)
  })

  it('combines the subcategory image with all of its gallery assets', () => {
    const wrapper = getWrapper({
      ...options,
      provide: {model: ref(mockAttachmentSubcategory), selectedVariant: ref(null)}
    })
    const srcs = wrapper.vm.images.map(img => img.src)
    expect(srcs).toEqual([
      'cloudinary:test/subcategory-image#{}',
      'cloudinary:test/asset-0#{}',
      'cloudinary:test/asset-1#{}'
    ])
  })

  it('keeps subcategory media even when a variant with its own images is selected', () => {
    const subcategoryWithVariantImages = {
      ...mockAttachmentSubcategory,
      variant1: {
        ...mockAttachmentSubcategory.variant1,
        image: {'@id': 'variant-image', src: 'cloudinary:test/variant-image#{}'},
        images: {
          '@nodes': ['images0'],
          images0: {
            '@id': 'variant-asset',
            '@name': 'images0',
            asset: {field: 'img', img: {src: 'cloudinary:test/variant-asset#{}'}}
          }
        }
      }
    }
    const wrapper = getWrapper({
      ...options,
      provide: {model: ref(subcategoryWithVariantImages), selectedVariant: ref('variant1')}
    })
    const srcs = wrapper.vm.images.map(img => img.src)
    expect(srcs).toEqual([
      'cloudinary:test/subcategory-image#{}',
      'cloudinary:test/asset-0#{}',
      'cloudinary:test/asset-1#{}'
    ])
  })

  it('shows gallery assets even when the subcategory image is missing', () => {
    const {image, ...withoutImage} = mockAttachmentSubcategory
    const wrapper = getWrapper({
      ...options,
      provide: {model: ref(withoutImage), selectedVariant: ref(null)}
    })
    const srcs = wrapper.vm.images.map(img => img.src)
    expect(srcs).toEqual([
      'cloudinary:test/asset-0#{}',
      'cloudinary:test/asset-1#{}'
    ])
  })

  it('renders compatible machines section when compatibleMachines exist', () => {
    const wrapper = getWrapper()
    expect(wrapper.find('[data-test-id="attachment-compatible-machines"]').exists()).toBe(true)
  })

  it('shows first 3 compatible machines by default', () => {
    const wrapper = getWrapper()
    expect(wrapper.findAll('[data-test-id^="compatible-machine-badge-"]').length).toBe(3)
  })

  it('shows "More" link when more than 3 compatible machines', () => {
    const wrapper = getWrapper()
    expect(wrapper.text()).toMatch(/\+1[\s\u00A0]+More/)
  })

  it('shows all compatible machines after clicking More', async () => {
    const wrapper = getWrapper()
    const moreLink = wrapper.find('a.font-weight-bold[href="#"]')
    expect(moreLink.exists()).toBe(true)
    await moreLink.trigger('click')
    await wrapper.vm.$nextTick()
    expect(wrapper.findAll('[data-test-id^="compatible-machine-badge-"]').length).toBe(4)
  })

  it('does not render compatible machines section when no compatibleMachines', () => {
    const modelNoCompat = ref({
      ...mockAttachmentModel,
      compatibleMachines: null
    })
    const wrapper = getWrapper({
      ...options,
      provide: {model: modelNoCompat, selectedVariant: ref('test-attachment')}
    })
    expect(wrapper.find('[data-test-id="attachment-compatible-machines"]').exists()).toBe(false)
  })

  it('renders variant dropdown when more than 2 variant options', () => {
    const wrapper = getWrapper({
      ...options,
      provide: {
        model: ref(mockAttachmentModelWithVariants),
        selectedVariant: ref(null)
      }
    })
    expect(wrapper.find('[data-test-id="switch-dropdown"]').exists()).toBe(true)
  })

  it('renders CTA buttons when cta prop has nodes', () => {
    const ctaProp = {
      '@nodes': ['cta0'],
      cta0: {
        field: 'primary',
        color: 'white',
        link: {
          field: 'external',
          link: {
            externalUrl: 'https://example.com',
            externalLabel: 'Learn More',
            target: '_blank'
          }
        }
      }
    }
    const wrapper = getWrapper({
      ...options,
      propsData: {...defaultProps, cta: ctaProp}
    })
    const ctaButtons = wrapper.findAll('[data-test-id="cta-button"]')
    expect(ctaButtons.length).toBeGreaterThan(0)
  })

  it('renders without title when title prop is not provided', () => {
    const wrapper = getWrapper({
      ...options,
      propsData: {nodeId: 'test', subtitle: '', htag: 'h2'}
    })
    expect(wrapper.find('[data-test-id="model-pricing-title__desktop"]').exists()).toBe(false)
  })

  it('uses default htag when not provided', () => {
    const wrapper = getWrapper({
      ...options,
      propsData: {...defaultProps, htag: undefined}
    })
    expect(wrapper.find('[data-test-id="attachment-pricing"]').exists()).toBe(true)
  })
})
