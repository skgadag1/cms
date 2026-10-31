<template>
  <div v-if="querySummaryState.firstSearchExecuted" class="summary-container">
    <div v-if="querySummaryState.hasResults && !querySummaryState.hasError && resultsLabel">
      <span part="results">
        {{ querySummaryState.lastResult }} {{ resultsLabel }}
      </span>
    </div>
  </div>
</template>

<script>
import {buildQuerySummary} from '@coveo/headless'

export default {
  props: {
    engine: {
      type: Object
    },
    resultsLabel: {
      type: String
    }
  },
  data() {
    return {
      querySummary: null,
      querySummaryState: {}
    }
  },
  mounted() {
    this.initialize()
  },
  methods: {
    initialize() {
      this.querySummary = buildQuerySummary(this.engine)
      this.querySummary.subscribe(() => {
        this.querySummaryState = this.querySummary.state
      })
    }
  }
}
</script>

<style>
  .summary-container {
    margin-bottom: 0px !important;
    font-size: 1rem;
  }
</style>
