# bobcat

## Build Setup

```bash
# install dependencies
$ yarn install

# serve with hot reload at localhost:3000 (replace :${env} accordingly)
$ yarn dev:env

# build for production and launch server (replace :${env} accordingly)
$ yarn build:env
$ yarn start:env
```

## Local serve (with hotreload)
To run `yarn dev` you have to have local magnolia running on port `:8080`.

To run local instance with different env use proper command, i.e.
```bash
# dev magnolia author
$ yarn dev:dev
```
or
```bash
# dev magnolia public
$ yarn dev:dev-public
```

## Local build
Running local build require delivering same `:env` command and will build for mode `production`.

i.e.:
```bash
# build using dev magnolia
yarn build:dev
```

### Temporary solution
Because `pagedetails` endpoint is using dynamodb table, we cannot use it on local (yet). There is temporary workaround which allows us to open page locally using
native magnolia endpoints by running it with `--native` flag:
```bash
# run local with native endpoint
yarn dev --native
```

The same option works for `:${env}`, i.e. `yarn dev:qa --native`

**ATTENTION:** The drawback is there is `no breadcrumbs` and `no inheritance`!

## Running SSR Lambda locally

### Additional dependencies
1. AWS SAM
2. Docker

### Steps (replace :${env} accordingly)
1. Remove previously built files
```pwsh
bobcat> rm -r aws/.nuxt
```

2. Build for specific env
```pwsh
bobcat> yarn build:{env}
```

3. Copy built files and environment
```pwsh
bobcat> yarn prepare:aws
```

4. Change directory to aws folder
```pwsh
bobcat> cd aws
```

5. Run lambda
```pwsh
bobcat/aws> yarn run:lambda:ssr
```

### Events
CloudFront event should be added in `aws/local/events/SsrFunction.json`

## Environment Variables
Environment variables are in `.env-cmdrc` file and are used during build and serve.

However if you want to use build for different env than `local` and run it still locally you have to remove `CLOUDFRONT_STATIC` property from `.env-cmdrc` file for this env (otherwise scripts will be served from external url).
 

## PurgeCSS
Configured in `purgecss.config.js`, hooked into `build.postcss` in `nuxt.config.js`. Only runs on production builds (`nuxt build` / `nuxt generate`).

It currently removes nothing. That is expected, not broken: `assets/sass/bootstrap_custom.scss` already imports only the Bootstrap parts we use and skips 14 modules entirely, so there is no dead Bootstrap left to strip. A build with and without the plugin produces identical CSS (15,131 selectors either way).

The safelist is long because PurgeCSS only keeps a rule if it can find the class name as a plain string in one of the `content` files, and plenty of our markup is not there: BootstrapVue output (`<b-container>` renders `class="container"`), state classes toggled by JS, Vue transition classes, names built from template literals, and CMS-authored HTML behind our `v-html` bindings. The groups in the config are named after those reasons, so new entries go in whichever one explains them.

**ATTENTION:** if you add a class name built from a template literal, i.e. `` `col-md-${n}` ``, add its prefix to `dynamicallyBuiltClasses` or that CSS gets stripped. Do not drop `htmlElements` either, PurgeCSS removes tag selectors too and without it `select` and `optgroup` are trimmed out of Bootstrap's reboot, which costs form controls their `font-family: inherit`.

To check a change, build with and without the plugin and diff the emitted CSS.

## Critical CSS
`build.extractCSS` emits the styles as content-hashed files instead of inlining them into the JS bundles, and [beasties](https://github.com/danielroe/beasties) inlines the rules the rendered markup uses into a `<style>` on every SSR response, deferring the rest with `rel="preload"` and a swap to `rel="stylesheet"`. Public SSR only.

Three things will silently break it:

- `pruneSource: false` — with it on, beasties rewrites the source `.css` on disk, so later requests extract from an already-pruned sheet.
- `reduceInlineStyles: false` — nuxt-speedkit renders an empty `<noscript><style></style></noscript>`, and beasties merges into the first `<style>` it may touch. On the default the critical CSS lands inside that noscript and never applies.
- beasties is loaded with `import('beasties')`, never `require`. Its CJS build does `require('css-select')`, and css-select is pure ESM, so a plain require throws `ERR_REQUIRE_ESM` on any runtime without `require(esm)` enabled. Node 20.19+/22.12+/24 locally allow it, the Lambda does not, so this only fails once deployed.
- `deploy-frontend.yml` keeps the `.css` files when it strips `.nuxt/dist/client`. The Lambda reads them off disk; without them beasties logs `Unable to locate stylesheet` and passes the HTML through unchanged.

## All commands
Check `package.json` for all possible commands.

## Runtime flags
Flag descriptions:

#### --native
```bash
yarn dev --native
```
To use magnolia native rest endpoints instead of bobcat.api

#### --ssr
```bash
yarn dev --ssr
```
Use to force ssr mode, otherwise universal SPA is being used

#### --author
```bash
yarn dev --author
```
If used, author instance is being called in api via `author` parameter

#### --modern
```bash
yarn dev --modern
```
Build using `modern` flag (https://nuxtjs.org/docs/configuration-glossary/configuration-modern/)
