
export default {
  downloadFile(blob, filename, isBlob = true, targetBlank = false) {
    let url = blob
    if (isBlob) {
      url = window.URL.createObjectURL(blob)
    }
    const a = document.createElement('a')
    a.href = url
    if (targetBlank) { a.target = '_blank' }
    a.download = filename
    document.body.appendChild(a)
    a.click()
    a.remove()
  }
}
