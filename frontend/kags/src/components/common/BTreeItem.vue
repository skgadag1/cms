<template>
  <li>
    <div :class="{'is-open':isOpen}" class="node-name cursor-pointer font-condensed" @click="toggle(node)">
      <div>{{ node.name }}</div>
    </div>
    <b-collapse v-model="isOpen" :class="{'is-open':isOpen}" tag="ul">
      <table v-if="node.type === 'Component'" class="node-details font-condensed pb-2">
        <tr v-for="item of node.child" :key="node.id + item.name">
          <td class="component-header">{{ item.name }}</td>
          <td v-for="tick of item.child" :key="node.id + item.name + tick" class="pb-1">
            <div v-if="tick.name" class="px-2">
              {{ tick.name }}
            </div>
            <div v-if="tick.labourTime && tick.labourTimeUnit" class="px-2 text-black">
              {{ tick.labourTime }} {{ tick.labourTimeUnit }} | {{ code }}-{{ item.code || '_' }}-{{ tick.code || '_' }}
            </div>
          </td>
        </tr>
      </table>
      <b-tree-item v-for="item in node.child" v-else :key="item.id" :node="item" :code="`${code}-${item.code || '_'}`" />
    </b-collapse>
  </li>
</template>

<script>
export default {
  name: 'BTreeItem',
  props: {
    node: {
      type: Object
    },
    code: {
      type: String
    }
  },
  data() {
    return {
      isOpen: false
    }
  },
  computed: {

  },
  mounted() {
    this.isOpen = this.node.child.length <= 1 && this.node.type === 'Sub System'
  },
  methods: {
    toggle() {
      this.isOpen = !this.isOpen
    }
  }
}
</script>
