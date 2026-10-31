export default {
  isSmallMobile() {
    return (process.client) ? typeof window.matchMedia === 'function' && window.matchMedia('(max-width: 576px)').matches : false
  },
  isMobile() {
    return (process.client) ? typeof window.matchMedia === 'function' && window.matchMedia('(max-width: 768px)').matches : false
  },
  isTablet() {
    return (process.client) ? typeof window.matchMedia === 'function' && window.matchMedia('(max-width: 992px)').matches : false
  },
  isDesktopLG() {
    return (process.client) ? typeof window.matchMedia === 'function' && window.matchMedia('(min-width: 992px) and (max-width: 1140px)').matches : false
  },
  isDesktopXL() {
    return (process.client) ? typeof window.matchMedia === 'function' && window.matchMedia('(min-width: 1140px)').matches : false
  }
}
