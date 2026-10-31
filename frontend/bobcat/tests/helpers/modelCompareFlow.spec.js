import {
  addCompareSlot,
  buildCompareSlots,
  removeCompareSlot
} from '~/helpers/evolutionCompareSlots'
import {
  buildModelCompareHref,
  mapModelSelectorCard
} from '~/helpers/modelSelectorCard'

const models = [
  {id: 'M0201', '@id': 'uuid-s66', '@name': 'S66', title: 'S66'},
  {id: 'M0202', '@id': 'uuid-s76', '@name': 'S76', title: 'S76'},
  {id: 'M0203', '@id': 'uuid-s650', '@name': 'S650', title: 'S650'}
]

describe('model selector card mapping', () => {
  it('maps catalog content, gallery image, price, and filtering key specs', () => {
    const card = mapModelSelectorCard({
      '@id': 'uuid-s66',
      name: 'S66',
      shortDescription: 'A compact loader.',
      suggestedPrice: '$64,941',
      image: {src: 'fallback-image'},
      images: {
        '@nodes': ['video', 'firstImage', 'secondImage'],
        video: {asset: {field: 'video', video: {src: 'video'}}},
        firstImage: {asset: {field: 'img', img: {src: 'first-gallery-image'}}},
        secondImage: {asset: {field: 'img', img: {src: 'second-gallery-image'}}}
      },
      keyProperties: [{name: 'Horsepower', value: '74', unit: 'hp'}]
    })

    expect(card).toMatchObject({
      title: 'S66',
      description: 'A compact loader.',
      suggestedPrice: '$64,941',
      image: {src: 'first-gallery-image'},
      properties: [{name: 'Horsepower', value: '74&nbsp;hp'}]
    })
  })

  it('deep links with the catalog model id rather than the JCR uuid', () => {
    expect(buildModelCompareHref('/compare', models[0])).toBe('/compare?compare=M0201')
  })

  it('falls back to the JCR uuid when a result has no catalog id', () => {
    expect(buildModelCompareHref('/compare', {'@id': 'uuid-only'})).toBe('/compare?compare=uuid-only')
  })

  it('builds a compare deep link without discarding query strings or hashes', () => {
    expect(buildModelCompareHref('/compare?campaign=test#models', models[0]))
      .toBe('/compare?campaign=test&compare=M0201#models')
  })

  it('returns no link when the page is unconfigured or the model has no id', () => {
    expect(buildModelCompareHref('', models[0])).toBeNull()
    expect(buildModelCompareHref('/compare', {title: 'S66'})).toBeNull()
  })
})

describe('dynamic evolution compare slots', () => {
  it('uses a URL model by itself and leaves the second slot empty', () => {
    expect(buildCompareSlots(models, ['M0201'], ['M0202'])).toEqual([models[0], null])
  })

  it('resolves a requested model by catalog id, JCR uuid, or node name', () => {
    expect(buildCompareSlots(models, ['M0201'])[0]).toBe(models[0])
    expect(buildCompareSlots(models, ['uuid-s76'])[0]).toBe(models[1])
    expect(buildCompareSlots(models, ['s650'])[0]).toBe(models[2])
  })

  it('uses configured defaults only when the URL has no model selection', () => {
    expect(buildCompareSlots(models, [], ['uuid-s66', 'uuid-s76'])).toEqual([models[0], models[1]])
  })

  it('supports adding a third empty slot', () => {
    expect(addCompareSlot([models[0], models[1]])).toEqual([models[0], models[1], null])
  })

  it('compacts to two slots after removal and preserves one selected model', () => {
    expect(removeCompareSlot([models[0], models[1], models[2]], 1)).toEqual([models[0], models[2]])
    expect(removeCompareSlot([models[0], models[1]], 0)).toEqual([models[1], null])
  })

  it('deduplicates requested models and caps the comparison at three', () => {
    expect(buildCompareSlots(models, ['M0201', 'M0201', 'M0202', 'M0203']))
      .toEqual(models)
  })
})
