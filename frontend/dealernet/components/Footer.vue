<template>
    <footer>
        <div class="bg-dark">
            <div class="container text-white">
                <div class="row py-5">
                    <div class="col-5">
                        <img :src="oneThoughImg.src" width="214" />
                    </div>
                    <div class="col-7">
                        <div class="row">
                            <div class="col" />
                            <div class="col-auto" v-for="(link, i) in footerLinks" :key="`footer-link__${i}`">
                                <a :href="link.url" class="font-weight-bold text-white">
                                    {{ link.label }}
                                </a>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <div class="bg-white pt-3">
            <div class="container">
                <div class="row">
                    <div class="col-12" v-html="disclaimer" />
                </div>
            </div>
        </div>
    </footer>
</template>

<script setup>
const props = defineProps({
  disclaimer: String,
  oneTough: Object,
  links: Object
})

const { $cloudinary } = useNuxtApp()

const oneThoughImg = {
  src: $cloudinary.generateUrl(props.oneTough.src)
}

const footerLinks = props?.links?.['@nodes']?.map(currentLink => {
  const link = props.links?.[currentLink]?.link
  const field = props.links?.[currentLink]?.field
  return {
    label: link[field + 'Label'],
    url: link[`${field}Url`]
  }
})
</script>