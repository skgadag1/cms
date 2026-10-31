import c from 'cloudinary'
import cloudinaryUtils from '~/plugins/utils/cloudinaryUtils'

const $cloudinary = {
  image: {
    url: (id, opt = {}) => c.url(id, {
      ...opt,
      api_secret: '',
      cloud_name: 'doosan-bobcat-dev'
    })
  },
  video: {
    url: (id, opt = {}) => c.url(id, {
      ...opt,
      api_secret: '',
      cloud_name: 'doosan-bobcat-dev'
    })
  },
  getCloudinaryAsset: () => {}
}

export default {
  ...cloudinaryUtils,
  $cloudinary,
  $utils: {
    ...cloudinaryUtils,
    $cloudinary
  }
}
