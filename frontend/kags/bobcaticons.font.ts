import type {FontConfig} from './build/vite-plugin-icon-font'

const fontConfig: FontConfig = {
  'inputDir': './public/assets/img/icons',
  'fontName': 'bobcaticons',
  'classPrefix': 'bobcaticon-',
  'types': ['woff2', 'ttf', 'svg']
}

export default fontConfig
