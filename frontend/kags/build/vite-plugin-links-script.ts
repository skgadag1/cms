// Every page template hardcodes `<script src="<%= VUE_APP_ASSETS_STATIC_JS_PREFIX %>links.js">`
// as a plain, non-module <script> tag (not a Vite-processed `type="module"` entry), so its output
// path can't be left to Vite's normal (content-hashed) bundling -- it must land at exactly
// assets/js/links.js, in plain-script (IIFE) form, both in dev and in the production build.
import fs from 'fs'
import path from 'path'
import {build, type Plugin, type Rollup} from 'vite'

const ROOT = path.resolve(__dirname, '..')
const ENTRY = path.resolve(ROOT, 'src/assets/js/links.js')

// A separate Vite library build (IIFE): the main build's rollupOptions output is ES modules for
// every entry, so it can't also emit this one file as a plain script.
async function bundle(minify: boolean): Promise<string> {
  const result = await build({
    configFile: false,
    root: ROOT,
    logLevel: 'silent',
    build: {
      write: false,
      minify,
      target: 'es2018',
      lib: {entry: ENTRY, name: 'kagsLinks', formats: ['iife'], fileName: () => 'links.js'}
    }
  }) as Rollup.RollupOutput | Rollup.RollupOutput[]
  const output = Array.isArray(result) ? result[0] : result
  return output.output[0].code
}

export default function linksScriptPlugin(): Plugin {
  let command: 'build' | 'serve' = 'serve'
  let outDir = 'dist'

  return {
    name: 'kags-links-script',
    configResolved(resolvedConfig) {
      command = resolvedConfig.command
      outDir = resolvedConfig.build.outDir
    },
    configureServer(server) {
      server.middlewares.use(async (req, res, next) => {
        const urlPath = (req.url || '').split('?')[0]
        // VUE_APP_ASSETS_STATIC_JS_PREFIX is empty in some env files (.env / .env.devapi), so the
        // template's <script src="<%= ...PREFIX %>links.js"> can resolve to a bare "/links.js"
        // instead of "/assets/js/links.js" -- match on the basename regardless of prefix.
        if (!/\/links\.js$/.test(urlPath)) return next()

        try {
          const js = await bundle(false)
          res.setHeader('Content-Type', 'application/javascript')
          res.end(js)
        } catch (e) {
          next(e)
        }
      })
    },
    async closeBundle() {
      if (command !== 'build') return

      const outDirAbs = path.isAbsolute(outDir) ? outDir : path.resolve(ROOT, outDir)
      const js = await bundle(true)

      // Emit at the conventional path (assets/js/links.js) *and* at the dist root, since
      // VUE_APP_ASSETS_STATIC_JS_PREFIX is empty in some env files and would otherwise reference
      // a root-level /links.js that doesn't exist.
      const prefixedDest = path.join(outDirAbs, 'assets', 'js', 'links.js')
      fs.mkdirSync(path.dirname(prefixedDest), {recursive: true})
      fs.writeFileSync(prefixedDest, js)
      fs.writeFileSync(path.join(outDirAbs, 'links.js'), js)
    }
  }
}
