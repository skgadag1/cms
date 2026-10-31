/// <reference types="vite/client" />

declare module '*.vue' {
  import type {DefineComponent} from 'vue'
  const component: DefineComponent<object, object, any>
  export default component
}

// Each page entry (src/components/<Name>/main.ts) exposes its mounted root instance on window.
interface Window {
  vm: any
  vm1: any
  vm2: any
  vm3: any
}
