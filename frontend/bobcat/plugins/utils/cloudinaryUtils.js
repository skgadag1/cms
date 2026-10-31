import {decode} from 'js-base64'

function decodeAssetMetadata(src) {
  /*
  this function decode metadata from JCR value as:
  - public_id
  - format
  - secure_url
  - aspect_ratio
  - height
  - width
  & more...
   */

  const file = src['@id'] || src // nested nodes sometimes has object instead of string
  const fileData = file.includes('cloudinary:') ? file.split('cloudinary:')[1].split('#').filter(x => x) : ''
  const [publicId] = fileData
  let metadata = {}
  try {
    if (fileData.length > 1) { metadata = JSON.parse(decode(fileData[1])) }
  } catch {
    // eslint-disable-next-line
    console.error(`Unable to get metadata object for asset: ${file}`)
  }

  return {
    publicId,
    metadata
  }
}

function adjustDomain(url) {
  return this.$regions?.current?.name === 'cn' && process.env.CLOUDINARY_DOMAIN_CN ? url.replace('res.cloudinary.com', process.env.CLOUDINARY_DOMAIN_CN) : url
}

export default {
  getCloudinaryAsset(src, isImg = true, options = {}, returnSVG = false) {
    if (!src) {
      return undefined
    }

    const {publicId, metadata} = decodeAssetMetadata(src)

    if (returnSVG && metadata.format === 'svg') {
      return metadata.secure_url
    }

    if (!publicId) {
      return undefined
    }
    const shouldContainVersion = publicId.includes('/') && !publicId.match(/^v[0-9]+/)

    if (!options.version && shouldContainVersion && metadata.version) {
      options.version = metadata.version
    }

    if ((options.height || options.width) && !options.crop) {
      // by default there is no crop option set so width/height without it won't work and transformation
      // won't be applied. This is why this default "hack" is needed here
      options.crop = 'fill'
    }
    const url = this.$cloudinary ? this.$cloudinary[isImg ? 'image' : 'video'].url(publicId, options) : undefined
    return adjustDomain.call(this, url)
  },
  getCloudinaryAssetMetadata(x) {
    if (!x) { return undefined }
    return decodeAssetMetadata(x)
  },
  getCloudinaryAssetMediaType(meta) {
    const is3dImage = ['glb', 'objz'].includes(meta.format)
    return is3dImage ? '3d' : meta.resource_type
  },
  getCloudinaryDocument(src, options = {}) {
    if (!src) {
      return undefined
    }

    const {publicId, metadata} = decodeAssetMetadata(src)

    if (metadata.secure_url?.length) {
      return adjustDomain.call(this, metadata.secure_url)
    }

    if (!options.version && metadata.version) {
      options.version = metadata.version
    }
    const url = (this.$cloudinary.image.url(publicId, options) || undefined)?.replace('image/upload/f_auto,q_auto', 'raw/upload')
    return adjustDomain.call(this, url)
  },
  adjustDomain
}
