<template>
  <b-col cols="12"
         class="d-md-none mb-2"
  >
    <div class="box">
      <b-row class="mb-2">
        <b-col cols="12"
               class="font-condensed mb-2"
        >
          {{ item.title }}
        </b-col>
        <b-col cols="12">
          {{ item.short_description }}
        </b-col>
      </b-row>

      <b-row v-if="item && item.scoreValue" align-v="center" class="mb-2">
        <b-col cols="auto"
               class="font-condensed pe-0"
        >
          Rank
        </b-col>
        <b-col cols="5">
          <b-progress height=".5rem"
                      variant="success"
                      :value="item.scoreValue"
          />
        </b-col>
      </b-row>

      <b-row align-v="center" no-gutters class="flex-nowrap procedure-item-mobile__actions">
        <b-col cols="auto"
               class="font-condensed"
        >
          <b-button size="sm"
                 variant="primary"
                 class="font-condensed btn-start-session"
                 @click.prevent="$emit('sessionStart', item)"
          >
            Start session
          </b-button>
        </b-col>
        <b-col cols="auto" class="ms-auto">
          <b-button size="sm"
                 variant="primary"
                 class="font-condensed btn-details-icon"
                 @click.prevent="$emit('sessionDetails', item)"
          >
            <b-icon :icon="['fal', 'plus']" />
          </b-button>
        </b-col>
      </b-row>
    </div>
  </b-col>
</template>

<script>
export default {
  props: {
    item: {
      type: Object,
      required: true
    }
  }
}
</script>

<style lang="scss" scoped>
.procedure-item-mobile__actions {
  gap: 0.625rem;
}

.btn-start-session {
  width: 6.875rem;
  height: 1.625rem;
  padding: 0.375rem 1rem;
  border-radius: 0.125rem;
  display: flex;
  align-items: center;
  justify-content: center;
  text-transform: capitalize;
}

.btn-details-icon {
  height: 1.625rem;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
