<template>
  <div>
    <a v-for="(link, index) in links"
       :key="'list-'+index"
       :href="link.url"
       :data-test-id="'cta-list-item_'+i"
    ><h3>{{ link.label }}</h3></a>
  </div>
</template>

<script>
export default {
  name: 'CTAList',
  props: {
    links: {
      type: Array,
      required: true
    }
  }
}
</script>
