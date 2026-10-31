import linkUtils from '~/plugins/utils/linkUtils'

describe('linkUtils.getLinkField', () => {
  it('resolves localAnchor to hash-only URL', () => {
    const node = {
      field: 'localAnchor',
      '@nodes': ['link'],
      link: {
        '@id': 'la-1',
        '@name': 'link',
        anchorID: 'models',
        localAnchorLabel: 'See models',
        target: '_self'
      }
    }

    expect(linkUtils.getLinkField(node, 'en')).toEqual({
      id: 'la-1',
      url: '#models',
      label: 'See models',
      target: '_self',
      type: 'localAnchor'
    })
  })

  it('appends optional anchorID to page URLs', () => {
    const node = {
      field: 'page',
      '@nodes': ['link'],
      link: {
        '@id': 'pg-1',
        '@name': 'link',
        pageUrl: '/bobcat/na/en/equipment',
        pageLabel: 'Equipment',
        anchorID: 'models',
        target: '_self'
      }
    }

    const result = linkUtils.getLinkField(node, 'en')
    expect(result.type).toBe('page')
    expect(result.label).toBe('Equipment')
    expect(result.url).toMatch(/#models$/)
    expect(result.url).not.toMatch(/##/)
  })

  it('keeps custom URLs unchanged', () => {
    const node = {
      field: 'custom',
      '@nodes': ['link'],
      link: {
        '@id': 'cu-1',
        '@name': 'link',
        customUrl: 'https://example.com/path',
        customLabel: 'External',
        target: '_blank'
      }
    }

    expect(linkUtils.getLinkField(node, 'en')).toEqual({
      id: 'cu-1',
      url: 'https://example.com/path',
      label: 'External',
      target: '_blank',
      type: 'custom'
    })
  })

  it('allows relative and mailto/tel custom URLs', () => {
    const relative = {
      field: 'custom',
      '@nodes': ['link'],
      link: {customUrl: '/na/en/equipment', customLabel: 'Rel', target: '_self'}
    }
    expect(linkUtils.getLinkField(relative, 'en').url).toBe('/na/en/equipment')

    const mailto = {
      field: 'custom',
      '@nodes': ['link'],
      link: {customUrl: 'mailto:test@example.com', customLabel: 'Mail', target: '_self'}
    }
    expect(linkUtils.getLinkField(mailto, 'en').url).toBe('mailto:test@example.com')
  })

  it('rejects javascript: and data: custom URLs', () => {
    const js = {
      field: 'custom',
      '@nodes': ['link'],
      link: {
        customUrl: 'javascript:alert(document.cookie)',
        customLabel: 'XSS',
        target: '_self'
      }
    }
    expect(linkUtils.getLinkField(js, 'en')).toBeUndefined()

    const data = {
      field: 'custom',
      '@nodes': ['link'],
      link: {
        customUrl: 'data:text/html,<script>alert(1)</script>',
        customLabel: 'Data',
        target: '_self'
      }
    }
    expect(linkUtils.getLinkField(data, 'en')).toBeUndefined()
  })

  it('returns null when localAnchor is missing anchorID', () => {
    const node = {
      field: 'localAnchor',
      '@nodes': ['link'],
      link: {
        localAnchorLabel: 'Missing id'
      }
    }

    expect(linkUtils.getLinkField(node, 'en')).toBeUndefined()
  })
})

describe('linkUtils.getLinks', () => {
  it('resolves multifield localAnchor items', () => {
    const node = {
      '@nodes': ['0'],
      0: {
        '@id': 'item-1',
        '@name': '0',
        field: 'localAnchor',
        link: {
          anchorID: 'apply',
          localAnchorLabel: 'Apply',
          target: '_self'
        }
      }
    }

    expect(linkUtils.getLinks(node, 'en')).toEqual([{
      id: 'item-1',
      url: '#apply',
      label: 'Apply',
      target: '_self',
      type: 'localAnchor'
    }])
  })

  it('falls back to the multifield item name when its optional label is empty', () => {
    const node = {
      '@nodes': ['contact-us'],
      'contact-us': {
        '@id': 'item-2',
        '@name': 'Contact us',
        field: 'custom',
        link: {
          '@name': 'link',
          customUrl: '/contact',
          target: '_self'
        }
      }
    }

    expect(linkUtils.getLinks(node, 'en')[0].label).toBe('Contact us')
  })
})
