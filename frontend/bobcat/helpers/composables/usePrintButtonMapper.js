import {computed} from '@nuxtjs/composition-api'
import {BUTTON_NAMES} from '~/constants/buttons'

export default function usePrintButtonMapper(printPDF, route, modelID) {
  const mapPrintButtons = (printPDF) => {
    return printPDF?.['@nodes']
      ? printPDF['@nodes'].map(x => ({
        variant: BUTTON_NAMES.LINEAR,
        color: printPDF[x]?.color,
        title: printPDF[x]?.customLabel || '',
        icon: printPDF[x]?.icon || '',
        pricingPage: printPDF[x]?.pricingPage || 'false',
        attachmentsPage: printPDF[x]?.attachmentsPage || 'false',
        packagesPage: printPDF[x]?.packagesPage || 'false',
        target: printPDF[x]?.target || '_blank'
      }))
      : []
  }

  const printButton = computed(() => mapPrintButtons(printPDF || {}))

  const handlePrint = (button) => {
    const params = new URLSearchParams({
      print: 'true',
      modelID
    })
    const pages = []
    if (button.attachmentsPage === 'true') { pages.push('attachmentsPage') }
    if (button.pricingPage === 'true') { pages.push('pricingPage') }
    if (button.packagesPage === 'true') { pages.push('packagesPage') }
    if (pages.length) {
      params.append('pages', pages.join(','))
    }
    if (process.client) {
      const url = `${route.value.path}?${params.toString()}`
      window.open(
        url,
        button.target === '_blank_follow' ? '_blank' : button.target
      )
    }
  }

  return {printButton, handlePrint}
}
