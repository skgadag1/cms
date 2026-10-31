<template>
  <div class="specifications pb-3 d-flex flex-column flex-wrap align-content-start">
    <template v-if="attachmentList.length">
      <PrintEquipmentItem v-for="(attachment, index) in attachmentList"
                          :key="index"
                          :name="attachment.name"
                          :amount-in-group="attachment.amountInGroup || ''"
      />
    </template>
  </div>
</template>

<script>
import PrintEquipmentItem from './elements/PrintEquipmentItem.vue'
export default {
  components: {PrintEquipmentItem},
  props: {
    attachmentList: {
      type: Array,
      required: true
    }
  }
}
</script>
