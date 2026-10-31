<template>
  <div class="troubleshooting-page">
    <div ref="chatStage" class="chat-stage">
      <ChatMessage
        v-for="(message, index) in messages"
        :key="index"
        :source="message.source"
        :content="message.content"
        :timestamp="message.timestamp"
        :files="message.files"
      />
    </div>
    <b-form-group class="w-100">
      <div class="chat-message-input-group">
        <b-dropdown
          v-if="hasFileUploadEnabled"
          variant="link"
          class="file-icon"
          toggle-class="py-0"
          no-caret
          placement="top-start"
        >
          <template #button-content>
            <b-icon icon="plus" />
          </template>
          <b-dropdown-text tag="h6" text-class="text-uppercase fw-bold px-2 mb-0">
            {{ translate('addContent', 'TroubleshootingProcedures') }}
          </b-dropdown-text>
          <b-dropdown-divider />
          <b-dropdown-item link-class="px-2" @click.prevent="isOpenFileModal = !isOpenFileModal">
            <b-icon icon="attachment" />
            {{ translate('uploadFile', 'TroubleshootingProcedures') }}
          </b-dropdown-item>
        </b-dropdown>
        <b-form-input
          v-model="inputText"
          class="chat-message-input"
          :placeholder="translate('InputPlaceholder', 'TroubleshootingProcedures')"
          @keydown.enter.prevent="sendMessage"
        />
        <b-button
          class="send-button"
          variant="primary"
          :disabled="!inputText.trim()"
          @click="sendMessage"
        >
          <b-icon icon="arrow-up" />
        </b-button>
      </div>
      <template #description>
        <div
          class="disclaimer"
          v-html="translate('Disclaimer', 'TroubleshootingProcedures')"
        ></div>
      </template>
    </b-form-group>
    <FileModal
      v-model="isOpenFileModal"
      @add-to-chat="onAddToChat"
    />
  </div>
</template>

<script>
import { useLabels } from '@/composables/useLabels'
import ChatMessage from '@/components/AiAssistant/ChatMessage';
import FileModal from '@/components/AiAssistant/FileModalAttach';
import { mapGetters } from 'vuex';

export default {
  name: 'AiAssistant',
  conceptName: ['TroubleshootingProcedures'],
  setup() { return useLabels() },
  components: {
    ChatMessage,
    FileModal
  },
  data() {
    return {
      inputText: '',
      messages: [],
      isOpenFileModal: false
    }
  },
  computed: {
    ...mapGetters(['getTroubleshootingKeyword','getFeatureFlag']),
    hasFileUploadEnabled(){
      return this.getFeatureFlag('serviceAIFile')
    }
  },
  watch: {
    messages() {
      this.scrollToBottom()
    }
  },
  mounted() {
    if (this.getTroubleshootingKeyword) {
      this.messages.push({
        source: 'user',
        content: this.getTroubleshootingKeyword,
        timestamp: this.getCurrentTimestamp()
      });
    }
  },
  methods: {
    getCurrentTimestamp() {
      return new Intl.DateTimeFormat('en-US', {
        hour: 'numeric',
        minute: '2-digit',
        hour12: true,
      }).format(new Date());
    },
    scrollToBottom() {
      this.$nextTick(() => {
        requestAnimationFrame(() => {
          const stage = this.$refs.chatStage
          if (!stage) return
          stage.scrollTop = stage.scrollHeight
        })
      })
    },
    sendMessage() {
      const text = this.inputText.trim();
      if (!text) return;
      this.messages.push({ source: 'user', content: text, timestamp: this.getCurrentTimestamp() });
      this.inputText = '';
    },
    onAddToChat(files) {
      this.messages.push({
        source: 'user',
        content: '',
        timestamp: this.getCurrentTimestamp(),
        files
      })
      this.isOpenFileModal = false
    },
  },
};
</script>

<style lang="scss" scoped>
@import '@/scss/colors';

.troubleshooting-page {
  padding: 0;
  background: $gray-100;
  box-shadow: 0 4px 8px 0 rgba(0, 0, 0, 0.2);
  height: 43rem;
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
  gap: 1rem;

  :deep(.form-control) {
    border-color: $black;
    color: $black;
  }

  :deep(.b-form-group) {
    border-color: $black;
  }

  .chat-stage {
    display: flex;
    flex-direction: column;
    justify-content: flex-end;
    width: 100%;
    gap: 1.25rem;
    margin-bottom: 0.5rem;
    height: calc(100% - 3.5rem);
    overflow-y: auto;
    padding: 1rem;
  }

  .chat-message-input {
    padding-left: 2.75rem;
  }

  .file-icon {
    position: absolute;
    left: 0.625rem;
    z-index: 1;

    :deep(.btn.btn-link) {
      padding: 0;
      color: $black;
    }

    :deep(.btn.dropdown-toggle.disabled) {
      background-color: transparent ;
      border-color: transparent ;
    }

    :deep(.bicon) {
      font-size: 1.4375rem;
      color: $black;
    }

    :deep(bicon) {
      height: 1.4375rem;
      width: .4375rem;
    }
  }

  .chat-message-input-group {
    display: flex;
    padding-right: 1rem;
    position: relative;
    align-items: center;
  }

  :deep(.b-form-group) {
    padding: 0.75rem 1rem;
  }

  .send-button {
    padding: 0;
    height: 2.625rem;
    width: 2.625rem;

    &:disabled {
      opacity: 0.5;
      background-color: $gray-600;
      border-color: $gray-600;
      cursor: not-allowed;
    }

    :deep(.bicon) {
      font-size: 1.4375rem;
      color: $white;
    }
  }

  .disclaimer {
    color: $black;

    :deep(p) ,
    :deep(h1) ,
    :deep(h2) ,
    :deep(h3) ,
    :deep(a) {
      padding: 0;
      margin: 0;
      color: $black;
    }
  }
}
</style>
