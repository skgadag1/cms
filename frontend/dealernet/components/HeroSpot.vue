<template>
    <div v-if="asset.src" class="container-fluid herospot" :style="`background-image:url(${asset.src})`" />
</template>

<script setup>
const { $cloudinary } = useNuxtApp()

const props = defineProps({
  img: Object
})

const asset = {
  ...(useVisibility().getVisibleContent(props.img)?.src && { src: $cloudinary.generateUrl(useVisibility().getVisibleContent(props.img)?.src) })
}
</script>

<style lang="scss">
.herospot {
    min-height: 250px;
    height: 250px;
    background-position: center center;
    background-repeat: norepeat;
    background-size: cover;
}
</style>