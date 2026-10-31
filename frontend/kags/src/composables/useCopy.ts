import {ref} from 'vue'

export function useCopy() {
  const copy = ref<string | false>(false)

  const fallbackCopyTextToClipboard = (text: string): void => {
    const textArea = document.createElement('textarea')
    textArea.value = text

    // Avoid scrolling to bottom
    textArea.style.top = '0'
    textArea.style.left = '0'
    textArea.style.position = 'fixed'

    document.body.appendChild(textArea)
    textArea.focus()
    textArea.select()

    try {
      document.execCommand('copy')
    } catch (err) {
      /* eslint-disable no-console */
      console.error('Fallback: unable to copy', err)
    }

    document.body.removeChild(textArea)
  }

  const copyClicked = (text: string): boolean => copy.value === text

  const copyTextToClipboard = (text: string): void => {
    if (!navigator.clipboard) {
      fallbackCopyTextToClipboard(text)
      return
    }
    navigator.clipboard.writeText(text)
    copy.value = text
  }

  return {copy, fallbackCopyTextToClipboard, copyClicked, copyTextToClipboard}
}
