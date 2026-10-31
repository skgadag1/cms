module.exports = {
  root: true,
  ignorePatterns: ['*.ts'],
  env: {
    node: true,
    es2022: true
  },
  'extends': [
    'plugin:vue/recommended',
    'eslint:recommended'
  ],
  rules: {
    'no-console': process.env.NODE_ENV === 'production' ? 'error' : 'warn',
    'no-debugger': process.env.NODE_ENV === 'production' ? 'error' : 'warn',
    'semi': ['warn', 'never'],
    'quotes': ['warn', 'single'],
    'comma-dangle': ['warn', 'never'],
    'space-before-function-paren': ['warn', {
      'anonymous': 'always',
      'named': 'never',
      'asyncArrow': 'always'
    }],
    'space-in-parens': ['warn'],
    'no-spaced-func': ['warn'],
    'indent': ['error', 2, {
      'ignoredNodes': ['TemplateLiteral']
    }],
    'space-infix-ops': 'warn',
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
    'vue/max-attributes-per-line': ['warn', {'singleline': 5, 'multiline': {'max': 1}}],
    'vue/require-prop-types': 'off',
    'vue/no-v-html': 'off',
    'vue/no-v-text-v-html-on-component': 'off',
    'vue/no-mutating-props': 'warn',
    'vue/v-slot-style': 'off',
    'vue/component-definition-name-casing': 'off',
    'vue/order-in-components': 'off',
    'vue/no-v-for-template-key': 'off',
    'vue/multi-word-component-names': 'off',
    'vue/no-reserved-component-names': 'off'
  },
  parserOptions: {
    ecmaVersion: 2022,
    sourceType: 'module'
  }
}
