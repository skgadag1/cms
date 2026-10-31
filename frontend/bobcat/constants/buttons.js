export const BUTTON_NAMES = {
  NONE: 'none',
  HEXAGONAL: 'hexagonal',
  CLASSIC: 'classic',
  LINEAR: 'linear'
}

export const BUTTON_COLORS = {
  PRIMARY: 'primary',
  SECONDARY: 'secondary'
}

export const BUTTON_STYLES = {
  [BUTTON_NAMES.HEXAGONAL]: {
    base: ['hexagonal', 'font-weight-bold'],
    colors: {
      red: ['btn-primary', 'bg-primary', 'text-white', 'border-0'],
      white: ['btn-primary', 'bg-white', 'text-black', 'border-0'],
      black: ['btn-primary', 'bg-black', 'text-white', 'border-0'],
      yellow: ['btn-orange', 'text-black', 'border-0'],
      grey: ['btn-tertiary', 'text-black', 'border-0'],
      'white-red': ['btn-outline-primary', 'bg-white', 'text-primary'],
      'white-black': ['btn-outline-dark', 'bg-white', 'text-black']
    },
    default: ['btn-primary', 'bg-primary', 'text-white', 'border-0']
  },
  [BUTTON_NAMES.CLASSIC]: {
    base: [],
    colors: {
      red: ['btn-primary', 'bg-primary', 'text-white', 'border-color-red'],
      black: ['bg-black', 'text-white', 'border-color-black'],
      yellow: ['btn-orange', 'text-black', 'border-color-yellow'],
      grey: ['btn-tertiary', 'text-black', 'border-color-grey'],
      white: ['btn-primary', 'bg-white', 'text-black', 'border-color-white'],
      'white-red': ['btn-outline-primary', 'btn']
    },
    default: ['btn-primary', 'bg-primary', 'text-white']
  },
  [BUTTON_NAMES.LINEAR]: {
    base: ['btn-linear', 'btn-lg', 'bg-transparent', 'font-weight-bold', 'border-0'],
    colors: {
      red: ['text-primary'],
      black: ['text-black'],
      white: ['text-white']
    },
    default: ['text-primary']
  }
}
