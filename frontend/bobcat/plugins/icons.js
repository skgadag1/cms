import Vue from 'vue'

const icons = () => import('~/external/bobcat-icons-plugin/src/index' /* webpackChunkName: 'bobcat-icons' */)

export default async function () {
  if (Vue.__icons_plugin__ !== true) {
    const _icons = await icons()
    Vue.use(_icons.default)
    Vue.__icons_plugin__ = true
  }
}
