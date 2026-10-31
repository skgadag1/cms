<template>
  <BasicFilterItem class="filter-item__code"
                   :title="title"
                   :options="options"
                   @hide="handleHide"
  >
    <div class="filter-item__code-input p-1">
      <b-input-group :class="{ 'disabled': disabled }">
        <b-input-group-text><b-icon icon="search" class="fw-bold text-black" /></b-input-group-text>

        <b-form-input id="popover-code-autocomplete"
                      v-model="keyword"
                      name="prefix"
                      data-test-id="filter-item__code-input"
                      debounce="300"
                      :disabled="disabled"
                      :placeholder="placeholder"
                      @change="handleChange($event.target.value)"
                      @keyup="fetchCodes"
                      @keydown.enter="handleHide"
        />
      </b-input-group>
      <div v-if="codes.length"
           class="popover-code bg-white border rounded"
           data-test-id="filter-item__options"
      >
        <button v-for="code in codes"
                :key="'filter-code-' + code.code"
                type="button"
                class="dropdown-item"
                @click="handleChange(code.code)"
        >
          {{ code.code }}
        </button>
      </div>
    </div>
  </BasicFilterItem>
</template>

<script>
import BasicFilterItem from '@/components/common/BasicFilterItem'
import {bdService} from '@/services'

export default {
  components: {
    BasicFilterItem
  },
  props: {
    disabled: {
      type: Boolean,
      required: false,
      default: false
    },
    title: {
      type: String,
      required: true
    },
    placeholder: {
      type: String,
      required: true
    },
    options: {
      type: Array,
      required: false,
      default() { return [] }
    }
  },
  data() {
    return {
      keyword: '',
      codes: []
    }
  },
  methods: {
    handleChange(code) {
      if (code.length) {
        this.keyword = code.toUpperCase()
        this.handleHide()
      }
    },
    handleHide() {
      if (this.keyword.length && !this.options.some(k => k.label.toUpperCase() === this.keyword.toUpperCase()) && this.codes.find(k => k.code.toUpperCase() === this.keyword.toUpperCase())) {
        this.options.push({label: this.keyword.toUpperCase(), value: true})
        this.keyword = ''
        this.codes = []
        this.$emit('updated')
      }
    },
    fetchCodes(e) {
      if (e.target.value.length >= 3 && !this.disabled) {
        bdService.fetchFaultCodesAutocomplete(e.target.value).then(r => this.codes = r.data)
      } else {
        this.codes = []
      }
    }
  }
}
</script>

<style lang="scss">
@import '../../scss/colors';
@import '../../scss/z-index';

.filter-item__code-input {
  position: relative;
}

// Matches the Bootstrap .popover look (border, radius, background, font size).
.popover-code {
  position: absolute;
  top: 100%;
  left: 50%;
  z-index: $zindex-popover;
  min-width: 7rem;
  max-height: 12.5rem;
  margin-top: .5rem;
  overflow-y: auto;
  transform: translateX(-50%);
  font-size: .875rem;
  .dropdown-item {
    cursor: pointer;
  }
}

.filter-item__code {
  button:not(.dropdown-item) {
    width: 10rem;
  }

  input,
  .input-group-text {
    padding: .5rem .3rem;
  }
}
</style>
