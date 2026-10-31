module.exports = {
  testEnvironment: 'jsdom',
  setupFiles: ['<rootDir>/jest.setup.js'],
  moduleNameMapper: {
    '^@/(.*)$': '<rootDir>/$1',
    '^~/(.*)$': '<rootDir>/$1',
    '^vue$': 'vue/dist/vue.common.js',
    '\\.(jpg|jpeg|png|gif|svg|webp)$': '<rootDir>/tests/__mocks__/fileMock.js',
    '\\.(css|scss|sass)$': '<rootDir>/tests/__mocks__/styleMock.js'
  },
  moduleFileExtensions: ['js', 'vue', 'json'],
  transform: {
    '^.+\\.js$': 'babel-jest',
    '.*\\.(vue)$': '@vue/vue2-jest'
  },
  transformIgnorePatterns: [
    '/node_modules/(?!(@nuxtjs/composition-api|@vue/composition-api)/)'
  ],
  globals: {
    '@vue/vue2-jest': {
      babelConfig: true,
      compilerOptions: {
        whitespace: 'preserve'
      }
    }
  },
  collectCoverage: true,
  collectCoverageFrom: [
    '<rootDir>/components/**/*.vue',
    '<rootDir>/pages/**/*.vue',
    '!<rootDir>/tests/**'
  ],
  coveragePathIgnorePatterns: [
    '/node_modules/',
    '<rootDir>/tests/'
  ],
  verbose: true
}
