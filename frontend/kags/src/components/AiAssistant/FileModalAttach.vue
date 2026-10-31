<template>
  <transition name="file-modal-fade">
    <div v-if="modelValue" class="file-modal-overlay" @mousedown.self="close">
      <div class="file-modal-dialog">
        <div
          class="drop-zone"
          :class="{'drop-zone--active': isDragging}"
          @dragover.prevent="isDragging = true"
          @dragleave.prevent="isDragging = false"
          @drop.prevent="onDrop"
        >
          <!-- Upload area -->
          <div class="drop-zone__icon-wrap">
            <b-icon icon="upload" />
          </div>
          <p class="drop-zone__text">{{ translate('dragDropText', 'TroubleshootingProcedures') }}</p>
          <label class="btn btn-primary drop-zone__browse-btn">
            {{ translate('selectFiles', 'TroubleshootingProcedures') }}
            <input ref="fileInput" type="file" multiple accept=".xls,.xlsx" class="d-none" @change="onFileSelect">
          </label>
          <p class="drop-zone__restrictions">{{ translate('fileTypeRestrictions', 'TroubleshootingProcedures') }}</p>

          <!-- File list -->
          <template v-if="files.length">
            <hr class="drop-zone__divider">

            <!-- Uploading section -->
            <template v-if="uploadingFiles.length">
              <p class="drop-zone__section-title">{{ translate('uploading', 'TroubleshootingProcedures') }}</p>
              <ul class="drop-zone__file-list">
                <li v-for="(file, i) in uploadingFiles" :key="'uploading-' + i" class="drop-zone__file-item">
                  <b-icon icon="circle-notch" class="drop-zone__file-spinner me-2" />
                  <div class="drop-zone__file-info">
                    <span class="drop-zone__file-name">{{ truncateFileName(file.name) }}</span>
                    <span class="drop-zone__file-size">{{ formatSize(file.size) }}</span>
                    <b-progress class="drop-zone__progress" :value="file.progress" max="100" variant="success" />
                  </div>
                  <b-button variant="link" class="p-0 ms-auto drop-zone__remove-btn" @click="cancelUpload(file)">
                    <b-icon icon="close" />
                  </b-button>
                </li>
              </ul>
            </template>
     <!-- Uploaded section -->
            <template v-if="uploadedFiles.length">
              <p class="drop-zone__section-title">{{ translate('uploadedFiles', 'TroubleshootingProcedures') }}</p>
              <ul class="drop-zone__file-list">
                <li v-for="(file, i) in uploadedFiles" :key="'uploaded-' + i" class="drop-zone__file-item">
                  <b-icon icon="check" class="drop-zone__file-check me-2" />
                  <div class="drop-zone__file-info">
                    <span class="drop-zone__file-name">{{ truncateFileName(file.name) }}</span>
                    <span class="drop-zone__file-size">{{ formatSize(file.size) }}</span>
                  </div>
                  <b-button variant="link" class="p-0 ms-auto drop-zone__remove-btn" @click="removeFile(file)">
                    <b-icon icon="bin" />
                  </b-button>
                </li>
              </ul>
            </template>

            <!-- Error section -->
            <div v-for="(file, i) in errorFiles" :key="'error-' + i" class="file-status-error">
              <span class="file-status-error-icon" aria-hidden="true">
                <b-icon icon="warning" />
              </span>
              <span class="file-status-error-text mb-0">{{ file.errorMessage }}</span>
            </div>
          </template>

          <!-- Add to Chat button -->
          <b-button
            v-if="uploadedFiles.length || uploadingFiles.length"
            variant="primary"
            class="drop-zone__add-btn mt-3"
            :disabled="!canAddToChat"
            @click="addToChat"
          >
            {{ translate('addToChat', 'TroubleshootingProcedures') }}
          </b-button>
        </div>
      </div>
    </div>
  </transition>
</template>

<script>
import { useLabels } from '@/composables/useLabels'
import { truncateFileName } from '@/utils'
export default {
  name: 'FileModalAttach',
  conceptName: ['TroubleshootingProcedures'],
  setup() { return useLabels() },
  props: {
    modelValue: {
      type: Boolean,
      required: true
    }
  },
  emits: ['update:modelValue', 'add-to-chat'],
  data() {
    return {
      isDragging: false,
      files: [],
      maxFileSize: 50 * 1024 * 1024 // 50 MB
    }
  },
  computed: {
    uploadingFiles() {
      return this.files.filter(f => f.status === 'uploading')
    },
    uploadedFiles() {
      return this.files.filter(f => f.status === 'uploaded')
    },
    errorFiles() {
      return this.files.filter(f => f.status === 'error')
    },
    canAddToChat() {
      return this.uploadedFiles.length > 0 &&
        this.uploadingFiles.length === 0 &&
        this.errorFiles.length === 0
    }
  },
  watch: {
    modelValue(val) {
      if (!val) this.cleanup()
    }
  },
  methods: {
    close() {
      this.$emit('update:modelValue', false)
    },
    cleanup() {
      this.files.forEach(f => clearInterval(f.intervalId))
      this.files = []
    },
    onDrop(e) {
      this.isDragging = false
      this.addFiles(Array.from(e.dataTransfer.files))
    },
    onFileSelect(e) {
      this.addFiles(Array.from(e.target.files))
      this.$refs.fileInput.value = ''
    },
    addFiles(newFiles) {
      const allowed = ['.xls', '.xlsx']
      newFiles.forEach(file => {
        const match = file.name.match(/(\.[^.]+)$/)
        const ext = match ? match[1].toLowerCase() : ''
        const isValidType = allowed.includes(ext)
        const isValidSize = file.size <= this.maxFileSize
        const entry = {
          name: file.name,
          size: file.size,
          status: 'uploading',
          progress: 0,
          raw: file
        }
        if (!isValidType) {
          entry.status = 'error'
          entry.errorMessage = this.translate('fileTypeError', 'TroubleshootingProcedures').replace('{filename}', truncateFileName(entry.name))
        } else if (!isValidSize) {
          entry.status = 'error'
          entry.errorMessage = this.translate('fileSizeError', 'TroubleshootingProcedures').replace('{filename}', truncateFileName(entry.name))
        }
        this.files.push(entry)
        if (entry.status === 'uploading') this.simulateUpload(entry)
      })
    },
    simulateUpload(entry) {
      entry.intervalId = setInterval(() => {
        if (!this.files.includes(entry)) {
          clearInterval(entry.intervalId)
          return
        }
        if (entry.progress >= 100) {
          clearInterval(entry.intervalId)
          entry.status = 'uploaded'
        } else {
          entry.progress += 20
        }
        this.files = [...this.files]
      }, 300)
    },
    cancelUpload(file) {
      clearInterval(file.intervalId)
      this.files = this.files.filter(f => f !== file)
    },
    removeFile(file) {
      this.files = this.files.filter(f => f !== file)
    },
    formatSize(bytes) {
      return (bytes / (1024 * 1024)).toFixed(1) + ' Mb'
    },
    truncateFileName,
    addToChat() {
      this.$emit('add-to-chat', this.uploadedFiles.map(f => ({
        name: f.name,
        size: this.formatSize(f.size)
      })))
      this.$emit('update:modelValue', false)
    }
  }
}
</script>

