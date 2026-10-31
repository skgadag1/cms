<template>
  <LazyHydrate never>
    <div class="row">
      <div v-for="i in items" :key="`listing-seleton-card-${i}`" class="col-12 mb-4" :class="`col-md-${mdCols} col-lg-${lgCols}`">
        <b-card no-body :img-top="imgPlacement === 'top'" :img-left="imgPlacement === 'left'">
          <BSkeletonImg v-if="imgPlacement !== 'none'" :card-img="imgPlacement" aspect="3:2" :width="imgPlacement === 'left' ? '100px' : ''" />
          <b-card-body>
            <b-card-title class="pt-2">
              <BSkeleton width="50%" />
            </b-card-title>

            <b-card-text class="mb-0">
              <BSkeleton width="80%" />
              <BSkeleton width="95%" />
              <BSkeleton width="75%" />
              <BSkeleton width="90%" />
              <BSkeleton width="55%" />
            </b-card-text>
          </b-card-body>
        </b-card>
      </div>
    </div>
  </LazyHydrate>
</template>

<script>
export default {
  props: {
    items: {
      type: Number,
      default: 9
    },
    cols: {
      type: Number,
      default: 3
    },
    imgPlacement: {
      type: String,
      default: 'top' // possible values: [top, left, none]
    }
  },
  setup(props) {
    const {cols} = props
    const mdCols = Math.ceil(16 / cols)
    const lgCols = 12 / cols

    return {
      mdCols,
      lgCols
    }
  }
}
</script>
