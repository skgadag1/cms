export const BLACK_GRADIENT_VARIANT = 'BLACK_GRADIENT'

export const BLACK_VARIANT = 'BLACK'

export const DARK_GRAY_GRADIENT_VARIANT = 'DARK_GRAY_GRADIENT'

export const DARK_GRAY_VARIANT = 'DARK_GRAY'

export const LIGHT_GRAY_VARIANT = 'LIGHT_GRAY'

export const WHITE_VARIANT = 'WHITE'

export const VARIANT_PROPERTIES = {
  [BLACK_GRADIENT_VARIANT]: {
    displayGradient: true,
    backgroundClass: 'bg-black',
    textClass: 'text-white',
    dropdownBackgroundClass: 'bg-light',
    chipsVariant: 'white',
    dividerClass: 'border-dark-divider'

  },
  [DARK_GRAY_GRADIENT_VARIANT]: {
    displayGradient: true,
    backgroundClass: 'bg-gray-darker',
    textClass: 'text-white',
    dropdownBackgroundClass: 'bg-light',
    chipsVariant: 'white',
    dividerClass: 'border-dark-divider'
  },
  [LIGHT_GRAY_VARIANT]: {
    displayGradient: false,
    backgroundClass: 'bg-gray-lighter',
    textClass: 'text-black',
    dropdownBackgroundClass: 'bg-white',
    chipsVariant: 'dark',
    dividerClass: 'border-tertiary'
  },
  [BLACK_VARIANT]: {
    displayGradient: false,
    backgroundClass: 'bg-black',
    borderClass: 'black',
    textClass: 'text-white',
    dropdownBackgroundClass: 'bg-light',
    chipsVariant: 'white',
    dividerClass: 'border-dark-divider'
  },
  [DARK_GRAY_VARIANT]: {
    displayGradient: false,
    backgroundClass: 'bg-gray-darker',
    borderClass: 'gray-darker',
    textClass: 'text-white',
    dropdownBackgroundClass: 'bg-light',
    chipsVariant: 'white',
    dividerClass: 'border-dark-divider'
  },
  [WHITE_VARIANT]: {
    displayGradient: false,
    backgroundClass: 'bg-white',
    textClass: 'text-black',
    dropdownBackgroundClass: 'bg-light',
    chipsVariant: 'dark',
    dividerClass: 'border-tertiary'
  }
}
