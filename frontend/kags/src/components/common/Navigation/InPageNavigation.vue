<template>
  <b-col cols="12" lg="3" class="pe-lg-0 px-0 px-sm-3">
    <InPageNavigationMobile :disabled="disabled"
                            :loading="loading"
                            :items="items"
    />
    <InPageNavigationDesktop :disabled="disabled"
                             :loading="loading"
                             :items="items"
    />
  </b-col>
</template>

<script>
import InPageNavigationBase from '@/components/common/Navigation/InPageNavigationBase'

export default {
  extends: InPageNavigationBase
}
</script>
