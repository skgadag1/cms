import fs from 'fs'
import path from 'path'
import template from 'lodash.template'
import {loadEnv, type Plugin} from 'vite'
import pagesConfig from '../pages.config'

const ROOT = path.resolve(__dirname, '..')
const TMP_DIR_NAME = '.kags-vite-mpa-pages'

// A pages.config.ts entry: either just the entry path, or the full vue-cli-style page object.
export interface PageDefinition {
  entry: string | string[]
  template?: string
  filename?: string
  title?: string
  devAliases?: string[]
}

export type PagesConfig = Record<string, string | PageDefinition>

interface Page {
  name: string
  entries: string[]
  templatePath: string
  filename: string
  title: string
  devAliases: string[]
}

type Env = Record<string, string>

function normalizePage(name: string, def: string | PageDefinition): Page {
  if (typeof def === 'string') def = {entry: def}
  const entries = Array.isArray(def.entry) ? def.entry : [def.entry]
  return {
    name,
    entries,
    templatePath: def.template || 'public/index.html',
    filename: def.filename || `${name}.html`,
    title: def.title || name,
    devAliases: def.devAliases || []
  }
}

function getPages(): Page[] {
  return Object.keys(pagesConfig).map(name => normalizePage(name, pagesConfig[name]))
}

const BASE_URL = '/'

