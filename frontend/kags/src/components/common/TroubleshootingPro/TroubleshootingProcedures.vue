<template>
  <b-col tag="section" cols="12" :class="{ 'text-gray' : disabled }">
    <slot name="header" />

    <slot name="top-content" />

    <template v-if="enableTabs">
      <p>
         {{ translate('DescriptionTitle', 'TroubleshootingProcedures') }}
      </p>
      <b-tabs :index="activeTab" @update:index="activeTab = $event" nav-class="troubleshooting-tabs">
        <b-tab>
          <template v-slot:title>
             <b-icon icon="sparkles" class="me-1" />
            {{ translate('AiAssistantTab', 'TroubleshootingProcedures') }}
          </template>
          <keep-alive>
            <ai-assistant />
          </keep-alive>
        </b-tab>
        <b-tab>
          <template v-slot:title>
             <b-icon icon="fieldmods" class="me-1" />
            {{ translate('TraditionalTab', 'TroubleshootingProcedures') }}
          </template>
          <keep-alive>
            <TraditionalTroubleshooting :disabled="disabled" :config="config" />
          </keep-alive>
        </b-tab>
      </b-tabs>
    </template>

    <!-- No tabs: render traditional view directly (original behaviour) -->
    <TraditionalTroubleshooting v-else
                                 :disabled="disabled"
                                 :config="config"
    />
  </b-col>
</template>

<script>
import { useLabels } from '@/composables/useLabels'
import TraditionalTroubleshooting from '@/components/common/TroubleshootingPro/TraditionalTroubleshooting'
import AiAssistant from '@/components/common/TroubleshootingPro/AiAssistant'

export default {
  name: 'TroubleshootingProcedures',
  conceptName: ['TroubleshootingProcedures'],
  setup() { return useLabels() },
  components: {
    TraditionalTroubleshooting,
    AiAssistant
  },
  props: {
    disabled: {
      type: Boolean,
      required: false,
      default: false
    },
    config: {
      type: Object,
      required: false,
      default: () => ({})
    },
    enableTabs: {
      type: Boolean,
      required: false,
      default: false
    }
  },
  data() {
    return {
      activeTab: 0
    }
  }
}
</script>

<style lang="scss" scoped>
@import '@/scss/colors';

:deep(.tab-sparkles-icon) {
  width: 1rem;
  height: 1rem;
  vertical-align: middle;
  margin-bottom: 2px;
}

:deep(.troubleshooting-tabs) {
  border-bottom: 0;

  .nav-link {
    background-color: $white;
    color: $black;
    border-radius: 2px 2px 0 0;
    border-top: 4px solid transparent;
    border-left: 0;
    border-right: 0;
    border-bottom: 0;
    font-family: 'Helvetica Neue Bold Condensed';
    transition: 0.3s all;
    padding: 0.35rem 0.5rem;
  }

  .nav-link.active {
    background-color: $gray-100;
    border-color: $primary;
  }

  .nav-link:not(.active):hover {
    background-color: $gray-200;
    border-color: $gray-200;
  }

  .nav-item {
    margin-right: 0.5rem;
  }
}

:deep(.tab-content) {
  box-shadow: 0 4px 8px 0 rgba(0, 0, 0, 0.20);
  padding: 1rem;
}
</style>
