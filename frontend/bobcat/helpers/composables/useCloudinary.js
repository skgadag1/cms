import {useContext} from '@nuxtjs/composition-api'

export default function useCloudinary(_options = {}) {
  const {$utils, $cloudinary, $config} = useContext()
  const options = {crop: 'fill', width: 300, height: 200, ..._options}
  const placeholderImage = (() => $utils.getCloudinaryAsset($config.PLACEHOLDER_IMAGE, true, options))()
  const thumbnailOptions = {
    transformation: {
      crop: 'fill',
      height: 200,
      width: 200,
      dpr: 1.0,
      gravity: 'auto',
      quality: 'auto',
      fetchFormat: 'auto'
    },
    height: 200,
    width: 200,
    crop: 'fill',
    fetchFormat: null,
    quality: null
  }

  const getImageSizeVariants = (image, isImg, sizes, imageOptions) => {
    const internal = image.startsWith('cloudinary:')

    if (!sizes || !sizes.length) { return [] }

    return sizes.map((size) => {
      const opts = {
        ...imageOptions,
        width: size,
        crop: imageOptions.crop || 'fill',
        ...(['fill', 'thumb', 'crop'].includes(imageOptions.crop) && {gravity: imageOptions.gravity || 'auto'}),
        fetch_format: 'auto',
        quality: 'auto'
      }

      let url
      if (internal) {
        url = $utils.getCloudinaryAsset(image, isImg, opts)
      } else {
        const adjustedUrl = $cloudinary.image.url(image, opts)
        url = $utils.adjustDomain(adjustedUrl)
      }

      return `${url} ${size}w`
    })
  }

  const getGalleryPlaceholders = (src, publicId, isImg = true) => {
    const shouldContainVersion = publicId.includes('/')
    const suffix = isImg ? '?pgw=1' : '?pgw=1&tmb=1'

    const previewUrl = $utils.getCloudinaryAsset(src, isImg, {
      ...(!isImg && {resource_type: 'video'}),
      ...thumbnailOptions,
      ...(shouldContainVersion && {version: 1}),
      height: 450,
      width: 600,
      transformation: {
        ...(isImg && {background: '#f1f1f1'}),
        ...(thumbnailOptions.transformation),
        height: 450,
        width: 600,
        gravity: 'center'
      }
    }) + suffix

    const thumbnailUrl = $utils.getCloudinaryAsset(src, isImg, {
      ...(!isImg && {resource_type: 'video'}),
      ...thumbnailOptions,
      ...(shouldContainVersion && {version: 1})
    }) + suffix

    return {previewUrl, thumbnailUrl}
  }

  const mapCloudinaryGalleryAssets = assets => assets
    ?.['@nodes']
    ? (assets['@nodes'].filter(y =>
      (assets[y]?.asset?.field === 'img' && assets[y]?.asset?.img?.src) ||
      (assets[y]?.asset?.field === 'video' && assets[y]?.asset?.video?.src) ||
      (assets[y]?.asset?.field === 'spinset' && assets[y]?.asset?.tag) ||
      null
    ).map(x =>
      (assets[x]?.asset?.field === 'img' && assets[x]?.asset?.img) ||
      (assets[x]?.asset?.field === 'video' && assets[x]?.asset?.video) ||
      (assets[x]?.asset?.field === 'spinset' && {field: 'spin', tag: assets[x]?.asset?.tag}) ||
      null
    ).filter(z => z) || [])
    : []

  return {
    placeholderImage,
    getImageSizeVariants,
    getGalleryPlaceholders,
    mapCloudinaryGalleryAssets
  }
}
