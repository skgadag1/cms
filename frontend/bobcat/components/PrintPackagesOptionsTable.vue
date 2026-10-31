<template>
  <div class="specifications pb-3">
    <div class="package-row">
      <div
        v-for="(item, index) in optionChoices"
        :key="index"
        class="package-card"
      >
        <h6 class="size-20">{{ item.itemName }}</h6>
        <p class="size-12">{{ item.itemWebDesc }}</p>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  props: {
    optionChoices: {
      type: Array,
      required: true
    }
  }
}
</script>
