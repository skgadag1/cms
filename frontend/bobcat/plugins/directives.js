import Vue from 'vue'

if (Vue.__clickoutside_directive__ !== true) {
  Vue.directive('click-outside', {
    bind(el, binding, vnode) {
      el.clickOutsideEvent = function (event) {
        // here I check that click was outside the el and his children
        if (!(el === event.target || el.contains(event.target))) {
          // and if it did, call method provided in attribute value
          vnode.context[binding.expression](event)
        }
      }
      document.body.addEventListener(binding.modifiers.mousedown ? 'mousedown' : 'click', el.clickOutsideEvent)
    },
    unbind(el, binding) {
      document.body.removeEventListener(binding.modifiers.mousedown ? 'mousedown' : 'click', el.clickOutsideEvent)
    }
  })
  Vue.__clickoutside_directive__ = true
}
