import Vue from 'vue'
import Router from 'vue-router'
import PageLoader from '~/helpers/PageLoader'
import {ERROR_ROUTE} from '~/constants/staticRoutes'

Vue.use(Router)

export function createRouter() {
  return new Router({
    mode: 'history',
    prefetchLinks: false,
    routes: [
      {path: ERROR_ROUTE},
      {path: '*', component: PageLoader}
    ]
  })
}
