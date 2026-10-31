import {decode} from 'js-base64'

const decodeAssetMetadata = (src) => {
    if (!src?.length) return {}
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
        if (fileData.length > 1) {
            metadata = JSON.parse(decode(fileData[1]))
        }
    } catch {
        // eslint-disable-next-line
        console.error(`Unable to get metadata object for asset: ${file}`)
    }

    return {
        publicId,
        metadata
    }
}

export default defineNuxtPlugin(nuxtApp => {
    const generateUrl = (src) => `${nuxtApp.$config.public.CLOUDINARY_BASEURL}${decodeAssetMetadata(src).publicId}` || undefined

    return {
        provide: {
            cloudinary: {
                generateUrl,
                getCloudinaryDocument: (src) => {
                    if (!src) {
                        return undefined
                    }

                    return generateUrl(src)?.replace('image/upload/f_auto,q_auto', 'raw/upload')
                }
            },
        }
    }
})