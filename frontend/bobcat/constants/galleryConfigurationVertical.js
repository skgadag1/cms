export default {
  aspectRatio: '6:4',
  transition: 'slide',
  navigation: 'always',
  thumbnailProps: {
    selectedStyle: 'all',
    selectedBorderWidth: 0,
    selectedBorderPosition: 'all',
    width: 258,
    height: 172
  },
  carouselLocation: 'right',
  carouselOffset: 50,
  viewportBreakpoints: [
    {breakpoint: 768, carouselOffset: 32, thumbnailProps: {height: 100, width: 150}},
    {breakpoint: 576, carouselOffset: 24, thumbnailProps: {height: 60, width: 90}},
    {breakpoint: 320, carouselOffset: 16, thumbnailProps: {height: 50, width: 75}}
  ]
}
