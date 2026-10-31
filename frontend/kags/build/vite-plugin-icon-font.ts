import fs from 'fs'
import path from 'path'
import {SVGIcons2SVGFontStream} from 'svgicons2svgfont'
import svg2ttf from 'svg2ttf'
import ttf2woff2 from 'ttf2woff2'
import type {Plugin} from 'vite'
import fontConfig from '../bobcaticons.font'

const ROOT = path.resolve(__dirname, '..')
const OUTPUT_CSS_PATH = path.resolve(ROOT, 'src/assets/bobcaticons.generated.css')
const START_CODEPOINT = 0xF101

// EOT (IE-only) and WOFF are not generated: every browser Vue 3 supports handles WOFF2, with TTF as fallback.
export type FontType = 'svg' | 'ttf' | 'woff2'

// Shape of bobcaticons.font.ts.
export interface FontConfig {
  inputDir: string
  fontName: string
  classPrefix?: string
  types?: FontType[]
}

interface Icon {
  name: string
  codepoint: number
  file: string
}

const FONT_FORMATS: Record<FontType, {mime: string, format: string, suffix?: string}> = {
  woff2: {mime: 'font/woff2', format: 'woff2'},
  ttf: {mime: 'application/x-font-ttf', format: 'truetype'},
  svg: {mime: 'image/svg+xml', format: 'svg'}
}

function loadIcons(inputDir: string): Icon[] {
  const dir = path.resolve(ROOT, inputDir)
  return fs.readdirSync(dir)
    .filter(file => file.endsWith('.svg'))
    .sort()
    .map((file, i) => ({name: path.basename(file, '.svg'), codepoint: START_CODEPOINT + i, file: path.join(dir, file)}))
}

function buildSvgFont(fontName: string, icons: Icon[]): Promise<string> {
  return new Promise((resolve, reject) => {
    const chunks: Buffer[] = []
    const stream = new SVGIcons2SVGFontStream({fontName, fontHeight: 1000, normalize: true, preserveAspectRatio: true})
      .on('data', chunk => chunks.push(Buffer.from(chunk)))
      .on('end', () => resolve(Buffer.concat(chunks).toString()))
      .on('error', reject)

    for (const icon of icons) {
      const glyph = Object.assign(fs.createReadStream(icon.file), {
        metadata: {name: icon.name, unicode: [String.fromCodePoint(icon.codepoint), icon.name]}
      })
      stream.write(glyph)
    }
    stream.end()
  })
}

async function buildFonts(fontName: string, icons: Icon[], types: FontType[]): Promise<Record<FontType, Uint8Array>> {
  const svg = await buildSvgFont(fontName, icons)
  const ttf = new Uint8Array(svg2ttf(svg, {}).buffer)
  return {
    svg: Buffer.from(svg),
    ttf,
    woff2: types.includes('woff2') ? ttf2woff2(Buffer.from(ttf)) : new Uint8Array()
  }
}

function renderCss(fontName: string, classPrefix: string, types: FontType[], fonts: Record<FontType, Uint8Array>, icons: Icon[]): string {
  const src = types.map(type => {
    const {mime, format, suffix = ''} = FONT_FORMATS[type]
    const hash = type === 'svg' ? `#${fontName}` : ''
    return `url("data:${mime};charset=utf-8;base64,${Buffer.from(fonts[type]).toString('base64')}${suffix}${hash}") format("${format}")`
  }).join(',\n')

  const rules = icons
    .map(({name, codepoint}) => `.${classPrefix}${name}:before {\n\tcontent: "\\${codepoint.toString(16)}";\n}`)
    .join('\n')

  return `@font-face {
\tfont-family: "${fontName}";
\tfont-display: block;
\tsrc: ${src};
}

.icon {
\tline-height: 1;
}

.icon:before {
\tfont-family: ${fontName} !important;
\tfont-style: normal;
\tfont-weight: normal !important;
\tvertical-align: top;
}

${rules}
`
}

async function generateIconFont(): Promise<void> {
  const config = fontConfig
  const types = config.types || ['woff2', 'ttf', 'svg']
  const icons = loadIcons(config.inputDir)
  const fonts = await buildFonts(config.fontName, icons, types)

  fs.mkdirSync(path.dirname(OUTPUT_CSS_PATH), {recursive: true})
  fs.writeFileSync(OUTPUT_CSS_PATH, renderCss(config.fontName, config.classPrefix || 'icon-', types, fonts, icons))
}

export default function iconFontPlugin(): Plugin {
  let generated = false

  const ensureGenerated = async (): Promise<void> => {
    if (generated) return
    await generateIconFont()
    generated = true
  }

  return {
    name: 'kags-icon-font',
    async buildStart() {
      await ensureGenerated()
    },
    async configureServer() {
      await ensureGenerated()
    }
  }
}
