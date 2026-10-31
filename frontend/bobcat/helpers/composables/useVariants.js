import {DARK_VARIANT, BLACK_VARIANT, BLACK_GRADIENT_VARIANT, EVOLUTION_WHITE_VARIANT, EVOLUTION_LIGHT_VARIANT, EVOLUTION_DARK_VARIANT, LIGHT_VARIANT, VARIANTS_CLASSES, WHITE_VARIANT} from '~/constants/variants/carouselVariants'

export default function useVariants() {
  const getColorVariantStylesForListing = (variant = {field: WHITE_VARIANT, tiles: WHITE_VARIANT}) => {
    const {field, tiles} = variant
    const baseVariant = {...VARIANTS_CLASSES[field]}
    if (!baseVariant) {
      return null
    }

    const tilesMapping = {
      [WHITE_VARIANT]: '',
      [LIGHT_VARIANT]: 'bg-gray-lighter',
      [DARK_VARIANT]: 'bg-gray-darker',
      [EVOLUTION_WHITE_VARIANT]: 'evolution-shadow',
      [EVOLUTION_LIGHT_VARIANT]: 'evolution-shadow bg-gray-lighter',
      [EVOLUTION_DARK_VARIANT]: 'evolution-shadow bg-gray-darker',
      [BLACK_VARIANT]: 'evolution-shadow bg-black',
      [BLACK_GRADIENT_VARIANT]: 'evolution-shadow evolution-gradient bg-black'
    }
    baseVariant.tiles = tilesMapping[tiles] ?? baseVariant.tiles
    baseVariant.caption = tiles

    const textColorMapping = {
      [WHITE_VARIANT]: '',
      [LIGHT_VARIANT]: '',
      [DARK_VARIANT]: 'text-white',
      [EVOLUTION_WHITE_VARIANT]: '',
      [EVOLUTION_LIGHT_VARIANT]: '',
      [EVOLUTION_DARK_VARIANT]: 'text-white',
      [BLACK_VARIANT]: 'text-white',
      [BLACK_GRADIENT_VARIANT]: 'text-white'
    }
    baseVariant.text = textColorMapping[tiles] ?? baseVariant.text

    const chipsMapping = {
      [WHITE_VARIANT]: 'black',
      [LIGHT_VARIANT]: 'black',
      [DARK_VARIANT]: 'white',
      [EVOLUTION_WHITE_VARIANT]: 'black',
      [EVOLUTION_LIGHT_VARIANT]: 'black',
      [EVOLUTION_DARK_VARIANT]: 'white',
      [BLACK_VARIANT]: 'white',
      [BLACK_GRADIENT_VARIANT]: 'white'
    }
    baseVariant.chips = chipsMapping[tiles] ?? baseVariant.chips

    // Special adjustments (dependencies on tiles property)
    switch (field) {
    case DARK_VARIANT:
      if (tiles === WHITE_VARIANT || tiles === LIGHT_VARIANT) {
        baseVariant.caption = 'black'
      }
      break
    case LIGHT_VARIANT:
      if (tiles === WHITE_VARIANT) {
        baseVariant.text = ''
        baseVariant.chips = ''
        baseVariant.amounts = 'bg-gray-lighter text-gray-dark-medium'
      } else if (tiles === DARK_VARIANT) {
        baseVariant.amounts = 'bg-gray-lighter text-dark'
        baseVariant.caption = 'black'
      }
      break
    case WHITE_VARIANT:
      if (tiles === LIGHT_VARIANT) {
        baseVariant.text = ''
        baseVariant.chips = ''
        baseVariant.amounts = 'bg-white'
        baseVariant.caption = 'dark'
      } else if (tiles === DARK_VARIANT) {
        baseVariant.caption = 'black'
        baseVariant.amounts = 'bg-white text-dark'
        baseVariant.chips = LIGHT_VARIANT
      } else if (tiles === WHITE_VARIANT) {
        baseVariant.text = ''
      }
      break
    }

    return baseVariant
  }

  return {
    getColorVariantStylesForListing
  }
}