// Request query params made available to page templates as `query.<name>`, like Magnolia's
// ctx.getParameter(). Values land inside JSON strings in single-quoted data-config attributes.
function toTemplateQuery(search: string): Record<string, string> {
  const query: Record<string, string> = {}
  new URLSearchParams(search).forEach((value, key) => {
    query[key] = JSON.stringify(value).slice(1, -1)
      .replace(/&/g, '&amp;')
      .replace(/'/g, '&#39;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
  })
  return query
}

function renderPage(page: Page, env: Env, scriptSrcs: string[] = page.entries.map(entry => '/' + entry), query: Record<string, string> = {}): string {
  const rawTemplate = fs.readFileSync(path.resolve(ROOT, page.templatePath), 'utf-8')
  const scripts = scriptSrcs.map(src => `<script type="module" src="${src}"></script>`).join('\n    ')
  const withEntries = rawTemplate.replace('</body>', `    ${scripts}\n  </body>`)

  const compile = template(withEntries, {
    interpolate: /<%=([\s\S]+?)%>/g,
    evaluate: /<%([\s\S]+?)%>/g
  })

  return compile({
    ...env,
    BASE_URL,
    process: {env},
    query,
    htmlWebpackPlugin: {options: {title: page.title}}
  })
}

function toTempFileName(pageName: string): string {
  return `${pageName.replace(/[^a-zA-Z0-9_-]/g, '_')}.html`
}

export default function pagesMpaPlugin(): Plugin {
  let resolvedEnv: Env = {}
  let outDir = 'dist'
  let tmpDirRel = ''

  return {
    name: 'kags-pages-mpa',
    config(userConfig, {command, mode}) {
      resolvedEnv = loadEnv(mode, ROOT, '')

      if (command !== 'build') return undefined

      // Generated inputs live under the build's own outDir (dist/ or dist-preview/). Rollup reads
      // them before Vite empties outDir for the output, and closeBundle tidies up the rest.
      tmpDirRel = path.posix.join(userConfig.build?.outDir || 'dist', TMP_DIR_NAME)
      const tmpDir = path.resolve(ROOT, tmpDirRel)
      fs.rmSync(tmpDir, {recursive: true, force: true})
      fs.mkdirSync(tmpDir, {recursive: true})

      // Magnolia loads each page's bundle from a fixed assets/js/<page name>.js, so every page gets
      // its own JS entry module (named after the page) that the page HTML points at. Vite drops an
      // HTML entry's chunk when it only contains imports, which is the case for any page whose
      // code is fully shared with another page (e.g. header vs. index); a JS entry is always kept.
      const input: Record<string, string> = {}
      for (const page of getPages()) {
        const entryFileName = toTempFileName(page.name).replace(/\.html$/, '.entry.js')
        const entryFile = path.join(tmpDir, entryFileName)
        const imports = page.entries.map(entry => `import ${JSON.stringify(path.resolve(ROOT, entry).replace(/\\/g, '/'))}`)
        fs.writeFileSync(entryFile, imports.join('\n') + '\n')
        input[page.name] = entryFile

        const html = renderPage(page, resolvedEnv, [`/${tmpDirRel}/${entryFileName}`])
        const tmpFile = path.join(tmpDir, toTempFileName(page.name))
        fs.writeFileSync(tmpFile, html)
        input[`${page.name}__html`] = tmpFile
      }

      return {build: {rollupOptions: {input}}}
    },
    configResolved(resolvedConfig) {
      outDir = resolvedConfig.build.outDir
    },
    closeBundle() {
      const outDirAbs = path.isAbsolute(outDir) ? outDir : path.resolve(ROOT, outDir)
      // HTML inputs keep their path relative to the root, so they come out at <outDir>/<tmpDirRel>.
      const sourceDir = path.join(outDirAbs, tmpDirRel)
      if (!tmpDirRel || !fs.existsSync(sourceDir)) return

      for (const page of getPages()) {
        const from = path.join(sourceDir, toTempFileName(page.name))
        if (!fs.existsSync(from)) continue

        const to = path.join(outDirAbs, page.filename)
        fs.mkdirSync(path.dirname(to), {recursive: true})
        fs.renameSync(from, to)
      }

      fs.rmSync(path.join(outDirAbs, tmpDirRel.split('/')[0]), {recursive: true, force: true})
    },
    configureServer(server) {
      // The page map below is built once per server start. pages.config.ts is imported by this
      // plugin, so Vite treats it as a config dependency and restarts the server when it changes.
      const pages = getPages()
      const pageByUrlPath = new Map<string, Page>()
      for (const page of pages) {
        pageByUrlPath.set('/' + page.filename.replace(/^\/+/, ''), page)
      }

      for (const page of pages) {
        const nameKey = '/' + page.name
        if (!pageByUrlPath.has(nameKey)) pageByUrlPath.set(nameKey, page)
      }

      // Pages with an extensionless filename (machine/locationhistory, machine/faultcodes, ...)
      // were also reachable at "<filename>.html" under vue-cli's dev server. Without this alias
      // the ".html" URL looks like a static file request, skips the index fallback below and 404s.
      for (const page of pages) {
        if (path.extname(page.filename)) continue
        const htmlKey = '/' + page.filename.replace(/^\/+/, '') + '.html'
        if (!pageByUrlPath.has(htmlKey)) pageByUrlPath.set(htmlKey, page)
      }

      // Legacy/renamed URLs declared per page as `devAliases` in pages.config.ts serve that page
      // directly (URL bar stays as requested -- no redirect) rather than 404ing or falling back
      // to index below. Extensionless aliases also get their ".html" form, like the filenames above.
      for (const page of pages) {
        for (const alias of page.devAliases) {
          const aliasKey = '/' + alias.replace(/^\/+/, '')
          pageByUrlPath.set(aliasKey, page)
          if (!path.extname(aliasKey)) pageByUrlPath.set(aliasKey + '.html', page)
        }
      }

      server.middlewares.use(async (req, res, next) => {
        if (req.method !== 'GET') return next()

        const rawUrlPath = (req.url || '/').split('?')[0].split('#')[0]

        const urlPath = rawUrlPath === '/' ? '/index.html' : rawUrlPath
        let page = pageByUrlPath.get(urlPath)

        if (!page) {
          const lastSegment = urlPath.split('/').pop() || ''
          const looksLikeFile = lastSegment.includes('.')
          const acceptsHtml = (req.headers.accept || '').includes('text/html')
          if (looksLikeFile || !acceptsHtml) return next()

          page = pageByUrlPath.get('/index.html')
          if (!page) return next()
        }

        try {
          const search = (req.url || '').split('#')[0].split('?')[1] || ''
          let html = renderPage(page, resolvedEnv, undefined, toTemplateQuery(search))
          html = await server.transformIndexHtml(urlPath, html, req.originalUrl)
          res.statusCode = 200
          res.setHeader('Content-Type', 'text/html')
          res.end(html)
        } catch (e) {
          next(e)
        }
      })
    }
  }
}
