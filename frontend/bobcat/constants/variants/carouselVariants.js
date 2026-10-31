const VARIANTS_CLASSES = {
  black: {
    background: 'bg-black',
    divider: 'border-color-white',
    headline: 'text-white',
    tiles: 'no-shadow bg-white', // Default value, can be overridden
    chips: '',
    dots: 'border-color-white',
    arrows: 'border-color-white',
    caption: '', // Default value, can be overridden
    amounts: 'bg-dark text-white'
  },
  dark: {
    background: 'bg-gray-darker',
    divider: 'border-color-white',
    headline: 'text-white',
    tiles: 'bg-gray-lighter', // Default value, can be overridden
    chips: '',
    dots: 'border-color-white',
    arrows: 'border-color-white',
    caption: 'secondary', // Default value, can be overridden
    amounts: 'bg-dark text-white'
  },
  light: {
    background: 'bg-gray-lighter',
    divider: 'border-color-gray-light',
    headline: '',
    text: 'text-white', // Default value, can be overridden
    tiles: 'bg-gray-darker', // Default value, can be overridden
    chips: 'light',
    dots: 'border-primary',
    arrows: '',
    amounts: 'bg-white'
  },
  white: {
    background: 'bg-white',
    divider: 'border-color-gray-dark-medium',
    headline: '',
    text: 'text-white', // Default value, can be overridden
    tiles: 'bg-gray-darker', // Default value, can be overridden
    chips: '', // Default value, can be overridden
    dots: 'border-primary',
    arrows: '',
    amounts: ''
  }
}

const WHITE_VARIANT = 'white'
const LIGHT_VARIANT = 'light'
const DARK_VARIANT = 'dark'
const EVOLUTION_WHITE_VARIANT = 'evolutionWhite'
const EVOLUTION_LIGHT_VARIANT = 'evolutionLight'
const EVOLUTION_DARK_VARIANT = 'evolutionDark'
const BLACK_VARIANT = 'black'
const BLACK_GRADIENT_VARIANT = 'gradientBlack'

export {WHITE_VARIANT, BLACK_VARIANT, LIGHT_VARIANT, EVOLUTION_WHITE_VARIANT, EVOLUTION_LIGHT_VARIANT, EVOLUTION_DARK_VARIANT, DARK_VARIANT, BLACK_GRADIENT_VARIANT, VARIANTS_CLASSES}
