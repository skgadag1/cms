const COLORS = {
  WHITE: 'white',
  DARK: 'dark',
  BLACK: 'black',
  EVOLUTION_WHITE: 'evolutionWhite',
  EVOLUTION_LIGHT: 'evolutionLight',
  EVOLUTION_DARK: 'evolutionDark',
  EVOLUTION_BLACK: 'evolutionBlack'
}

const VARIANTS_CLASSES = {
  [COLORS.WHITE]: {
    accordionBackground: 'bg-white',
    cardBackground: 'bg-white',
    cardText: 'text-black',
    counterText: 'text-gray-light',
    expandAllText: 'text-primary',
    chipsColor: 'dark',
    text: 'text-black'
  },
  [COLORS.DARK]: {
    accordionBackground: 'bg-gray-darker',
    cardBackground: 'bg-white',
    cardText: 'text-black',
    counterText: 'text-gray-light',
    expandAllText: 'text-primary',
    chipsColor: 'dark',
    text: 'text-white'
  },
  [COLORS.BLACK]: {
    accordionBackground: 'bg-black',
    cardBackground: 'bg-white',
    cardText: 'text-black',
    counterText: 'text-gray-light',
    expandAllText: 'text-primary',
    chipsColor: 'dark',
    text: 'text-white'
  },
  [COLORS.EVOLUTION_WHITE]: {
    background: 'bg-white',
    accordionBackground: 'bg-light',
    cardBackground: 'bg-light',
    cardText: '',
    counterText: '',
    expandAllText: 'text-primary',
    chipsColor: 'dark',
    text: ''
  },
  [COLORS.EVOLUTION_LIGHT]: {
    background: 'bg-light',
    accordionBackground: 'bg-white',
    cardBackground: 'bg-white',
    cardText: '',
    counterText: '',
    expandAllText: 'text-primary',
    chipsColor: 'dark',
    text: ''
  },
  [COLORS.EVOLUTION_DARK]: {
    background: 'bg-gray-darker',
    accordionBackground: 'bg-black',
    cardBackground: 'bg-gray-darker',
    cardText: 'text-white',
    counterText: 'text-white',
    expandAllText: 'text-white',
    chipsColor: 'white',
    text: 'text-white'
  },
  [COLORS.EVOLUTION_BLACK]: {
    background: 'bg-black',
    accordionBackground: 'bg-dark',
    cardBackground: 'bg-dark',
    cardText: 'text-white',
    counterText: 'text-white',
    expandAllText: 'text-white',
    chipsColor: 'white',
    text: 'text-white'
  }
}

export {COLORS, VARIANTS_CLASSES}
