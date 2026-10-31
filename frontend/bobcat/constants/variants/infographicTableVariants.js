const LIGHT_GRAY_VARIANT = 'LIGHT_GRAY'
const WHITE_VARIANT = 'WHITE'
const BLACK_GRADIENT_VARIANT = 'BLACK_GRADIENT'
const DARK_GRAY_VARIANT = 'DARK_GRAY'
const DARK_AND_WHITE_VARIANT = 'DARK_AND_WHITE'

const VARIANTS_CLASSES = {
  [LIGHT_GRAY_VARIANT]: {
    background: 'bg-gray-lighter',
    text: '',
    card: 'bg-white'
  },
  [WHITE_VARIANT]: {
    background: 'bg-white',
    text: '',
    card: 'bg-gray-lighter text-black'
  },
  [BLACK_GRADIENT_VARIANT]: {
    background: 'bg-black evolution-gradient',
    text: 'text-white',
    card: 'text-white bg-black'
  },
  [DARK_GRAY_VARIANT]: {
    background: 'bg-gray-darker',
    text: 'text-white',
    card: 'bg-black'
  },
  [DARK_AND_WHITE_VARIANT]: {
    background: 'bg-gray-dark-medium',
    text: '',
    card: 'bg-white'
  }
}

const CARD_OPTIONS = {
  none: 'none',
  black: 'black',
  'gray-lighter': 'gray-lighter',
  white: 'white'
}

const CUSTOM_CARD_VARIANTS = {
  [CARD_OPTIONS.black]: 'bg-black text-white',
  [CARD_OPTIONS['gray-lighter']]: 'bg-gray-lighter text-black',
  [CARD_OPTIONS.white]: 'bg-white text-black'
}

export {
  LIGHT_GRAY_VARIANT,
  WHITE_VARIANT,
  BLACK_GRADIENT_VARIANT,
  DARK_GRAY_VARIANT,
  DARK_AND_WHITE_VARIANT,
  VARIANTS_CLASSES,
  CUSTOM_CARD_VARIANTS,
  CARD_OPTIONS
}
