export default {
  aspectRatio: '4:3',
  transition: 'slide',
  navigation: 'none',
  bgColor: 'transparent',
  themeProps: {
    active: '#ff3600'
  },
  thumbnailProps: {
    selectedStyle: 'all',
    selectedBorderWidth: 1,
    selectedBorderPosition: 'all',
    mediaSymbolShape: 'round',
    spacing: 16,
    width: 123,
    height: 123
  },
  zoomProps: {
    type: 'popup',
    trigger: 'click'
  },
  zoomPopupProps: {
    backdropOpacity: 0.8,
    buttonSize: 42,
    zIndex: 12000
  },
  loaderProps: {
    style: 'circle'
  },
  carouselLocation: 'bottom',
  carouselOffset: 16,
  navigationButtonProps: {
    color: '#FFFFFF',
    iconColor: '#333333'
  },
  viewportBreakpoints: [
    {breakpoint: 768, thumbnailProps: {height: 76, width: 76}},
    {breakpoint: 576, thumbnailProps: {height: 74, width: 74}},
    {breakpoint: 320, thumbnailProps: {height: 60, width: 60}}
  ],
  videoProps: {
    playerType: 'cloudinary',
    bigPlayButton: true,
    autoplay: false,
    sound: true,
    loop: false,
    muted: false,
    controls: 'all',
    playsinline: true,
    showLogo: false
  }
}
