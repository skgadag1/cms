const LEFT_ALIGNMENT = 'left'
const CENTER_ALIGNMENT = 'center'

const VARIANTS_CLASSES = {
  white: {
    background: 'bg-white',
    text: ''
  },
  lightGray: {
    background: 'bg-light',
    text: ''
  },
  dark: {
    background: 'bg-gray-darker',
    text: 'text-white'
  },
  grayDarkMedium: {
    background: 'bg-gray-dark-medium',
    text: ''
  },
  black: {
    background: 'bg-black',
    text: 'text-white'
  },
  hyperspace: {
    background: 'bg-black evolution-gradient',
    text: 'text-white'
  },
  red: {
    background: 'bg-primary',
    text: 'text-white'
  },
  // Legacy CTA colors (old dialog: red, black, gray)
  gray: {
    background: 'bg-light',
    text: 'text-black'
  }
}

const ALIGNMENT_CLASSES = {
  [LEFT_ALIGNMENT]: '',
  [CENTER_ALIGNMENT]: 'justify-content-center'
}

export {
  LEFT_ALIGNMENT,
  CENTER_ALIGNMENT,
  VARIANTS_CLASSES,
  ALIGNMENT_CLASSES
}
