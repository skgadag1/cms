<template>
  <Error :error="error" />
</template>

<script>
import Error from '@/components/Error'
export default {
  components: {Error},
  props: ['error'],
  head() {
    return {
      title: `Error ${this.error.statusCode}`
    }
  }
}
</script>
