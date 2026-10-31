module.exports = {
  root: true,
  env: {
    browser: true,
    node: true
  },
  parserOptions: {
    parser: '@babel/eslint-parser',
    requireConfigFile: false,
    babelOptions: {
      plugins: ['@babel/plugin-syntax-jsx']
    }
  },
  extends: [
    '@nuxtjs',
    'plugin:nuxt/recommended',
    'plugin:vue/recommended',
    'eslint:recommended'
  ],
  plugins: [],
  // add your custom rules here
  rules: {
    'vue/component-name-in-template-casing': ['error', 'PascalCase'],
    'no-console': process.env.NODE_ENV === 'production' ? 'error' : 'warn',
    'no-debugger': process.env.NODE_ENV === 'production' ? 'error' : 'warn',
    'require-await': 'off',
    semi: ['warn', 'never'],
    quotes: ['warn', 'single'],
    'comma-dangle': ['warn', 'never'],
    'space-before-function-paren': ['warn', {
      anonymous: 'always',
      named: 'never',
      asyncArrow: 'always'
    }],
    'space-in-parens': ['warn'],
    'no-spaced-func': ['warn'],
    indent: ['error', 2],
    'space-infix-ops': 'warn',
    'vue/space-infix-ops': 'warn',
    'no-lone-blocks': 'warn',
    'no-multi-spaces': 'warn',
    'array-bracket-spacing': 'warn',
    'comma-spacing': 'warn',
    'key-spacing': 'warn',
    'no-multiple-empty-lines': 'warn',
    'no-trailing-spaces': 'warn',
    'arrow-spacing': 'warn',
    'vue/singleline-html-element-content-newline': 'off',
    'vue/require-default-prop': 'off',
    'object-curly-spacing': ['warn', 'never'],
    'padded-blocks': ['warn', 'never'],
    'vue/max-attributes-per-line': ['warn', {singleline: 5, multiline: {max: 1, allowFirstLine: true}}],
    'vue/require-prop-types': 'off',
    'vue/no-v-html': 'off',
    'vue/padding-line-between-blocks': ['warn', 'always'],
    'eol-last': ['error', 'always']
  },
  overrides: [
    {
      files: ['*.(vue|js)'],
      rules: {
        'no-unused-vars': 'warn'
      }
    }
  ],
  globals: {
    defineProps: 'readonly',
    defineEmits: 'readonly'
  }
}
