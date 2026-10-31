const LEFT_ALIGNMENT = 'left'
const CENTER_ALIGNMENT = 'center'
const FULL_WIDTH_ALIGNMENT = 'fullWidth'

const WHITE_VARIANT = 'white'
const LIGHT_VARIANT = 'light'
const DARK_VARIANT = 'dark'

const VARIANTS_CLASSES = {
  dark: {
    background: 'bg-gray-darker',
    headline: 'text-white',
    buttonBg: 'bg-white border-0 dark-btn',
    arrows: 'text-white'
  },
  light: {
    background: 'bg-gray-lighter',
    headline: '',
    buttonBg: 'light-btn',
    arrows: 'text-primary'
  },
  darkMedium: {
    background: 'bg-gray-dark-medium',
    headline: '',
    buttonBg: '',
    arrows: 'text-primary'
  },
  white: {
    background: 'bg-white',
    headline: '',
    buttonBg: '',
    arrows: 'text-primary'
  },
  black: {
    background: 'bg-black',
    headline: 'text-white',
    buttonBg: '',
    arrows: 'text-white'
  },
  gradientBlack: {
    background: 'bg-black evolution-gradient',
    headline: 'text-white',
    buttonBg: '',
    arrows: 'text-white'
  }
}

const ALIGNMENT_CLASSES = {
  [LEFT_ALIGNMENT]: 'constrained',
  [CENTER_ALIGNMENT]: 'constrained justify-content-center',
  [FULL_WIDTH_ALIGNMENT]: ''
}

export {WHITE_VARIANT, LIGHT_VARIANT, DARK_VARIANT, VARIANTS_CLASSES, LEFT_ALIGNMENT, CENTER_ALIGNMENT, FULL_WIDTH_ALIGNMENT, ALIGNMENT_CLASSES}
