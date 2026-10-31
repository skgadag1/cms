<template>
  <div :class="['message-row', source]">
    <div :class="['message-bubble', `message-bubble-${source}`]">
      <div class="message-meta">
        <span v-if="source === 'assistant'" class="assistant-icon-wrap">
          <b-icon icon="bobcaticon-sparkles" />
        </span>
        <span class="message-author">{{ authorLabel }}</span>
        <span class="message-time">{{ timestamp }}</span>
      </div>
      <div v-if="content" class="message-content text-break" v-html="content"></div>
      <div v-if="files && files.length" class="message-files">
        <div v-for="(file, i) in files" :key="i" class="message-file-item">
          <div class="message-file-icon">
            <b-icon icon="filecopy" />
          </div>
          <div class="message-file-info">
            <span class="message-file-name">{{ truncateFileName(file.name) }}</span>
            <span class="message-file-size">{{ file.size }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { useLabels } from '@/composables/useLabels';
import { truncateFileName } from '@/utils';

export default {
  name: 'ChatMessage',
  conceptName: ['TroubleshootingProcedures'],
  setup() { return useLabels() },
  props: {
    source: {
      type: String,
      required: true
    },
    content: {
      type: String,
      required: false,
      default: ''
    },
    timestamp: {
      type: String,
      required: false,
      default: ''
    },
    files: {
      type: Array,
      required: false,
      default: () => []
    }
  },
  computed: {
    authorLabel() {
      if (this.source === 'user') {
        return this.translate('chatAuthorUserLabel', 'TroubleshootingProcedures')
      }
      return this.translate('chatAuthorAssistantLabel', 'TroubleshootingProcedures')
    }
  },
  methods: {
    truncateFileName
  }
}
</script>

<style lang="scss" scoped>
@import '../../scss/colors';
@import '../../scss/grid';

.message-bubble {
  border-radius: 20px;
  padding: 1rem 1.1rem;
  max-width: 75%;

  @include media-breakpoint-down('xl') {
    max-width: 100%;
  }
}

.message-bubble-user {
  background: $white;
  min-width: 250px;

  @include media-breakpoint-down('xl') {
    min-width: 150px;
  }
}

.message-bubble-assistant {
  background: $gray-800;
  color: $white;
  min-width: 360px;
  max-width: 620px;
  border-bottom-left-radius: 1.25rem;
  border-top-left-radius: 1.25rem;
  border-top-right-radius: 1.25rem;
  border-bottom-right-radius: 1.25rem;

  @include media-breakpoint-down('xl') {
    min-width: 200px;
  }
}

.message-meta {
  display: flex;
  align-items: center;
  gap: 0.45rem;
  font-size: 1rem;
  line-height: 1.1;
  margin-bottom: 0.75rem;
}

.message-author {
  font-weight: 500;
}

.message-time {
  font-size: 1rem;
}

.assistant-icon-wrap {
  font-size: 1.375rem;

  :deep(svg) {
    width: 1em;
    height: 1em;
  }
}

.message-row {
  display: flex;
}

.message-row.user {
  justify-content: flex-end;
}

.message-row.assistant {
  justify-content: flex-start;
}

.message-content {
  font-size: 1rem;
  line-height: 1.25;
}

.message-files {
  display: flex;
  flex-direction: column;
  gap: 0;
  margin-top: 0.5rem;
}

.message-file-item {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.65rem 0;
  border-bottom: 1px solid $gray-200;
}

.message-file-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 2.5rem;
  height: 2.5rem;
  background: $gray-800;
  border-radius: 6px;
  font-size: 1.2rem;
  color: $white;
  flex-shrink: 0;
}

.message-file-info {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.message-file-name {
  font-size: 0.9rem;
  font-weight: 500;
  color: $black;
  word-break: break-word;
}

.message-file-size {
  font-size: 0.8rem;
  color: $gray-600;
}
</style>