<style lang="scss" scoped>
@import '../../scss/colors';
@import '../../scss/grid';

.file-modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 1050;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 1rem;
}

.file-modal-dialog {
  background: $white;
  border-radius: 8px;
  width: 28rem;
  max-width: 100%;
  max-height: calc(100vh - 2rem);
  overflow-y: auto;

  @include media-breakpoint-down('md') {
    width: 100%;
  }
}

.file-modal-fade-enter-active,
.file-modal-fade-leave-active {
  transition: opacity 0.15s ease;
}
.file-modal-fade-enter,
.file-modal-fade-leave-to {
  opacity: 0;
}

.drop-zone {
  padding: 1.25rem 1rem;
  text-align: center;

  @include media-breakpoint-down('md') {
    padding: 1rem 0.75rem;
  }

  &--active {
    background-color: $gray-100;
    border-radius: 8px;
  }

  &__icon-wrap {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    width: 2.75rem;
    height: 2.75rem;
    background: $gray-200;
    border-radius: 50%;
    font-size: 1.25rem;
    margin-bottom: 0.75rem;
    color: $black;
  }

  &__text {
    font-size: 0.9rem;
    font-weight: bold;
    margin-bottom: 0.75rem;
  }

  &__browse-btn {
    display: inline-block;
    cursor: pointer;
    font-weight: bold;
    padding: 0.5rem 1.5rem;
    font-size: 0.9rem;
    margin-bottom: 0.5rem;

    @include media-breakpoint-down('md') {
      width: 100%;
    }
  }

  &__restrictions {
    font-size: 0.8rem;
    color: $gray-600;
    margin-bottom: 0;
  }

  &__divider {
    border-top: 1px solid $gray-300;
    margin: 1rem 0;
  }

  &__section-title {
    font-weight: bold;
    font-size: 0.95rem;
    text-align: center;
    margin-bottom: 0.5rem;
  }

  &__file-list {
    list-style: none;
    padding: 0;
    margin: 0;
    text-align: left;
  }

  &__file-item {
    display: flex;
    align-items: center;
    padding: 0.6rem 0;
    border-bottom: 1px solid $gray-200;

    &:first-child {
      border-top: 1px solid $gray-200;
    }
  }

  &__file-check {
    color: $black;
    font-size: 1rem;
    flex-shrink: 0;
    margin-right: 0.75rem;
  }

  &__file-spinner {
    color: $gray-500;
    font-size: 1.2rem;
    animation: spin 1s linear infinite;
    flex-shrink: 0;
    margin-right: 0.75rem;
  }

  &__file-info {
    display: flex;
    flex-direction: column;
    flex-grow: 1;
    min-width: 0;
  }

  &__file-name {
    font-size: 0.9rem;
    font-weight: 500;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  &__file-size {
    font-size: 0.8rem;
    color: $gray-600;
  }

  &__progress {
    height: 4px;
    margin-top: 0.35rem;
    border-radius: 2px;
  }

  &__remove-btn {
    color: $orange !important;
    flex-shrink: 0;
    font-size: 1.1rem;
    margin-left: 0.75rem;
  }

  .file-status-error {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    border: 2px solid $red;
    background-color: rgba($red, 0.1);
    color: $red;
    border-radius: 4px;
    padding: 1rem;
    text-align: left;
    margin-top: 0.75rem;
  }

  .file-status-error-icon {
    display: inline-flex;
    align-items: center;
    width: 1.5rem;
    min-width: 1.5rem;
    height: 1.5rem;
  }

  .file-status-error-text {
    font-family: 'Helvetica Neue Bold Condensed';
    font-size: 1.125rem;
  }

  &__add-btn {
    display: block;
    width: auto;
    font-weight: bold;
    padding: 0.5rem 1.75rem;
    font-size: 0.9rem;
    margin: 0 auto;

    @include media-breakpoint-down('md') {
      width: 100%;
    }
  }
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}
</style>
