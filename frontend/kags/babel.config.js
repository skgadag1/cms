// Only used by Jest now (Vite/esbuild handle the app build directly, no Babel involved) --
module.exports = {
  presets: [
    ['@babel/preset-env', {targets: {node: 'current'}}]
  ]
}
