module.exports = {
  testEnvironment: 'jest-environment-jsdom',
  moduleFileExtensions: ['js', 'ts', 'json', 'vue'],
  transform: {
    '^.+\\.vue$': '@vue/vue3-jest',
    '^.+\\.js$': '@swc/jest',
    '^.+\\.ts$': ['@swc/jest', {jsc: {parser: {syntax: 'typescript'}}}]
  },
  moduleNameMapper: {
    '^vue$': '@vue/compat',
    '^@/(.*)$': '<rootDir>/src/$1'
  },
  setupFiles: ['<rootDir>/jest.setup.ts'],
  globals: {
    // Mirrors vite.config.ts's vue() plugin compilerOptions -- without this, @vue/vue3-jest
    // compiles templates in strict Vue 3 mode, where `{{ x | filter }}` pipe syntax and other
    // compat-only template features silently fail (e.g. `_ctx.translate is not a function`).  
    'vue-jest': {
      compilerOptions: {
        compatConfig: {MODE: 2}
      }
    }
  }
}
